// 後端 API 根路徑（與本頁同網域，留空表示同 origin）
const API_BASE = '';

// 統一存放所有會用到的 DOM 元素，方便管理與維護
const el = {
    // 各主要區塊
    loginSection: document.getElementById('loginSection'),
    mainSection: document.getElementById('mainSection'),
    profileSection: document.getElementById('profileSection'),
    planDetailSection: document.getElementById('planDetailSection'),
    paymentSection: document.getElementById('paymentSection'),

    paymentFormArea: document.getElementById('paymentFormArea'),
    paymentResultArea: document.getElementById('paymentResultArea'),

    // 登入相關
    username: document.getElementById('username'),
    password: document.getElementById('password'),
    loginBtn: document.getElementById('loginBtn'),
    loginStatus: document.getElementById('loginStatus'),

    // 主畫面顯示用
    userInfo: document.getElementById('userInfo'),
    flightStatus: document.getElementById('flightStatus'),
    planCount: document.getElementById('planCount'),
    lastUpdated: document.getElementById('lastUpdated'),
    statusText: document.getElementById('statusText'),
    plansTable: document.getElementById('plansTable'),
    plansBody: document.getElementById('plansBody'),
    priceSlider: document.getElementById('priceSlider'),
    priceLabel: document.getElementById('priceLabel'),
    priceStatus: document.getElementById('priceStatus'),

    // 手動更新方案按鈕
    refreshPlansBtn: document.getElementById('refreshPlansBtn'),

    // 資料輸入頁
    profileFullName: document.getElementById('profileFullName'),
    profileHotelName: document.getElementById('profileHotelName'),
    profileHotelAddress: document.getElementById('profileHotelAddress'),
    profileFlightId: document.getElementById('profileFlightId'),
    profileSaveBtn: document.getElementById('profileSaveBtn'),
    profileStatus: document.getElementById('profileStatus'),

    // 返回登入按鈕
    backToLoginFromProfile: document.getElementById('backToLoginFromProfile'),
    backToLoginFromMain: document.getElementById('backToLoginFromMain'),

    // 方案詳情元素
    detailPlanType: document.getElementById('detailPlanType'),
    detailArrivalTime: document.getElementById('detailArrivalTime'),
    detailCost: document.getElementById('detailCost'),
    detailContent: document.getElementById('detailContent'),
    backToMainFromDetail: document.getElementById('backToMainFromDetail'),
    goToPaymentBtn: document.getElementById('goToPaymentBtn'),

    // 付款頁元素
    payOrderInfo: document.getElementById('payOrderInfo'),
    payAmount: document.getElementById('payAmount'),
    payCardNumber: document.getElementById('payCardNumber'),
    payCardExpiry: document.getElementById('payCardExpiry'),
    payCardCvv: document.getElementById('payCardCvv'),
    payCardHolder: document.getElementById('payCardHolder'),
    backToDetailFromPayment: document.getElementById('backToDetailFromPayment'),
    paySubmitBtn: document.getElementById('paySubmitBtn'),
    payStatus: document.getElementById('payStatus'),

    // 付款結果元素
    payResultStatus: document.getElementById('payResultStatus'),
    payResultPlanType: document.getElementById('payResultPlanType'),
    payResultArrivalTime: document.getElementById('payResultArrivalTime'),
    payResultCost: document.getElementById('payResultCost'),
    payResultContent: document.getElementById('payResultContent'),
    backToMainFromPaymentResult: document.getElementById('backToMainFromPaymentResult'),
    cancelPlanFromPaymentResult: document.getElementById('cancelPlanFromPaymentResult'),
};

// =========================
// 全域狀態變數
// =========================

// 保留 timer 變數供 stopAuto 清掉（現在不再使用 setInterval）
let timer = null;
// 避免同時多次觸發 fetchPlans()
let isFetching = false;
// 登入成功後的 user 資料
let currentUser = null;
// 從後端抓到的所有備援方案
let allPlans = [];
// 價格篩選上限（range 目前的值）
let priceFilterMax = null;
// 使用者目前選中的方案
let selectedPlan = null;
// 價格拉桿是否已依方案初始化
let priceInitialized = false;

