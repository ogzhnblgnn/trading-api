package com.oguzhanbilgin.TradingApi.Repository;

import com.oguzhanbilgin.TradingApi.Entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
//    @Transactional
//    @Modifying
//    @Query("UPDATE Portfolio p " +
//            "SET" +
//            " p.purchasedShares = :purchasedShare," +
//            " p.balance = :balance"+
//            " WHERE p.id = :portfolioId"
//    )
//    void updateField(
//            @Param("portfolioId") Long portfolioId,
//            @Param("purchasedShare") PurchasedShare purchasedShare,
//            @Param("balance") Double balance
//    );
}
