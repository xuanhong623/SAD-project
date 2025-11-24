package com.example.flightrescue;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.example.flightrescue.database.DataBase;
import com.example.flightrescue.model.Flight;
import com.example.flightrescue.storage.InMemoryData;

import jakarta.annotation.PostConstruct;

@EnableScheduling
@SpringBootApplication
public class FlightRescueApplication {

        public static void main(String[] args) {
                SpringApplication.run(FlightRescueApplication.class, args);
        }

        @PostConstruct
        public void initMockData() {

                try {
                        DataBase db = new DataBase();
                        db.Init();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                // 航班資料
                // 桃園（TPE） → 羽田（HND）的酷航航班（示例 5 筆）

                InMemoryData.flights.add(
                                new Flight(1L, "TR892", "TPE", "HND", LocalDateTime.now().minusMinutes(30))); // 一開始就延誤

                InMemoryData.flights.add(
                                new Flight(2L, "TR894", "TPE", "HND", LocalDateTime.now().minusMinutes(5))); // 一開始延誤

                InMemoryData.flights.add(
                                new Flight(3L, "TR896", "TPE", "HND", LocalDateTime.now().plusMinutes(45))); // 5 秒後延誤

                InMemoryData.flights.add(
                                new Flight(4L, "TR898", "TPE", "HND", LocalDateTime.now().plusHours(1))); // 10 秒後延誤

                InMemoryData.flights.add(
                                new Flight(5L, "TR880", "TPE", "HND", LocalDateTime.now().plusHours(2))); // 不延誤

                // 使用者資料
                // InMemoryData.users.add(new User("demo001", "王小明", "大阪難波飯店", "大阪市中央區xxxx路",
                // 1L));
                // InMemoryData.users.add(new User("demo002", "陳美玲", "東京新宿飯店", "東京都新宿區xxxx路",
                // 2L));
                // InMemoryData.users.add(new User("demo003", "林志宏", "名古屋榮飯店", "名古屋市中區xxxx路",
                // 3L));
                // InMemoryData.users.add(new User("demo004", "張雅惠", "福岡天神飯店", "福岡市中央區xxxx路",
                // 4L));
                // InMemoryData.users.add(new User("demo005", "李建國", "札幌大通飯店", "札幌市中央區xxxx路",
                // 5L));
                // InMemoryData.users.add(new User("demo006", "黃俊傑", "沖繩國際通飯店", "那霸市牧志xxxx路",
                // 6L));
                // InMemoryData.users.add(new User("demo007", "周怡君", "京都四條飯店", "京都市中京區xxxx路",
                // 7L));
                // InMemoryData.users.add(new User("demo008", "蔡宗翰", "橫濱海濱飯店", "橫濱市中區xxxx路",
                // 8L));
                // InMemoryData.users.add(new User("demo009", "許淑芬", "神戶三宮飯店", "神戶市中央區xxxx路",
                // 9L));
                // InMemoryData.users.add(new User("demo010", "吳宗賢", "金澤車站飯店", "金澤市此花町xxxx路",
                // 10L));
                // // 新增一個沒有航班資料的帳號 demo999，登入後會被導向資料輸入頁
                // InMemoryData.users.add(new User("demo999"));
                // InMemoryData.users.add(new User("demo998"));
                // InMemoryData.users.add(new User("demo997"));
                // InMemoryData.users.add(new User("demo996"));
                // InMemoryData.users.add(new User("demo995"));

                System.out.println("✅ 已載入 5 筆航班 + 10 筆使用者假資料");

                // === 模擬假延誤邏輯 ===
                ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

                // 一開始就延誤的航班（1、4）
                InMemoryData.flights.get(0).setDelayed(true);
                InMemoryData.flights.get(3).setDelayed(true);

                // 3 秒後延誤的航班（2）
                scheduler.schedule(() -> {
                        InMemoryData.flights.get(1).setDelayed(true);
                        System.out.println("✈️ Flight 2 (TPE-NRT) 已模擬延誤！");
                }, 3, TimeUnit.SECONDS);

                // 10 秒後延誤的航班（3）
                scheduler.schedule(() -> {
                        InMemoryData.flights.get(2).setDelayed(true);
                        System.out.println("✈️ Flight 3 (TSA-HND) 已模擬延誤！");
                }, 10, TimeUnit.SECONDS);

                // 5 不會延誤，不設定
        }

}