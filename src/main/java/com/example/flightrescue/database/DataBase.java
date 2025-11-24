package com.example.flightrescue.database;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.example.flightrescue.model.Flight;
import com.example.flightrescue.model.User;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

public class DataBase {

        public static Firestore db;

        // 簡介
        // 要使用這個DataBase一定要初始化，真的
        // 目前都只有使用者資料庫
        // 要Flight的話，我再弄，找我就行
        // 嘿嘿嘿ㄏㄟ ，我要寫到中風了

        // 初始化資料庫，只要執行一次，但一定要
        public void Init() throws IOException {
                // 用 ClassLoader 從 resources 讀取
                InputStream serviceAccount = DataBase.class
                                .getClassLoader()
                                .getResourceAsStream("serviceAccountKey.json");

                FirebaseOptions options = FirebaseOptions.builder()
                                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                                .build();

                FirebaseApp.initializeApp(options);

                Firestore db = FirestoreClient.getFirestore();

                System.out.println("Firebase 成功初始化！");

                DataBase.db = db;
        }

        // 讀取使用者資料庫：就是String你要的使用者名稱，就會回傳一個User物件
        public User ReadUserData(String user /* 使用者的username */, Firestore db) throws Exception {
                DocumentSnapshot doc = db.collection("users").document(user).get().get();

                if (doc.exists()) {
                        User u = doc.toObject(User.class);
                        System.out.println("成功轉回使用者物件：" + u.getUsername());
                        return u;
                } else {
                        System.out.println("找不到使用者！");
                        return null;
                }
        }

        // 新增使用者資料：顧名思義，輸入一個User物件進去就會新增
        // 如果有同樣username的話會覆蓋，不用擔心
        public void InputUserData(User user, Firestore db) throws Exception {
                db.collection("users").document(user.getUsername()).set(user).get();
                System.out.println("使用者資料輸入成功！");
        }

        public Flight ReadFlightData(int newFlightId, Firestore db) throws Exception {
                DocumentSnapshot doc = db.collection("flights").document(String.valueOf(newFlightId)).get().get();

                if (doc.exists()) {
                        Flight f = doc.toObject(Flight.class);
                        System.out.println("成功轉回航班物件：" + f.getFlightId());
                        return f;
                } else {
                        System.out.println("找不到航班！");
                        return null;
                }
        }

        public void InputFlightData(Flight flight, Firestore db) throws Exception {
                db.collection("flights").document(flight.getFlightId().toString()).set(flight).get();
                System.out.println("航班資料輸入成功！");
        }

        // 建立使用者資料庫
        // 不要用這個，這個我已經建立過一次了，所以不要用
        public void CreateUserData() throws Exception {
                // Firestore 自動將物件轉成文件欄位
                db.collection("users").document("demo001").set(
                                new User("demo001", "王小明", "東京新宿商務飯店", "東京都新宿區西新宿1-3-5", 1L)).get();

                db.collection("users").document("demo002").set(
                                new User("demo002", "陳美玲", "東京銀座精品飯店", "東京都中央區銀座2-4-8", 2L)).get();

                // 羽田機場附近
                db.collection("users").document("demo003").set(
                                new User("demo003", "林志宏", "羽田機場國際航站飯店", "東京都大田區羽田空港2-6-5", 3L)).get();

                db.collection("users").document("demo004").set(
                                new User("demo004", "張雅惠", "東京池袋城市飯店", "東京都豐島區東池袋3-8-5", 4L)).get();

                db.collection("users").document("demo005").set(
                                new User("demo005", "李建國", "東京上野精選飯店", "東京都台東區上野7-2-6", 5L)).get();

                // 羽田機場附近
                db.collection("users").document("demo006").set(
                                new User("demo006", "王小明", "羽田機場快捷飯店", "東京都大田區羽田1-2-1", 6L)).get();

                db.collection("users").document("demo007").set(
                                new User("demo007", "陳美玲", "東京澀谷站前飯店", "東京都澀谷區道玄坂1-12-9", 7L)).get();

                // 羽田機場附近（第三筆）
                db.collection("users").document("demo008").set(
                                new User("demo008", "林志宏", "羽田天空之橋飯店", "東京都大田區羽田5-18-1", 8L)).get();

                db.collection("users").document("demo009").set(
                                new User("demo009", "張雅惠", "東京六本木設計飯店", "東京都港區六本木5-16-3", 9L)).get();

                db.collection("users").document("demo010").set(
                                new User("demo010", "李建國", "東京丸之內車站飯店", "東京都千代田區丸之內1-7-12", 10L)).get();

                // 新增一個沒有航班資料的帳號 demo999，登入後會被導向資料輸入頁
                db.collection("users").document("demo999").set(new User("demo999")).get();
                db.collection("users").document("demo998").set(new User("demo998")).get();
                db.collection("users").document("demo997").set(new User("demo997")).get();
                db.collection("users").document("demo996").set(new User("demo996")).get();
                db.collection("users").document("demo995").set(new User("demo995")).get();

                System.out.println("使用者資料庫建立成功！");
        }

