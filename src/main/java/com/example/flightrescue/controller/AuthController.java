package com.example.flightrescue.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.database.DataBase;
import com.example.flightrescue.model.LoginRequest;
import com.example.flightrescue.model.User;

@RestController
@RequestMapping("/api")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest loginRequest) {

        // 仍然固定 demo 密碼為 1234
        if (!"1234".equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = loginRequest.getUsername();

        DataBase db = new DataBase();
        User user;
        try{
            user = db.ReadUserData(username, DataBase.db);
            if(user==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            } 
            return ResponseEntity.ok(user);
        }catch(Exception e){
            e.printStackTrace();
        }
        // 直接把 User 回傳（包含 profileCompleted 狀態）
        return null;
    }
}
