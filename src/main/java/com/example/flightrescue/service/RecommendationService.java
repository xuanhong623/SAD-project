// com.example.flightrescue.service.RecommendationService
package com.example.flightrescue.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.flightrescue.config.PlanRankingProperties;
import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.Plan;
import com.example.flightrescue.model.PlanLoader;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class RecommendationService {

        private final PlanLoader planLoader;
        private final PlanRankingProperties weights;

        public RecommendationService(PlanLoader planLoader, PlanRankingProperties weights) {
                this.planLoader = planLoader;
                this.weights = weights;
        }

        public void generatePlans(Flight f) {
                // 1) 清除舊資料
                Long flightId = f.getFlightId();
                InMemoryData.plans.removeIf(p -> p.getFlightId().equals(flightId));

                // 2) 取得候選方案（CSV 優先，否則 fallback）
                List<Plan> candidates = new ArrayList<>();
                try {
                        List<Plan> real = planLoader.loadFromCsv("hotels_with_maps.csv", flightId);
                        if (!real.isEmpty()) {
                                candidates.addAll(real);
                                System.out.println("✅ 已載入 CSV 真資料 (" + real.size() + " 筆, flightId=" + flightId + ")");
                        } else {
                                System.out.println("⚠️ CSV 為空或載入失敗，改用預設假資料");
                                addFallbackSamples(candidates, flightId);
                        }
                } catch (Exception e) {
                        System.out.println("⚠️ 讀取 CSV 失敗，改用預設假資料: " + e.getMessage());
                        addFallbackSamples(candidates, flightId);
                }

                // 3) 計分與排序
                List<Plan> ranked = rankAndSort(candidates);

                // 4) 寫入（已排序）
                InMemoryData.plans.addAll(ranked);
                System.out.println("✅ 已寫入排序後方案 (flightId=" + flightId + ")");
        }

        private void addFallbackSamples(List<Plan> out, Long flightId) {
                out.add(new Plan(
                                flightId, "方案 A", 16000, "今晚 23:30 抵達飯店",
                                "新航班：TPE → KIX 19:00 起飛\n鐵路：關西機場快線 → 難波\n飯店：原訂飯店加價保留今晚住宿",
                                null, null));
                out.add(new Plan(
                                flightId, "方案 B", 9000, "明天 09:30 抵達飯店",
                                "新航班：TPE → ITM 明早 06:30 起飛\n鐵路：大阪單軌電車 → 地下鐵\n飯店：改住機場附近飯店一晚",
                                null, null));
                out.add(new Plan(
                                flightId, "方案 C", 6500, "明天 14:00 抵達飯店",
                                "新航班：TPE → KIX 明早 09:00 起飛\n鐵路：搭乘南海電鐵普通車\n飯店：今晚改住市區商務旅館",
                                null, null));
                System.out.println("✅ 已生成預設方案 (flightId=" + flightId + ")");
        }

        /**
         * 依權重計分後排序（高分在前）。
         * - price：越便宜越好
         * - rating：越高越好
         * - driveMinutes：越短越好
         * 缺值處理：用群組中位數替補；若全為缺值則該指標不參與計分。
         */
        private List<Plan> rankAndSort(List<Plan> plans) {
                if (plans.isEmpty())
                        return plans;

                // 蒐集指標值
                List<Integer> prices = plans.stream().map(Plan::getCost).collect(Collectors.toList());
                List<Double> ratings = plans.stream().map(Plan::getRating).filter(Objects::nonNull)
                                .collect(Collectors.toList());
                List<Integer> drives = plans.stream().map(Plan::getDriveMinutes).filter(Objects::nonNull)
                                .collect(Collectors.toList());

                Double medianRating = ratings.isEmpty() ? null : median(ratings);
                Integer medianDrive = drives.isEmpty() ? null : medianInt(drives);

                int minPrice = prices.stream().min(Integer::compareTo).orElse(0);
                int maxPrice = prices.stream().max(Integer::compareTo).orElse(0);
                double minRating = ratings.isEmpty() ? 0 : ratings.stream().min(Double::compareTo).get();
                double maxRating = ratings.isEmpty() ? 0 : ratings.stream().max(Double::compareTo).get();
                int minDrive = drives.isEmpty() ? 0 : drives.stream().min(Integer::compareTo).get();
                int maxDrive = drives.isEmpty() ? 0 : drives.stream().max(Integer::compareTo).get();

                double wPrice = weights.getWeightPrice();
                double wRating = weights.getWeightRating();
                double wDrive = weights.getWeightDrive();

                // 若某指標無法比較（如全缺值或 min==max），將其權重視為 0
                if (minPrice == maxPrice)
                        wPrice = 0;
                if (ratings.isEmpty() || minRating == maxRating)
                        wRating = 0;
                if (drives.isEmpty() || minDrive == maxDrive)
                        wDrive = 0;

                double wSum = wPrice + wRating + wDrive;
                // 避免全 0（例如只有價格），自動正規化
                double normPrice = wSum == 0 ? 0 : wPrice / wSum;
                double normRating = wSum == 0 ? 0 : wRating / wSum;
                double normDrive = wSum == 0 ? 0 : wDrive / wSum;

                for (Plan p : plans) {
                        // 缺值補中位數
                        Double r = p.getRating() == null ? medianRating : p.getRating();
                        Integer d = p.getDriveMinutes() == null ? medianDrive : p.getDriveMinutes();

                        double priceScore = (maxPrice == minPrice) ? 1.0
                                        : (double) (maxPrice - p.getCost()) / (maxPrice - minPrice); // 便宜→分數高
                        double ratingScore = (r == null || maxRating == minRating) ? 0.0
                                        : (r - minRating) / (maxRating - minRating); // 高→分數高
                        double driveScore = (d == null || maxDrive == minDrive) ? 0.0
                                        : (double) (maxDrive - d) / (maxDrive - minDrive); // 近→分數高

                        double score = normPrice * priceScore + normRating * ratingScore + normDrive * driveScore;
                        p.setScore(score);
                }

                // 高分在前；同分以價格較低者在前，再以 planType 穩定排序
                return plans.stream()
                                .sorted(Comparator
                                                .comparing(Plan::getScore,
                                                                Comparator.nullsLast(Comparator.reverseOrder()))
                                                .thenComparing(Plan::getCost)
                                                .thenComparing(Plan::getPlanType))
                                .collect(Collectors.toList());
        }

        private static Double median(List<Double> xs) {
                if (xs.isEmpty())
                        return null;
                List<Double> s = new ArrayList<>(xs);
                Collections.sort(s);
                int n = s.size();
                return (n % 2 == 1) ? s.get(n / 2) : (s.get(n / 2 - 1) + s.get(n / 2)) / 2.0;
        }

        private static Integer medianInt(List<Integer> xs) {
                if (xs.isEmpty())
                        return null;
                List<Integer> s = new ArrayList<>(xs);
                Collections.sort(s);
                int n = s.size();
                return (n % 2 == 1) ? s.get(n / 2) : (s.get(n / 2 - 1) + s.get(n / 2)) / 2;
        }
}
