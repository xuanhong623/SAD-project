package com.example.flightrescue.service;

import org.springframework.stereotype.Service;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.Plan;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class RecommendationService {

        public void generatePlans(Flight f) {
                // 先清掉這個航班舊的方案
                InMemoryData.plans.removeIf(p -> p.getFlightId().equals(f.getFlightId()));

                Long flightId = f.getFlightId();

                // 方案 A：最快抵達，但價錢較高
                InMemoryData.plans.add(new Plan(
                                flightId,
                                "方案 A",
                                16000,
                                "今晚 23:30 抵達飯店",
                                "新航班：TPE → KIX 19:00 起飛\n" +
                                                "鐵路：關西機場快線 → 難波\n" +
                                                "飯店：原訂飯店加價保留今晚住宿"));

                // 方案 B：平衡價格與時間
                InMemoryData.plans.add(new Plan(
                                flightId,
                                "方案 B",
                                9000,
                                "明天 09:30 抵達飯店",
                                "新航班：TPE → ITM 明早 06:30 起飛\n" +
                                                "鐵路：大阪單軌電車 → 地下鐵\n" +
                                                "飯店：改住機場附近飯店一晚"));

                // 方案 C：最省錢，但抵達時間較晚
                InMemoryData.plans.add(new Plan(
                                flightId,
                                "方案 C",
                                6500,
                                "明天 14:00 抵達飯店",
                                "新航班：TPE → KIX 明早 09:00 起飛\n" +
                                                "鐵路：搭乘南海電鐵普通車\n" +
                                                "飯店：今晚改住市區商務旅館"));

                System.out.println("✅ 已生成組合備援方案 (flightId=" + f.getFlightId() + ")");
        }
}
