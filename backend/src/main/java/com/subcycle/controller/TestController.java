package com.subcycle.controller;

import com.subcycle.entity.User;
import com.subcycle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 測試用 Controller
 * 用於驗證資料庫連線和基本功能
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(originPatterns = "*")
public class TestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private UserRepository userRepository;

    /**
     * 測試 API 是否正常運作
     * GET http://localhost:8080/api/test
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "SubCycle Backend API 正常運作！");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 測試資料庫連線
     * GET http://localhost:8080/api/test/db
     */
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "success");
            response.put("message", "資料庫連線成功！");
            response.put("database", connection.getCatalog());
            response.put("url", connection.getMetaData().getURL());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "資料庫連線失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 測試查詢所有使用者
     * GET http://localhost:8080/api/test/users
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<User> users = userRepository.findAll();
            response.put("status", "success");
            response.put("message", "查詢成功");
            response.put("count", users.size());
            response.put("data", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "查詢失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 測試根據 email 查詢使用者
     * GET http://localhost:8080/api/test/user?email=demo@subcycle.com
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                response.put("status", "success");
                response.put("message", "找到使用者");
                response.put("data", user);
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "not_found");
                response.put("message", "找不到使用者: " + email);
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "查詢失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 測試統計資料
     * GET http://localhost:8080/api/test/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> response = new HashMap<>();

        try {
            long userCount = userRepository.count();

            response.put("status", "success");
            response.put("message", "統計資料");
            response.put("totalUsers", userCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "查詢失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
