package com.oguzhanbilgin.TradingApi.Repository;

import com.oguzhanbilgin.TradingApi.Entity.Share;
import com.oguzhanbilgin.TradingApi.Entity.Transaction;
import com.oguzhanbilgin.TradingApi.enums.TransactionStatus;
import com.oguzhanbilgin.TradingApi.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByShareAndTransactionStatusAndTransactionType(Share share, TransactionStatus transactionStatus, TransactionType transactionType);


}

