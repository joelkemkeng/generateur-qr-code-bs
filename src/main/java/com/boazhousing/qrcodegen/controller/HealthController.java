package com.boazhousing.qrcodegen.controller;

import com.boazhousing.qrcodegen.model.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        try {
            Map<String, Object> healthData = new HashMap<>();
            healthData.put("status", "UP");
            healthData.put("service", "QR Code Generator API");
            healthData.put("version", "1.0.0");
            healthData.put("timestamp", LocalDateTime.now());
            healthData.put("description", "Boaz Housing QR Code Generator with custom styling");

            return ResponseEntity.ok(ApiResponse.success(healthData, "Service is healthy"));

        } catch (Exception e) {
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("status", "DOWN");
            errorData.put("error", e.getMessage());
            errorData.put("timestamp", LocalDateTime.now());

            return ResponseEntity.status(500)
                    .body(ApiResponse.error("Service health check failed", e.getMessage()));
        }
    }
}