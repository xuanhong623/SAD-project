package com.example.flightrescue.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

import org.springframework.web.bind.annotation.RequestParam;

import com.example.flightrescue.database.DataBase;

@RestController
@RequestMapping("/dashboard")
public class FlightController {

    @GetMapping("/flights")
    public List<Flight> getFlights() {
        // 直接回傳記憶體中的 5 筆假資料
        return InMemoryData.flights;
    }

    @GetMapping("/user-flight-name")
    public ResponseEntity<Flight> getUserFlight(@RequestParam int newFlightId) {
        DataBase db = new DataBase();
        try{
            Flight flight = db.ReadFlightData(newFlightId, DataBase.db);
            if(flight!=null){
                return ResponseEntity.ok(flight);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }
}
