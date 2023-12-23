package com.oguzhanbilgin.TradingApi.Entity;

import lombok.Data;

@Data
public class BuyRequest {

    private Long userId;
    private Long shareId;
    private Double quantity;
}
