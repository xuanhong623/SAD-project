package com.example.flightrescue.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class DelayMonitorService {

    @Autowired
    RecommendationService recommendationService;

    /**
     * 原本是 @Scheduled(fixedRate = 10000) 的方法，
     * 現在改成讓 Controller 在需要時手動呼叫。
     */
    public void checkFlightDelay() {
        for (Flight f : InMemoryData.flights) {
            // 判斷是否已超過「起飛時間 + 30 分鐘」
            if (LocalDateTime.now().isAfter(f.getDepartTime().plusMinutes(30))) {
                // 只在尚未標記 delayed 時處理一次
                if (!"delayed".equals(f.getDelayStatus())) {
                    f.setDelayStatus("delayed");
                    // 生成 / 更新此航班的備援方案
                    recommendationService.generatePlans(f);
                    System.out.println("✈ 偵測到航班延誤 → flightId=" + f.getFlightId());
                }
            }
        }
    }
}
