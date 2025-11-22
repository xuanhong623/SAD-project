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

    /**
     * 讀取符合下列欄位順序的 CSV：
     * name,price,link,gmaps_rating,drive_minutes_from_haneda,gmaps_place_id
     */
    public List<Plan> loadFromCsv(String resourcePath, long flightId) throws Exception {
        List<Plan> plans = new ArrayList<>();

        ClassPathResource res = new ClassPathResource(resourcePath);
        if (!res.exists()) {
            System.out.println("[WARN] CSV 不在 classpath: " + resourcePath);
            return plans;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            // 跳過標題列
            br.readLine();

            int index = 0;
            while ((line = br.readLine()) != null) {
                // 僅在引號外切逗號
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length < 6)
                    continue;

                String name = unquote(parts[0]);
                String priceText = unquote(parts[1]);
                String link = unquote(parts[2]);
                double rating = safeDouble(parts[3]);
                int driveMinutes = safeInt(parts[4]);
                String placeId = unquote(parts[5]);

                int cost = parsePriceToInt(priceText);

                // 詳細文字（可供前端直接顯示）
                String detail = String.format(
                        "飯店：%s%n評分：%.1f%n距離羽田機場車程：%d 分鐘%n連結：%s%nPlace ID：%s",
                        name, rating, driveMinutes, link, placeId);

                // 方案代號：A..Z, AA..AZ, BA..（避免超過 26 筆出現非字母）
                String planType = "方案 " + alphaLabel(index++);

                // 搭配擴充後的 Plan 建構子（含 rating / driveMinutes）
                plans.add(new Plan(flightId, planType, cost, "", detail, rating, driveMinutes));
            }
        }

        System.out.println("[INFO] 從 CSV 載入真資料筆數: " + plans.size());
        return plans;
    }

    // 將 0,1,2.. 轉為 A, B, .., Z, AA, AB, ...
    private static String alphaLabel(int idx) {
        StringBuilder sb = new StringBuilder();
        idx += 1; // 轉為 1-based
        while (idx > 0) {
            int rem = (idx - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            idx = (idx - 1) / 26;
        }
        return sb.toString();
    }

    private static String unquote(String s) {
        return s == null ? "" : s.replaceAll("^\"|\"$", "").trim();
    }

    private static int parsePriceToInt(String s) {
        if (s == null)
            return 0;
        // 先將不換行空白（\u00A0）轉為一般空白，再移除非數字
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
