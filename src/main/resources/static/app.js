// 後端 API 根路徑（與本頁同網域，留空表示同 origin）
const API_BASE = '';

// 自動輪詢間隔（毫秒）：每 5 秒查一次最新方案
const POLL_MS = 5000;

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

// setInterval 用來做輪詢
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

/**
 * 將數字格式化為 TWD（無小數位）
 * @param {number} n
 * @returns {string} e.g. "NT$ 1,000"
 */
function fmtTwd(n) {
    return new Intl.NumberFormat('zh-Hant', {
        style: 'currency',
        currency: 'TWD',
        maximumFractionDigits: 0,
    }).format(n);
}

/**
 * 回傳「目前時間」的在地化字串（繁中、台灣）
 */
function nowStr() {
    const d = new Date();
    return d.toLocaleString('zh-Hant-TW');
}

/**
 * 將文字做 HTML escape 避免 XSS
 * @param {string} s
 */
function escapeHtml(s) {
    return String(s)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

/**
 * 顯示方案名稱，若無則回傳「方案」
 * @param {string} t
 */
function labelPlanType(t) {
    if (!t) return '方案';
    return escapeHtml(t);
}

// =========================
// 登入流程
// =========================

/**
 * 呼叫後端登入 API，依回傳 user 狀態切換畫面
 */
async function doLogin() {
    // 準備送給後端的登入資訊
    const body = {
        username: el.username.value.trim(),
        password: el.password.value,
    };

    // 禁用登入按鈕，避免連點
    el.loginBtn.disabled = true;
    el.loginStatus.textContent = '登入中…';

    try {
        // 呼叫後端登入 API
        const res = await fetch(`${API_BASE}/api/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Accept: 'application/json',
            },
            body: JSON.stringify(body),
        });

        // 非 2xx 視為錯誤
        if (!res.ok) {
            throw new Error(res.status === 401 ? '帳號或密碼錯誤' : `HTTP ${res.status}`);
        }

        // 取得後端回傳的 user 物件
        const user = await res.json();
        currentUser = user;

        
        const res2 = await fetch(`/dashboard/user-flight-name?newFlightId=${user.newFlightId}`);
        if (!res2.ok) throw new Error("找不到 flightId");

        const flight = await res2.json();
        
        // 如果 Flight 裡面還包含你的自訂 Status 物件
        // 你可以直接用，例如：
        // console.log(flight.status.delayMinutes);

        

        // 登入後先隱藏登入頁、詳情頁、付款頁
        el.loginSection.style.display = 'none';
        el.planDetailSection.style.display = 'none';
        el.paymentSection.style.display = 'none';

        if (user.profileCompleted) {
            // 已經填過航班 + 飯店資料，直接進主頁
            el.profileSection.style.display = 'none';
            el.mainSection.style.display = 'block';

            renderUserInfo(user,flight); // 顯示使用者資訊
            startAuto();          // 開始自動輪詢方案
            el.loginStatus.textContent = '登入成功';
        } else {
            // 尚未填資料，改導向 profile 填寫頁
            el.mainSection.style.display = 'none';
            el.profileSection.style.display = 'block';

            // 若後端有帶資料，就填入表單欄位
            el.profileFullName.value = user.fullName || '';
            el.profileHotelName.value = user.hotelName || '';
            el.profileHotelAddress.value = user.hotelAddress || '';
            el.profileFlightId.value = user.flightId != null ? user.flightId : '';

            el.profileStatus.textContent = '請先完成航班與飯店資料設定';
        }
    } catch (e) {
        // 顯示錯誤訊息
        el.loginStatus.textContent = `登入失敗：${e.message}`;
    } finally {
        // 無論成功失敗都重新啟用按鈕
        el.loginBtn.disabled = false;
    }
}

/**
 * 將 currentUser 的資料顯示在主頁的 banner
 */
function renderUserInfo(user,flight) {
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

/**
 * 呼叫後端 /dashboard/plans/{flightId} 取得備援方案
 * 並更新畫面（方案表格、狀態、價格拉桿）
 */
async function fetchPlans() {
    // 若尚未登入或已有 fetch 正在進行則忽略
    if (!currentUser) return;
    if (isFetching) return;
    isFetching = true;

    const flightId = currentUser.flightId;
    const url = `${API_BASE}/dashboard/plans/${flightId}`;

    try {
        el.statusText.textContent = '載入中…';

        // 呼叫後端 dashboard/plans API
        const res = await fetch(url, { headers: { Accept: 'application/json' } });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        // 取得方案列表（可能是空陣列）
        const data = await res.json();
        allPlans = data || [];

        // 第一次載入方案時初始化拉桿，
        // 之後只更新 min/max 並保留使用者原本選擇
        updatePriceSliderRange(allPlans, { keepUserSelection: priceInitialized });
        priceInitialized = true;

        // 根據目前價格上限做過濾
        const filtered = applyPriceFilter();
        renderPlans(filtered);

        // 更新主頁上方統計數字
        el.planCount.textContent = filtered.length;
        el.lastUpdated.textContent = nowStr();

        // 根據方案有無來顯示航班狀態
        el.flightStatus.innerHTML =
            allPlans.length > 0
                ? '<span class="ok">延誤已偵測，已產生方案</span>'
                : '<span class="warn">尚未偵測到延誤</span>';

        el.statusText.textContent =
            allPlans.length > 0 ? '已更新' : '尚無方案，等待系統判定延誤…';
    } catch (e) {
        // 顯示錯誤、清空表格
        el.statusText.innerHTML = `<span class="err">讀取失敗：${e.message}</span>`;
        el.flightStatus.innerHTML = '<span class="err">未知</span>';
        el.plansTable.style.display = 'none';
        allPlans = [];
    } finally {
        isFetching = false;
    }
}

// =========================
// 價格拉桿相關
// =========================

/**
 * 根據目前方案列表，設定價格拉桿的 min / max
 * @param {Array} plans
 * @param {Object} options
 * @param {boolean} options.keepUserSelection 是否保留使用者原本的選擇
 */
function updatePriceSliderRange(plans, { keepUserSelection = false } = {}) {
    // 若無方案資料
    if (!plans || plans.length === 0) {
        el.priceSlider.disabled = true;
        el.priceLabel.textContent = '不限';
        el.priceStatus.textContent = '目前沒有可用方案。';
        priceFilterMax = null;
        return;
    }

    // 收集所有 cost
    const costs = plans.map((p) => p.cost || 0);
    const minCost = Math.min(...costs);
    const maxCost = Math.max(...costs);

    // 調整 range 範圍
    el.priceSlider.min = String(minCost);
    el.priceSlider.max = String(maxCost);
    el.priceSlider.step = '500';
    el.priceSlider.disabled = minCost === maxCost;

    if (!keepUserSelection || priceFilterMax == null) {
        // 第一次或不保留舊選擇：預設為最高價
        priceFilterMax = maxCost;
    } else {
        // 若方案改變，舊的選擇超出新範圍 => 修正為 min / max
        if (priceFilterMax < minCost) priceFilterMax = minCost;
        if (priceFilterMax > maxCost) priceFilterMax = maxCost;
    }

    // 更新滑桿與文字顯示
    el.priceSlider.value = String(priceFilterMax);
    el.priceLabel.textContent = '≤ ' + fmtTwd(priceFilterMax);
    el.priceStatus.textContent = '拖曳拉桿，可只顯示小於等於指定金額的方案。';
}

/**
 * 套用目前價格上限後，回傳要顯示的方案清單
 */
function applyPriceFilter() {
    if (!allPlans || allPlans.length === 0) return [];
    if (priceFilterMax == null) return allPlans;
    return allPlans.filter((p) => (p.cost || 0) <= priceFilterMax);
}

// =========================
// 渲染方案表格
// =========================

/**
 * 將方案清單渲染到表格上
 * @param {Array} list
 */
function renderPlans(list) {
    // 沒有資料 => 隱藏表格
    if (!Array.isArray(list) || list.length === 0) {
        el.plansTable.style.display = 'none';
        el.plansBody.innerHTML = '';
        return;
    }

    el.plansTable.style.display = 'table';

    // 逐筆方案組成 <tr>，用 template literal 建成 HTML 字串
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

/**
 * 儲存使用者的航班與飯店設定到後端，再載入主頁
 */
async function saveFlightProfile() {
    if (!currentUser) {
        // 理論上不會發生（必須已登入才會到這頁）
        return;
    }

    const body = {
        fullName: el.profileFullName.value.trim(),
        hotelName: el.profileHotelName.value.trim(),
        hotelAddress: el.profileHotelAddress.value.trim(),
        flightId: el.profileFlightId.value ? Number(el.profileFlightId.value) : null,
    };

    // 前端簡單驗證：不可為空
    if (!body.fullName || !body.hotelName || !body.hotelAddress || !body.flightId) {
        el.profileStatus.innerHTML = '<span class="err">請完整填寫所有欄位</span>';
        return;
    }

    el.profileSaveBtn.disabled = true;
    el.profileStatus.textContent = '儲存中…';

    try {
        // 呼叫後端更新 flight profile
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

        // 後端回傳最新 user 資料
        const user = await res.json();
        currentUser = user;

        const res2 = await fetch(`/dashboard/user-flight-name?newFlightId=${user.newFlightId}`);
        if (!res2.ok) throw new Error("找不到 flightId");

        const flight = await res2.json();

        // 切換到主頁
        el.profileSection.style.display = 'none';
        el.planDetailSection.style.display = 'none';
        el.paymentSection.style.display = 'none';
        el.mainSection.style.display = 'block';

        renderUserInfo(user,flight);
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

/**
 * 回到登入畫面並重置所有狀態
 */
function showLogin() {
    // 停止輪詢
    stopAuto();

    // 清空全域狀態
    currentUser = null;
    allPlans = [];
    priceFilterMax = null;
    selectedPlan = null;
    priceInitialized = false;

    // UI：只顯示登入區
    el.loginSection.style.display = 'block';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'none';

    // 重設主頁顯示數值
    el.statusText.textContent = '等待資料…';
    el.flightStatus.textContent = '未知';
    el.planCount.textContent = '0';
    el.lastUpdated.textContent = '—';
    el.plansTable.style.display = 'none';
    el.plansBody.innerHTML = '';

    // 重設價格滑桿狀態
    el.priceSlider.disabled = true;
    el.priceLabel.textContent = '不限';
    el.priceStatus.textContent = '拖曳拉桿，可只顯示小於等於指定金額的方案。';

    // 提示需重新登入與重新填寫
    el.profileStatus.textContent = '請輸入您的航班與飯店資料';
    el.loginStatus.textContent = '請重新登入';

    // 清空方案詳情
    el.detailPlanType.textContent = '—';
    el.detailArrivalTime.textContent = '—';
    el.detailCost.textContent = '—';
    el.detailContent.textContent = '—';

    // 清空付款頁狀態
    clearPaymentForm();
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';
}

// =========================
// 顯示方案詳情頁
// =========================

/**
 * 顯示單一方案的詳情（並停止輪詢）
 * @param {Object} plan
 */
function showPlanDetail(plan) {
    // 記錄使用者選中的方案（付款會用到）
    selectedPlan = plan;
    // 進入詳情頁後關閉輪詢
    stopAuto();

    // 將方案細節填入 UI
    el.detailPlanType.textContent = labelPlanType(plan.planType);
    el.detailArrivalTime.textContent = plan.arrivalTime || '—';
    el.detailCost.textContent = fmtTwd(plan.cost || 0);
    el.detailContent.textContent = plan.detail || '—';

    // 切換區塊：只顯示詳情頁
    el.loginSection.style.display = 'none';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.paymentSection.style.display = 'none';
    el.planDetailSection.style.display = 'block';
}

// 方案表格「選擇」按鈕使用的全域函式
// 透過 planType 找到對應方案並打開詳情頁
window.selectPlan = function (planType) {
    const plan = allPlans.find((p) => p.planType === planType);
    if (!plan) {
        alert('找不到此方案資料');
        return;
    }
    showPlanDetail(plan);
};

/**
 * 詳情頁返回主頁（或登入頁）
 */
function backToMainFromDetail() {
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'none';

    if (currentUser && currentUser.profileCompleted) {
        // 若仍有已登入的 user，回主頁並恢復輪詢
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        // 否則回登入
        el.loginSection.style.display = 'block';
    }
}

// =========================
// 進入付款頁
// =========================

/**
 * 進入付款頁前：檢查是否有選方案、填好訂單資訊
 */
function goToPayment() {
    if (!selectedPlan) {
        alert('尚未選擇方案');
        return;
    }

    // 進入付款後先停止輪詢
    stopAuto();

    const userName = currentUser
        ? currentUser.fullName || currentUser.username || ''
        : '';
    const planName = labelPlanType(selectedPlan.planType);

    // 訂單資訊：使用者 / 航班 / 方案名稱
    el.payOrderInfo.textContent = `${userName} / 航班 Flight ${currentUser ? currentUser.flightId : ''
        } / ${planName}`;

    // 應付金額
    el.payAmount.textContent = fmtTwd(selectedPlan.cost || 0);

    // 清空表單欄位與狀態
    clearPaymentForm();
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    // 顯示表單區，隱藏結果區
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';

    // 切換區塊：只顯示付款頁
    el.loginSection.style.display = 'none';
    el.profileSection.style.display = 'none';
    el.mainSection.style.display = 'none';
    el.planDetailSection.style.display = 'none';
    el.paymentSection.style.display = 'block';
}

/**
 * 清空付款表單欄位
 */
function clearPaymentForm() {
    el.payCardNumber.value = '';
    el.payCardExpiry.value = '';
    el.payCardCvv.value = '';
    el.payCardHolder.value = '';
}

/**
 * 付款頁返回方案詳情頁
 */
function backToDetailFromPayment() {
    // 清空表單 + 重設狀態提示
    clearPaymentForm();
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    // 隱藏付款區，顯示原本的詳情或主頁 / 登入
    el.paymentSection.style.display = 'none';
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';

    if (selectedPlan) {
        // 若仍有選取方案，回詳情頁
        el.planDetailSection.style.display = 'block';
    } else if (currentUser && currentUser.profileCompleted) {
        // 沒有選取方案但仍登入中 => 回主頁
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        // 未登入 => 回登入頁
        el.loginSection.style.display = 'block';
    }
}

// =========================
// Demo 付款送出（不串真正金流）
// =========================

/**
 * 檢查表單欄位（Demo），顯示付款成功畫面
 */
function submitPaymentDemo() {
    if (!selectedPlan) {
        el.payStatus.innerHTML = '<span class="err">尚未選擇方案。</span>';
        return;
    }

    // 基本欄位檢查（只是示意，不做 Luhn 檢查）
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

    // 模擬付款成功：
    // 1. 隱藏表單
    // 2. 顯示付款結果與方案細節
    el.paymentFormArea.style.display = 'none';
    el.paymentResultArea.style.display = 'block';

    el.payResultStatus.textContent = '付款成功，感謝您的使用。';
    el.payResultPlanType.textContent = labelPlanType(selectedPlan.planType);
    el.payResultArrivalTime.textContent = selectedPlan.arrivalTime || '—';
    el.payResultCost.textContent = fmtTwd(selectedPlan.cost || 0);
    el.payResultContent.textContent = selectedPlan.detail || '—';
}

/**
 * 付款成功頁：返回登入（同時重置所有狀態）
 */
function backToMainFromPaymentResult() {
    showLogin();
}

/**
 * 付款成功頁：取消此方案，回主頁重新選擇
 */
function cancelPlanFromPaymentResult() {
    if (!selectedPlan) {
        alert('目前沒有選擇中的方案');
        return;
    }
    const ok = confirm('確定要取消此方案嗎？');
    if (!ok) return;

    // 單純把選中方案清空（方案列表仍存在）
    selectedPlan = null;

    // 重置付款 UI
    clearPaymentForm();
    el.paymentFormArea.style.display = 'block';
    el.paymentResultArea.style.display = 'none';
    el.payStatus.textContent = '此為示範畫面，不會真的進行扣款。';

    // 關閉付款區，回主頁（若有登入）或登入頁
    el.paymentSection.style.display = 'none';
    if (currentUser && currentUser.profileCompleted) {
        el.mainSection.style.display = 'block';
        startAuto();
    } else {
        el.loginSection.style.display = 'block';
    }
}

// =========================
// 自動輪詢控制
// =========================

/**
 * 開始自動輪詢：先立即抓一次，再每 POLL_MS 抓一次
 */
function startAuto() {
    // 先確保沒有舊的 timer
    stopAuto();
    if (!currentUser || currentUser.flightId == null) return;
    // 立即 fetch 一次
    fetchPlans();
    // 之後每 POLL_MS 再抓一次
    timer = setInterval(fetchPlans, POLL_MS);
}

/**
 * 停止自動輪詢
 */
function stopAuto() {
    if (timer) clearInterval(timer);
    timer = null;
}

// 當分頁被隱藏 / 顯示時，動態啟停輪詢，省資源
document.addEventListener('visibilitychange', () => {
    if (document.hidden) {
        stopAuto();
    } else {
        if (
            currentUser &&
            currentUser.flightId != null &&
            el.mainSection.style.display === 'block'
        ) {
            startAuto();
        }
    }
});

// =========================
// 事件綁定：一開始載入時執行
// =========================

// 登入按鈕
el.loginBtn.addEventListener('click', doLogin);
// 密碼輸入框按 Enter 也能登入
el.password.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') doLogin();
});

// 儲存航班與飯店資料
el.profileSaveBtn.addEventListener('click', saveFlightProfile);

// 返回登入按鈕（主頁 / profile 皆有）
el.backToLoginFromProfile.addEventListener('click', showLogin);
el.backToLoginFromMain.addEventListener('click', showLogin);

// 詳情頁返回主頁
el.backToMainFromDetail.addEventListener('click', backToMainFromDetail);

// 詳情頁進入付款
el.goToPaymentBtn.addEventListener('click', goToPayment);

// 付款頁返回詳情
el.backToDetailFromPayment.addEventListener('click', backToDetailFromPayment);

// Demo 付款送出
el.paySubmitBtn.addEventListener('click', submitPaymentDemo);

// 付款成功頁：返回登入
el.backToMainFromPaymentResult.addEventListener(
    'click',
    backToMainFromPaymentResult
);

// 付款成功頁：取消此方案
el.cancelPlanFromPaymentResult.addEventListener(
    'click',
    cancelPlanFromPaymentResult
);

// 價格拉桿事件：使用者調整時即時套用價格篩選
// 不會因為 fetchPlans 重新拉資料而重置（因為用 priceFilterMax 保留狀態）
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
