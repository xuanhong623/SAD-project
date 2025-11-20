package com.example.flightrescue.model;

import java.time.LocalDateTime;

public class Flight {
    private String flightName;
    //flightName
    private Long flightId;
    private int newFlightId;
    private String fromCity;
    private String toCity;
    private LocalDateTime departTime;
    private String delayStatus = "normal"; // normal / delayed
    private boolean delayed = false;

    public Flight() {}

    public Flight(Long flightId, String flightName, String fromCity, String toCity, LocalDateTime departTime) {
        this.flightId = flightId;
        this.newFlightId = flightId.intValue();
        this.flightName = flightName;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.departTime = departTime;
    }

    public Long getFlightId() {
        return flightId;
    }

    public int getNewFlightId() {
        return newFlightId;
    }

    public String getFlightName() {
        return flightName;
    }

    public String getFromCity() {
        return fromCity;
    }

    public String getToCity() {
        return toCity;
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

    public void setDelayed(boolean delayed) {
        this.delayed = delayed;
    }
}
