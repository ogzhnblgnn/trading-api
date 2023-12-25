package com.oguzhanbilgin.TradingApi.Service;

import com.oguzhanbilgin.TradingApi.Entity.Share;
import com.oguzhanbilgin.TradingApi.Repository.ShareRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@EnableScheduling
public class ShareService {

    private final ShareRepository shareRepository;

    public ShareService(ShareRepository shareRepository) {
        this.shareRepository = shareRepository;
    }

    // Her saat başında çalışacak
    @Scheduled(cron = "0 0 * * * *")
    public void updateSharePrices() {
        //TÜm hisselerin fiyatlarını saat başı random ##.## formatında güncelle
        List<Share> shares = shareRepository.findAll();

        for (Share share : shares) {
            BigDecimal newPrice = generateRandomPriceBigDecimal();
            share.setPrice(newPrice);
        }
        shareRepository.saveAll(shares);
    }

    private BigDecimal generateRandomPriceBigDecimal() {
        double randomPrice = Math.random() * (200 - 50) + 50;
        return BigDecimal.valueOf(randomPrice).setScale(2, BigDecimal.ROUND_HALF_UP); // ##.## formatında random bir BigDecimal üretiyoruz.
    }
}