// =========================
// 小工具函式：格式化 / 時間字串
// =========================

function fmtTwd(n) {
    return new Intl.NumberFormat('zh-Hant', {
        style: 'currency',
        currency: 'TWD',
        maximumFractionDigits: 0,
    }).format(n);
}

function nowStr() {
    const d = new Date();
    return d.toLocaleString('zh-Hant-TW');
}

function escapeHtml(s) {
    return String(s)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function labelPlanType(t) {
    if (!t) return '方案';
    return escapeHtml(t);
}

// =========================
// 登入流程
// =========================

async function doLogin() {
    const body = {
        username: el.username.value.trim(),
        password: el.password.value,
    };

    el.loginBtn.disabled = true;
    el.loginStatus.textContent = '登入中…';

    try {
        const res = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
            body: JSON.stringify(body),
        });

        if (!res.ok) {
            throw new Error(res.status === 401 ? '帳號或密碼錯誤' : `HTTP ${res.status}`);
        }

        const user = await res.json();
        currentUser = user;

        // 登入後先隱藏登入頁、詳情頁、付款頁
        el.loginSection.style.display = 'none';
        el.planDetailSection.style.display = 'none';
        el.paymentSection.style.display = 'none';

        if (user.profileCompleted) {
            // 已經填過航班 + 飯店資料，直接進主頁
            el.profileSection.style.display = 'none';
            el.mainSection.style.display = 'block';

            const res2 = await fetch(`/dashboard/user-flight-name?newFlightId=${user.newFlightId}`);
            if (!res2.ok) throw new Error('找不到 flightId');

            const flight = await res2.json();

            renderUserInfo(user, flight);
            startAuto(); // 進主頁時手動檢查一次延誤 + 抓方案
            el.loginStatus.textContent = '登入成功';
        } else {
            // 尚未填資料，改導向 profile 填寫頁
            el.mainSection.style.display = 'none';
            el.profileSection.style.display = 'block';

            el.profileFullName.value = user.fullName || '';
            el.profileHotelName.value = user.hotelName || '';
            el.profileHotelAddress.value = user.hotelAddress || '';
            el.profileFlightId.value = user.flightId != null ? user.flightId : '';

            el.profileStatus.textContent = '請先完成航班與飯店資料設定';
        }
    } catch (e) {
        el.loginStatus.textContent = `登入失敗：${e.message}`;
    } finally {
        el.loginBtn.disabled = false;
    }
}

function renderUserInfo(user, flight) {
    el.userInfo.innerHTML = `
        <div><strong>${escapeHtml(user.fullName || user.username || '')}</strong></div>
        <div>綁定航班：<span class="badge">Flight ${escapeHtml(flight.flightName)}</span></div>
        <div>從${escapeHtml(flight.fromCity || '')} 去 ${escapeHtml(flight.toCity || '')}</div>
        <div>飯店：${escapeHtml(user.hotelName || '—')}</div>
        <div>地址：${escapeHtml(user.hotelAddress || '—')}</div>
    `;
}

// =========================
// 依照 flightId 取得備援方案
// =========================

async function fetchPlans() {
    if (!currentUser) return;
    if (isFetching) return;
    isFetching = true;

    const flightId = currentUser.flightId;
    const url = `${API_BASE}/dashboard/plans/${flightId}`;

    try {
        el.statusText.textContent = '載入中…';

        const res = await fetch(url, { headers: { Accept: 'application/json' } });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const data = await res.json();
        allPlans = data || [];

        updatePriceSliderRange(allPlans, { keepUserSelection: priceInitialized });
        priceInitialized = true;

        const filtered = applyPriceFilter();
        renderPlans(filtered);

        el.planCount.textContent = filtered.length;
        el.lastUpdated.textContent = nowStr();

        el.flightStatus.innerHTML =
            allPlans.length > 0
                ? '<span class="ok">延誤已偵測，已產生方案</span>'
                : '<span class="warn">尚未偵測到延誤</span>';

        el.statusText.textContent =
            allPlans.length > 0 ? '已更新' : '尚無方案，等待系統判定延誤…';
    } catch (e) {
        el.statusText.innerHTML = `<span class="err">讀取失敗：${e.message}</span>`;
        el.flightStatus.innerHTML = '<span class="err">未知</span>';
        el.plansTable.style.display = 'none';
        allPlans = [];
    } finally {
        isFetching = false;
    }
}

