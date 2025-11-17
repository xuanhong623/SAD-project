package com.example.flightrescue.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.model.Plan;
import com.example.flightrescue.service.RecommendationService;
import com.example.flightrescue.storage.InMemoryData;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private RecommendationService recommendationService; // 目前沒在這裡呼叫，但保留注入

    @GetMapping("/plans/{flightId}")
    public List<Plan> getPlans(@PathVariable Long flightId) {
        // 回傳「該航班的所有組合方案」
        return InMemoryData.plans.stream()
                .filter(p -> p.getFlightId().equals(flightId))
                .collect(Collectors.toList());
    }

}
