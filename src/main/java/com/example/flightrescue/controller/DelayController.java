package com.example.flightrescue.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flightrescue.service.DelayMonitorService;

@RestController
@RequestMapping("/dashboard")
public class DelayController {

    @Autowired
    private DelayMonitorService delayMonitorService;

    /**
     * 手動觸發一次「檢查所有航班是否延誤 + 生成方案」。
     * 前端更新按鈕會呼叫這個 API。
     */
    @PostMapping("/check-delay")
    public ResponseEntity<Void> checkDelayOnce() {
        delayMonitorService.checkFlightDelay();
        return ResponseEntity.ok().build();
    }
}
