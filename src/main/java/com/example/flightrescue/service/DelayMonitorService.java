package com.example.flightrescue.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class DelayMonitorService {

    @Autowired
    RecommendationService recommendationService;

    @Scheduled(fixedRate = 10000) // 每 10 秒檢查一次
    public void checkFlightDelay() {
        for (Flight f : InMemoryData.flights) {
            if (LocalDateTime.now().isAfter(f.getDepartTime().plusMinutes(30))) {
                if (!f.getDelayStatus().equals("delayed")) {
                    f.setDelayStatus("delayed");
                    recommendationService.generatePlans(f);
                    System.out.println("✈ 偵測到航班延誤 → flightId=" + f.getFlightId());
                }
            }
        }
    }
}
