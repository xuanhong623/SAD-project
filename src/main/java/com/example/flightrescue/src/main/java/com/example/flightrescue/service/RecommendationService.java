package com.example.flightrescue.service;

import org.springframework.stereotype.Service;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.Plan;
import com.example.flightrescue.storage.InMemoryData;

@Service
public class RecommendationService {

    public void generatePlans(Flight f) {
        InMemoryData.plans.removeIf(p -> p.getFlightId().equals(f.getFlightId()));

        InMemoryData.plans.add(new Plan(f.getFlightId(), "flight", 7000, "16:20"));
        InMemoryData.plans.add(new Plan(f.getFlightId(), "rail", 1500, "14:50"));
        InMemoryData.plans.add(new Plan(f.getFlightId(), "hotel", 4000, "明天 10:00"));

        System.out.println("✅ 已生成備援方案 (flightId=" + f.getFlightId() + ")");
    }
}
