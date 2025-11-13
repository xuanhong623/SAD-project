package com.example.flightrescue.database;

import com.example.flightrescue.model.Flight;
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
import java.time.LocalDateTime;

public class Init {

    private Firestore db;

    // 初始化資料庫
    public Firestore init() throws IOException {
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

        return db;
    }
    
    public static void main(String[] args){
        //Flight flight = new Flight(1L, "Taipei", "Tokyo"));
        Init init = new Init();
        Firestore db;
        try{
        db = init.init();
    
        Flight flight = new Flight(1L, "Taipei", "Tokyo", LocalDateTime.now());
        db.collection("Flights").document("flight001").set(flight);
    }
        catch(IOException e){
            e.printStackTrace();
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

