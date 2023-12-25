package com.oguzhanbilgin.TradingApi.Controller;

import com.oguzhanbilgin.TradingApi.Entity.BuyRequest;
import com.oguzhanbilgin.TradingApi.Service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TradeService tradeService;

    @Autowired
    public TransactionController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping("/buy")
    public ResponseEntity<String> buyTransaction(@RequestBody BuyRequest buyRequest) {
        try {
            tradeService.buyTransaction(buyRequest.getUserId(), buyRequest.getShareId(), buyRequest.getQuantity(), buyRequest.getPrice());
            return ResponseEntity.ok("Alım işlemi başarıyla gerçekleştirildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<String> sellTransaction(@RequestBody BuyRequest buyRequest) {
        try {
            tradeService.sellTransaction(buyRequest.getUserId(), buyRequest.getShareId(), buyRequest.getQuantity(), buyRequest.getPrice());
            return ResponseEntity.ok("Satış işlemi başarıyla gerçekleştirildi.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Hata: " + e.getMessage());
        }
    }
}

