package com.subcycle.config;

import com.subcycle.entity.User;
import com.subcycle.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 將資料庫中尚未加密的密碼自動轉換為 bcrypt。
 * 方便使用現有的種子資料（純文字密碼）登入，而不需手動重新匯入資料。
 */
@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PasswordMigrationRunner.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        for (User user : userRepository.findAll()) {
            String password = user.getPassword();

            // 略過已經是 bcrypt 的密碼（$2 開頭）
            if (password == null || password.startsWith("$2")) {
                continue;
            }

            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            log.info("Encoded plaintext password for user {}", user.getEmail());
        }
    }
}
