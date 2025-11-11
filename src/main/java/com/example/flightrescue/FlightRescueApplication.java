package com.example.flightrescue;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class FlightRescueApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightRescueApplication.class, args);
    }

    @PostConstruct
    public void initMockData() {
        // 故意讓航班已經延誤 → 系統啟動後會自動觸發方案生成
        InMemoryData.flights.add(
                new Flight(1L, "TPE", "KIX", LocalDateTime.now().minusMinutes(40)));
        System.out.println("✅ 已載入航班假資料（flightId = 1）");
    }
}