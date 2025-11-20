package com.example.flightrescue.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.Plan;
import com.example.flightrescue.model.PlanLoader;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class RecommendationService {

        private final PlanLoader planLoader;

        public RecommendationService(PlanLoader planLoader) {
                this.planLoader = planLoader;
        }

        public void generatePlans(Flight f) {
                // 先清掉舊的方案
                InMemoryData.plans.removeIf(p -> p.getFlightId().equals(f.getFlightId()));
                Long flightId = f.getFlightId();

                // 1) 優先從 CSV 載入真資料
                try {
                        List<Plan> real = planLoader.loadFromCsv("hotels_with_maps.csv", flightId);
                        if (!real.isEmpty()) {
                                InMemoryData.plans.addAll(real);
                                System.out.println("✅ 已載入 CSV 真資料 (" + real.size() + " 筆, flightId=" + flightId + ")");
                                return; // 有真資料就不再塞假資料
                        } else {
                                System.out.println("⚠️ CSV 為空或載入失敗，改用預設假資料");
                        }
                } catch (Exception e) {
                        System.out.println("⚠️ 讀取 CSV 失敗，改用預設假資料: " + e.getMessage());
                }

                // 2) 後備：塞入預設假資料
                addFallbackSamples(flightId);
        }

        private void addFallbackSamples(Long flightId) {
                InMemoryData.plans.add(new Plan(
                                flightId, "方案 A", 16000, "今晚 23:30 抵達飯店",
                                "新航班：TPE → KIX 19:00 起飛\n鐵路：關西機場快線 → 難波\n飯店：原訂飯店加價保留今晚住宿"));

                InMemoryData.plans.add(new Plan(
                                flightId, "方案 B", 9000, "明天 09:30 抵達飯店",
                                "新航班：TPE → ITM 明早 06:30 起飛\n鐵路：大阪單軌電車 → 地下鐵\n飯店：改住機場附近飯店一晚"));

                InMemoryData.plans.add(new Plan(
                                flightId, "方案 C", 6500, "明天 14:00 抵達飯店",
                                "新航班：TPE → KIX 明早 09:00 起飛\n鐵路：搭乘南海電鐵普通車\n飯店：今晚改住市區商務旅館"));

                System.out.println("✅ 已生成預設方案 (flightId=" + flightId + ")");
        }
}
