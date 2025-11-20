package com.example.flightrescue.model;

public class Plan {

    private Long flightId;
    private String planType; // ex: "方案 A" / "方案 B" / "方案 C"
    private int cost; // 總費用（航班 + 飯店的加總）
    private String arrivalTime;// 最終抵達飯店或目的地的時間說明
    private String detail; // 這個方案的細節說明（包含 / 飯店名稱/飯店評分/ 飯店網址）

    public Plan() {
    }

    public Plan(Long flightId, String planType, int cost, String arrivalTime, String detail) {
        this.flightId = flightId;
        this.planType = planType;
        this.cost = cost;
        this.arrivalTime = arrivalTime;
        this.detail = detail;
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

    public String getDetail() {
        return detail;
    }
}
