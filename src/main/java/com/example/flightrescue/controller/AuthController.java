package com.example.flightrescue.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.model.LoginRequest;
import com.example.flightrescue.model.User;
import com.example.flightrescue.storage.InMemoryData;

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

        Optional<User> userOpt = InMemoryData.users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();

        User user;
        if (userOpt.isPresent()) {
            // 已存在的使用者（可能已經填過航班資料，也可能還沒）
            user = userOpt.get();
        } else {
            // 新的使用者：一開始沒有任何航班/飯店資料
            user = new User(username);
            InMemoryData.users.add(user);
        }

        // 直接把 User 回傳（包含 profileCompleted 狀態）
        return ResponseEntity.ok(user);
    }
}