        public void CreateFlightData() throws Exception {
                // Firestore 自動將物件轉成文件欄位
                // 桃園（TPE） → 羽田（HND）的酷航航班（5 筆）

                db.collection("flights").document("1").set(
                                new Flight(1L, "TR892", "HND", "TPE", null)).get();

                db.collection("flights").document("2").set(
                                new Flight(2L, "TR894", "HND", "TPE", null)).get();

                db.collection("flights").document("3").set(
                                new Flight(3L, "TR896", "HND", "TPE", null)).get();

                db.collection("flights").document("4").set(
                                new Flight(4L, "TR898", "HND", "TPE", null)).get();

                db.collection("flights").document("5").set(
                                new Flight(5L, "TR880", "HND", "TPE", null)).get();

                System.out.println("航班資料庫建立成功！");
        }

        // 下面是Sample，你們不用管這些，我給我自己看得

        public void CreateDataSample() throws Exception {

                // 🔹 建立一個 Map 當成要存入的資料
                Map<String, Object> player = new HashMap<>();
                player.put("name", "Henry");
                player.put("level", 5);
                player.put("coins", 1000);

                // 🔹 將資料存進 Firestore（集合名稱：players）
                ApiFuture<WriteResult> result = db.collection("players").document("player001").set(player);

                System.out.println("寫入成功，時間：" + result.get().getUpdateTime());
        }

        public void InputDataSample() throws Exception {

                // 🔹 建立一筆新的玩家資料
                Map<String, Object> player = new HashMap<>();
                player.put("name", "Alice");
                player.put("level", 10);
                player.put("coins", 2500);

                // 🔹 新增到已存在的集合 players
                ApiFuture<WriteResult> result = db.collection("players")
                                .document("player002") // 文件 ID（可以自己指定或讓系統自動產生）
                                .set(player);

                System.out.println("新增成功：" + result.get().getUpdateTime());
        }

        public void ReadDataSample() throws Exception {

                DocumentReference docRef = db.collection("players").document("player001");
                DocumentSnapshot document = docRef.get().get();

                if (document.exists()) {
                        // 🔹 讀取到的資料是 Map<String, Object>
                        Map<String, Object> data = document.getData();
                        System.out.println("玩家資料：" + data);

                        // 你也可以分別取欄位
                        System.out.println("名字：" + document.getString("name"));
                        System.out.println("等級：" + document.getLong("level"));
                        System.out.println("金幣：" + document.getLong("coins"));
                } else {
                        System.out.println("❌ 找不到文件！");
                }
        }

        // 建立資料使用，後續不要理這段
        // public static void main(String[] args) {
        // DataBase db = new DataBase();
        // try {
        // db.Init();
        // db.CreateFlightData();
        // db.CreateUserData();
        // // init.CreateDataSample();
        // // init.InputDataSample();
        // // init.ReadDataSample();
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // }

        // 就說不用看，看甚麼，給我上去
}
