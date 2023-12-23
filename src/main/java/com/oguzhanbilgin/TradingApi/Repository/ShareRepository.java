package com.oguzhanbilgin.TradingApi.Repository;

import com.oguzhanbilgin.TradingApi.Entity.Portfolio;
import com.oguzhanbilgin.TradingApi.Entity.Share;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShareRepository extends JpaRepository<Share, Long> {
    Share findByPortfolioAndSymbol(Portfolio portfolio, String symbol);
}
