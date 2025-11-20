package com.example.flightrescue.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class PlanLoader {

    public List<Plan> loadFromCsv(String resourcePath, long flightId) throws Exception {
        List<Plan> plans = new ArrayList<>();

        ClassPathResource res = new ClassPathResource(resourcePath);
        if (!res.exists()) {
            System.out.println("[WARN] CSV 不在 classpath: " + resourcePath);
            return plans; // 空
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            br.readLine(); // 跳過標題列

            int index = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length < 6)
                    continue;

                String name = unquote(parts[0]);
                String priceText = unquote(parts[1]);
                String link = unquote(parts[2]);
                double rating = safeDouble(parts[3]);
                int driveMinutes = safeInt(parts[4]);

                int price = parsePriceToInt(priceText);

                String detail = String.format(
                        "飯店：%s%n評分：%.1f%n距離羽田機場車程：%d 分鐘%n連結：%s",
                        name, rating, driveMinutes, link);

                // 方案 A/B/C...
                String planType = "方案 " + (char) ('A' + index);
                index++;

                plans.add(new Plan(flightId, planType, price, "", detail));
            }
        }

        System.out.println("[INFO] 從 CSV 載入真資料筆數: " + plans.size());
        return plans;
    }

    private static String unquote(String s) {
        return s == null ? "" : s.replaceAll("^\"|\"$", "").trim();
    }

    private static int parsePriceToInt(String s) {
        if (s == null)
            return 0;
        String digits = s.replace('\u00A0', ' ').replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private static double safeDouble(String s) {
        try {
            return Double.parseDouble(s.trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static int safeInt(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
