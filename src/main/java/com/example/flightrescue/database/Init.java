package com.example.flightrescue.database;

import com.example.flightrescue.model.User;
import com.example.flightrescue.storage.InMemoryData;
import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.io.IOException;

public class Init {

    private Firestore db;

    // 初始化資料庫
    public void init() throws IOException {
        // 用 ClassLoader 從 resources 讀取
        InputStream serviceAccount = Init.class
                .getClassLoader()
                .getResourceAsStream("serviceAccountKey.json");


        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

        FirebaseApp.initializeApp(options);

        Firestore db = FirestoreClient.getFirestore();
        
        System.out.println("Firebase 成功初始化！");

        this.db = db;
    }
    
    public static void main(String[] args) {
        Init init = new Init();
        try {
            init.init();
            init.CreateUserData();
            init.ReadUserData();
            // init.CreateDataSample();
            // init.InputDataSample();
            // init.ReadDataSample();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void CreateUserData() throws Exception{
        // 使用者資料
                // InMemoryData.users.add(new User("demo001", "王小明", "大阪難波飯店", "大阪市中央區xxxx路", 1L));
                // InMemoryData.users.add(new User("demo002", "陳美玲", "東京新宿飯店", "東京都新宿區xxxx路", 2L));
                // InMemoryData.users.add(new User("demo003", "林志宏", "名古屋榮飯店", "名古屋市中區xxxx路", 3L));
                // InMemoryData.users.add(new User("demo004", "張雅惠", "福岡天神飯店", "福岡市中央區xxxx路", 4L));
                // InMemoryData.users.add(new User("demo005", "李建國", "札幌大通飯店", "札幌市中央區xxxx路", 5L));
                // InMemoryData.users.add(new User("demo006", "黃俊傑", "沖繩國際通飯店", "那霸市牧志xxxx路", 6L));
                // InMemoryData.users.add(new User("demo007", "周怡君", "京都四條飯店", "京都市中京區xxxx路", 7L));
                // InMemoryData.users.add(new User("demo008", "蔡宗翰", "橫濱海濱飯店", "橫濱市中區xxxx路", 8L));
                // InMemoryData.users.add(new User("demo009", "許淑芬", "神戶三宮飯店", "神戶市中央區xxxx路", 9L));
                // InMemoryData.users.add(new User("demo010", "吳宗賢", "金澤車站飯店", "金澤市此花町xxxx路", 10L));
                // // 新增一個沒有航班資料的帳號 demo999，登入後會被導向資料輸入頁
                // InMemoryData.users.add(new User("demo999"));
                // InMemoryData.users.add(new User("demo998"));
                // InMemoryData.users.add(new User("demo997"));
                // InMemoryData.users.add(new User("demo996"));
                // InMemoryData.users.add(new User("demo995"));

        // Firestore 自動將物件轉成文件欄位
        db.collection("users").document("demo001").set(new User("demo001", "王小明", "大阪難波飯店", "大阪市中央區xxxx路", 1L)).get();

        System.out.println("使用者資料建立成功！");

    }

    public void ReadUserData() throws Exception{
        DocumentSnapshot doc = db.collection("users").document("demo001").get().get();

        if (doc.exists()) {
            User u = doc.toObject(User.class);
            System.out.println("成功轉回物件");
        } else {
            System.out.println("❌ 找不到文件！");
        }
    }

    //下面是Sample

    public void CreateDataSample() throws Exception{
        
        // 🔹 建立一個 Map 當成要存入的資料
        Map<String, Object> player = new HashMap<>();
        player.put("name", "Henry");
        player.put("level", 5);
        player.put("coins", 1000);
        
        // 🔹 將資料存進 Firestore（集合名稱：players）
        ApiFuture<WriteResult> result = db.collection("players").document("player001").set(player);
        
        System.out.println("寫入成功，時間：" + result.get().getUpdateTime());
    }

    public void InputDataSample() throws Exception{

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

    public void ReadDataSample() throws Exception{

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

}

