package com.example.flightrescue.model;

import java.time.LocalDateTime;

public class Flight {

    private Long flightId;
    private String fromCity;
    private String toCity;
    private LocalDateTime departTime;
    private String delayStatus = "normal"; // normal / delayed

    public Flight(Long flightId, String fromCity, String toCity, LocalDateTime departTime) {
        this.flightId = flightId;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departTime = departTime;
    }

    public Long getFlightId() {
        return flightId;
    }

    public LocalDateTime getDepartTime() {
        return departTime;
    }

    public String getDelayStatus() {
        return delayStatus;
    }

    public void setDelayStatus(String delayStatus) {
        this.delayStatus = delayStatus;
    }
}
