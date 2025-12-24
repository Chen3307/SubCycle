package com.subcycle.repository;

import com.subcycle.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * 根據 email 或 name 搜尋使用者（分頁）
     * @param email 電子郵件關鍵字
     * @param name 姓名關鍵字
     * @param pageable 分頁參數
     * @return Page<User>
     */
    Page<User> findByEmailContainingOrNameContaining(String email, String name, Pageable pageable);
}
