// com.example.flightrescue.config.PlanRankingProperties
package com.example.flightrescue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "plan.rank")
public class PlanRankingProperties {
    // 權重：總和不必需為 1，程式會照比例使用
    private double weightPrice = 0.5; // 價格（越低越好）
    private double weightRating = 0.3; // 評分（越高越好）
    private double weightDrive = 0.2; // 車程（越短越好）

    // getters / setters
    public double getWeightPrice() {
        return weightPrice;
    }

    public void setWeightPrice(double weightPrice) {
        this.weightPrice = weightPrice;
    }

    public double getWeightRating() {
        return weightRating;
    }

    public void setWeightRating(double weightRating) {
        this.weightRating = weightRating;
    }

    public double getWeightDrive() {
        return weightDrive;
    }

    public void setWeightDrive(double weightDrive) {
        this.weightDrive = weightDrive;
    }
}
