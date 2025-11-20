package com.example.flightrescue.model;

public class User {
    private String username;
    private String fullName;
    private String hotelName;
    private String hotelAddress;
    private Long flightId;
    private int newFlightId;

    // 新增：是否已填完航班 / 飯店資料
    private boolean profileCompleted;

    public User() {
    }

    // 原本的完整建構子，可以順便把 profileCompleted 設為 true
    public User(String username, String fullName, String hotelName, String hotelAddress, Long flightId) {
        this.username = username;
        this.fullName = fullName;
        this.hotelName = hotelName;
        this.hotelAddress = hotelAddress;
        this.flightId = flightId;
        this.newFlightId = flightId.intValue();
        // 只要建構子給了完整資料，就當作已完成設定
        this.profileCompleted = (fullName != null && hotelName != null && hotelAddress != null && flightId != null);
    }

    // 新增：給「新帳號、尚未填資料」用的建構子
    public User(String username) {
        this.username = username;
        this.profileCompleted = false;
    }

    // === getter / setter ===

    public String getUsername() {
        return username;
    }

    public int getNewFlightId() {
        return newFlightId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelAddress() {
        return hotelAddress;
    }

    public void setHotelAddress(String hotelAddress) {
        this.hotelAddress = hotelAddress;
    }

    public Long getFlightId() {
        return flightId;
    }

    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }
}