/**
 * 先請後端檢查一次延誤狀態，再抓最新方案
 * 後端需要有 POST /dashboard/check-delay 這支 API
 */
async function checkDelayAndFetchPlans() {
    if (!currentUser || currentUser.flightId == null) return;

    try {
        el.statusText.textContent = '後端檢查延誤中…';

        await fetch(`${API_BASE}/dashboard/check-delay`, {
            method: 'POST',
        });
    } catch (e) {
        console.error('check-delay 失敗', e);
        // 失敗仍繼續抓方案，避免畫面完全卡住
    }

    await fetchPlans();
}

// =========================
// 價格拉桿相關
// =========================

function updatePriceSliderRange(plans, { keepUserSelection = false } = {}) {
    if (!plans || plans.length === 0) {
        el.priceSlider.disabled = true;
        el.priceLabel.textContent = '不限';
        el.priceStatus.textContent = '目前沒有可用方案。';
        priceFilterMax = null;
        return;
    }

    const costs = plans.map((p) => p.cost || 0);
    const minCost = Math.min(...costs);
    const maxCost = Math.max(...costs);

    el.priceSlider.min = String(minCost);
    el.priceSlider.max = String(maxCost);
    el.priceSlider.step = '500';
    el.priceSlider.disabled = minCost === maxCost;

    if (!keepUserSelection || priceFilterMax == null) {
        priceFilterMax = maxCost;
    } else {
        if (priceFilterMax < minCost) priceFilterMax = minCost;
        if (priceFilterMax > maxCost) priceFilterMax = maxCost;
    }

    el.priceSlider.value = String(priceFilterMax);
    el.priceLabel.textContent = '≤ ' + fmtTwd(priceFilterMax);
    el.priceStatus.textContent = '拖曳拉桿，可只顯示小於等於指定金額的方案。';
}

function applyPriceFilter() {
    if (!allPlans || allPlans.length === 0) return [];
    if (priceFilterMax == null) return allPlans;
    return allPlans.filter((p) => (p.cost || 0) <= priceFilterMax);
}

// =========================
// 渲染方案表格
// =========================

function renderPlans(list) {
    if (!Array.isArray(list) || list.length === 0) {
        el.plansTable.style.display = 'none';
        el.plansBody.innerHTML = '';
        return;
    }

    el.plansTable.style.display = 'table';

    el.plansBody.innerHTML = list
        .map(
            (p) => `
                <tr>
                  <td>${labelPlanType(p.planType)}</td>
                  <td>
                    <pre style="margin:0;font-size:12px;white-space:pre-wrap;">
${escapeHtml(p.detail || '')}
                    </pre>
                  </td>
                  <td>${escapeHtml(p.arrivalTime || '')}</td>
                  <td>${fmtTwd(p.cost || 0)}</td>
                  <td><button onclick="selectPlan('${p.planType}')">選擇</button></td>
                </tr>
            `
        )
        .join('');
}

// =========================
// 儲存航班與飯店資料
// =========================

