package com.example.flightrescue.model;

public class Plan {

    private Long flightId;
    private String planType; // flight / rail / hotel
    private int cost;
    private String arrivalTime;

    public Plan(Long flightId, String planType, int cost, String arrivalTime) {
        this.flightId = flightId;
        this.planType = planType;
        this.cost = cost;
        this.arrivalTime = arrivalTime;
    }

    public Long getFlightId() {
        return flightId;
    }

    public String getPlanType() {
        return planType;
    }

    public int getCost() {
        return cost;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }
}
