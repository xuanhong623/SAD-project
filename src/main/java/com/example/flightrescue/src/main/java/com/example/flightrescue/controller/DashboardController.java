package com.example.flightrescue.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.model.Plan;
import com.example.flightrescue.storage.InMemoryData;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/plans/{flightId}")
    public List<Plan> getPlans(@PathVariable Long flightId) {
        return InMemoryData.plans.stream()
                .filter(p -> p.getFlightId().equals(flightId))
                .collect(Collectors.toList());
    }
}