async function saveFlightProfile() {
    if (!currentUser) {
        return;
    }

    const body = {
        fullName: el.profileFullName.value.trim(),
        hotelName: el.profileHotelName.value.trim(),
        hotelAddress: el.profileHotelAddress.value.trim(),
        flightId: el.profileFlightId.value ? Number(el.profileFlightId.value) : null,
    };

    if (!body.fullName || !body.hotelName || !body.hotelAddress || !body.flightId) {
        el.profileStatus.innerHTML = '<span class="err">請完整填寫所有欄位</span>';
        return;
    }

    el.profileSaveBtn.disabled = true;
    el.profileStatus.textContent = '儲存中…';

    try {
        const res = await fetch(
            `${API_BASE}/api/users/${encodeURIComponent(currentUser.username)}/flight-profile`,
            {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                },
                body: JSON.stringify(body),
            }
        );

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const user = await res.json();
        currentUser = user;

        

        const res2 = await fetch(`/dashboard/user-flight-name?newFlightId=${user.newFlightId}`);
        if (!res2.ok) throw new Error('找不到 flightId');

        const flight = await res2.json();

        el.profileSection.style.display = 'none';
        el.planDetailSection.style.display = 'none';
        el.paymentSection.style.display = 'none';
        el.mainSection.style.display = 'block';

        renderUserInfo(user, flight);
        startAuto();

        el.statusText.textContent = '已載入航班與方案資料';
    } catch (e) {
        el.profileStatus.innerHTML = `<span class="err">儲存失敗：${e.message}</span>`;
    } finally {
        el.profileSaveBtn.disabled = false;
    }
}

// =========================
// 返回登入頁（登出 / reset 狀態）
// =========================

function showLogin() {
    stopAuto();

    currentUser = null;
    allPlans = [];
    priceFilterMax = null;
    selectedPlan = null;
    priceInitialized = false;

    el.loginSection.style.display = 'block';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'none';

    el.statusText.textContent = '等待資料…';
    el.flightStatus.textContent = '未知';
    el.planCount.textContent = '0';
    el.lastUpdated.textContent = '—';
    el.plansTable.style.display = 'none';
    el.plansBody.innerHTML = '';

    el.priceSlider.disabled = true;
    el.priceLabel.textContent = '不限';
    el.priceStatus.textContent = '拖曳拉桿，可只顯示小於等於指定金額的方案。';

    el.profileStatus.textContent = '請輸入您的航班與飯店資料';
    el.loginStatus.textContent = '請重新登入';

    el.detailPlanType.textContent = '—';
    el.detailArrivalTime.textContent = '—';
    el.detailCost.textContent = '—';
    el.detailContent.textContent = '—';

    clearPaymentForm();
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';
}

// =========================
// 顯示方案詳情頁
// =========================

function showPlanDetail(plan) {
    selectedPlan = plan;
    stopAuto();

    el.detailPlanType.textContent = labelPlanType(plan.planType);
    el.detailArrivalTime.textContent = plan.arrivalTime || '—';
    el.detailCost.textContent = fmtTwd(plan.cost || 0);
    el.detailContent.textContent = plan.detail || '—';

    el.loginSection.style.display = 'none';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.paymentSection.style.display = 'none';
    el.planDetailSection.style.display = 'block';
}

window.selectPlan = function (planType) {
    const plan = allPlans.find((p) => p.planType === planType);
    if (!plan) {
        alert('找不到此方案資料');
        return;
    }
    showPlanDetail(plan);
};

function backToMainFromDetail() {
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'none';

    if (currentUser && currentUser.profileCompleted) {
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        el.loginSection.style.display = 'block';
    }
}

// =========================
// 進入付款頁
// =========================

function goToPayment() {
    if (!selectedPlan) {
        alert('尚未選擇方案');
        return;
    }

    stopAuto();

    const userName = currentUser
        ? currentUser.fullName || currentUser.username || ''
        : '';
    const planName = labelPlanType(selectedPlan.planType);

    el.payOrderInfo.textContent = `${userName} / 航班 Flight ${currentUser ? currentUser.flightId : ''
        } / ${planName}`;

    el.payAmount.textContent = fmtTwd(selectedPlan.cost || 0);

    clearPaymentForm();
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';

    el.loginSection.style.display = 'none';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'block';
}

function clearPaymentForm() {
    el.payCardNumber.value = '';
    el.payCardExpiry.value = '';
    el.payCardCvv.value = '';
    el.payCardHolder.value = '';
}

function backToDetailFromPayment() {
    clearPaymentForm();
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    el.paymentSection.style.display = 'none';
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';

    if (selectedPlan) {
        el.planDetailSection.style.display = 'block';
    } else if (currentUser && currentUser.profileCompleted) {
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        el.loginSection.style.display = 'block';
    }
}

// =========================
// Demo 付款送出（不串真正金流）
// =========================

