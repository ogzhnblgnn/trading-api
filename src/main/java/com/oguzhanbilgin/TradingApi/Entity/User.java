package com.oguzhanbilgin.TradingApi.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "balance")
    private Double balance;

    @OneToOne(mappedBy = "user")
    private Portfolio portfolio;

    public User(String username, Double balance) {
        this.username = username;
        this.balance = balance;

    }

    public User() {

    }
}

