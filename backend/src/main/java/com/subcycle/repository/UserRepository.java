package com.subcycle.repository;

import com.subcycle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 使用者資料訪問層
 * 繼承 JpaRepository 自動提供 CRUD 方法
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據 email 查詢使用者
     * @param email 電子郵件
     * @return Optional<User>
     */
    Optional<User> findByEmail(String email);

    /**
     * 檢查 email 是否已存在
     * @param email 電子郵件
     * @return boolean
     */
    boolean existsByEmail(String email);
}
