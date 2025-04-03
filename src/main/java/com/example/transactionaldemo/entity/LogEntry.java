package com.example.transactionaldemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor // Lombok: 生成无参构造函数 (JPA需要)
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 使用数据库自增ID
    private Long id;

    private String message;

    // Lombok: 提供了一个带参数的构造函数方便创建
    public LogEntry(String message) {
        this.message = message;
    }

    // Lombok 会自动生成 getId(), setId(), getMessage(), setMessage()
}