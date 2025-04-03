package com.example.transactionaldemo.repository;

import com.example.transactionaldemo.entity.LogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository // 虽然 JpaRepository 默认会被扫描，显式标注更清晰
public interface LogRepository extends JpaRepository<LogEntry, Long> {
    // JpaRepository 提供了基础的 save(), findById(), findAll() 等方法
    // 我们这里不需要额外定义方法
}