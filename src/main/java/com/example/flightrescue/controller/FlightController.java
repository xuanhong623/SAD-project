package com.example.flightrescue.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

@RestController
@RequestMapping("/dashboard")
public class FlightController {

    @GetMapping("/flights")
    public List<Flight> getFlights() {
        // 直接回傳記憶體中的 5 筆假資料
        return InMemoryData.flights;
    }
}