function submitPaymentDemo() {
    if (!selectedPlan) {
        el.payStatus.innerHTML = '<span class="err">尚未選擇方案。</span>';
        return;
    }

    const num = el.payCardNumber.value.replace(/\s+/g, '');
    const exp = el.payCardExpiry.value.trim();
    const cvv = el.payCardCvv.value.trim();
    const holder = el.payCardHolder.value.trim();

    if (!num || num.length < 12) {
        el.payStatus.innerHTML =
            '<span class="err">請輸入正確的卡號（Demo）。</span>';
        return;
    }
    if (!exp) {
        el.payStatus.innerHTML = '<span class="err">請輸入有效期限。</span>';
        return;
    }
    if (!cvv || cvv.length < 3) {
        el.payStatus.innerHTML = '<span class="err">請輸入安全碼。</span>';
        return;
    }
    if (!holder) {
        el.payStatus.innerHTML =
            '<span class="err">請輸入持卡人姓名。</span>';
        return;
    }

    el.paymentFormArea.style.display = 'none';
    el.paymentResultArea.style.display = 'block';

    el.payResultStatus.textContent = '付款成功，感謝您的使用。';
    el.payResultPlanType.textContent = labelPlanType(selectedPlan.planType);
    el.payResultArrivalTime.textContent = selectedPlan.arrivalTime || '—';
    el.payResultCost.textContent = fmtTwd(selectedPlan.cost || 0);
    el.payResultContent.textContent = selectedPlan.detail || '—';
}

function backToMainFromPaymentResult() {
    showLogin();
}

function cancelPlanFromPaymentResult() {
    if (!selectedPlan) {
        alert('目前沒有選擇中的方案');
        return;
    }
    const ok = confirm('確定要取消此方案嗎？');
    if (!ok) return;

    selectedPlan = null;

    clearPaymentForm();
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    el.paymentSection.style.display = 'none';
    if (currentUser && currentUser.profileCompleted) {
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        el.loginSection.style.display = 'block';
    }
}

// =========================
// 自動更新控制（改為只在需要時抓一次）
// =========================

function startAuto() {
    // 現在改成「進主頁時手動檢查一次延誤 + 抓方案」，不再 setInterval
    if (!currentUser || currentUser.flightId == null) return;
    checkDelayAndFetchPlans();
}

function stopAuto() {
    if (timer) clearInterval(timer);
    timer = null;
}

// =========================
// 事件綁定：一開始載入時執行
// =========================

el.loginBtn.addEventListener('click', doLogin);
el.password.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') doLogin();
});

el.profileSaveBtn.addEventListener('click', saveFlightProfile);

el.backToLoginFromProfile.addEventListener('click', showLogin);
el.backToLoginFromMain.addEventListener('click', showLogin);

el.backToMainFromDetail.addEventListener('click', backToMainFromDetail);

el.goToPaymentBtn.addEventListener('click', goToPayment);

el.backToDetailFromPayment.addEventListener('click', backToDetailFromPayment);

el.paySubmitBtn.addEventListener('click', submitPaymentDemo);

el.backToMainFromPaymentResult.addEventListener(
    'click',
    backToMainFromPaymentResult
);

el.cancelPlanFromPaymentResult.addEventListener(
    'click',
    cancelPlanFromPaymentResult
);

// 手動更新方案按鈕：請後端檢查延誤後抓方案
el.refreshPlansBtn.addEventListener('click', () => {
    checkDelayAndFetchPlans();
});

// 價格拉桿事件：使用者調整時即時套用價格篩選
el.priceSlider.addEventListener('input', () => {
    if (!allPlans || allPlans.length === 0) {
        el.priceLabel.textContent = '不限';
        return;
    }
    const v = Number(el.priceSlider.value);
    priceFilterMax = v;
    el.priceLabel.textContent = '≤ ' + fmtTwd(v);

    const filtered = applyPriceFilter();
    renderPlans(filtered);
    el.planCount.textContent = filtered.length;
});

// 初始化：一開始尚未有方案，先停用拉桿
el.priceSlider.disabled = true;
