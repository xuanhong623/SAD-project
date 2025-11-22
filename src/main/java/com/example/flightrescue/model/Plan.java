package com.example.flightrescue.model;

public class Plan {

    private Long flightId;
    private String planType; // ex: "方案 A" / "方案 B" / "方案 C"
    private int cost; // 總費用（航班 + 飯店的加總）
    private String arrivalTime; // 最終抵達飯店或目的地的時間說明
    private String detail; // 方案細節（飯店名稱、評分、網址等）

    // 以下為擴充欄位，供排序與權重演算法使用
    private Double rating; // 飯店評分（越高越好）
    private Integer driveMinutes; // 距離機場車程（越短越好）
    private Double score; // 依權重計算出的總分（越高越好）

    public Plan() {
    }

    // 主要建構子（一般假資料使用）
    public Plan(Long flightId, String planType, int cost, String arrivalTime, String detail) {
        this(flightId, planType, cost, arrivalTime, detail, null, null);
    }

    // 擴充建構子（CSV 載入時使用，可同時設定 rating 與 driveMinutes）
    public Plan(Long flightId, String planType, int cost, String arrivalTime, String detail,
            Double rating, Integer driveMinutes) {
        this.flightId = flightId;
        this.planType = planType;
        this.cost = cost;
        this.arrivalTime = arrivalTime;
        this.detail = detail;
        this.rating = rating;
        this.driveMinutes = driveMinutes;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getDriveMinutes() {
        return driveMinutes;
    }

    public void setDriveMinutes(Integer driveMinutes) {
        this.driveMinutes = driveMinutes;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Plan{" +
                "flightId=" + flightId +
                ", planType='" + planType + '\'' +
                ", cost=" + cost +
                ", arrivalTime='" + arrivalTime + '\'' +
                ", rating=" + rating +
                ", driveMinutes=" + driveMinutes +
                ", score=" + score +
                '}';
    }
}
