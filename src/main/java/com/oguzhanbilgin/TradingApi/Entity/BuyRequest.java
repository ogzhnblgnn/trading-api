package com.oguzhanbilgin.TradingApi.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuyRequest {

    private Long userId;
    private Long shareId;
    private Double quantity;
    private BigDecimal price;
}
