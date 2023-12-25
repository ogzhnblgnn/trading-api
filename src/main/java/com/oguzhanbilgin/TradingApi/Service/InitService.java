package com.oguzhanbilgin.TradingApi.Service;

import com.oguzhanbilgin.TradingApi.Entity.Portfolio;
import com.oguzhanbilgin.TradingApi.Entity.Share;
import com.oguzhanbilgin.TradingApi.Entity.User;
import com.oguzhanbilgin.TradingApi.Repository.PortfolioRepository;
import com.oguzhanbilgin.TradingApi.Repository.ShareRepository;
import com.oguzhanbilgin.TradingApi.Repository.TransactionRepository;
import com.oguzhanbilgin.TradingApi.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class InitService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final ShareRepository shareRepository;
    private final TransactionRepository transactionRepository;

    public InitService(UserRepository userRepository, PortfolioRepository portfolioRepository, ShareRepository shareRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.portfolioRepository = portfolioRepository;
        this.shareRepository = shareRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public void initializeData() {
        deleteAllData();
        addUserAndShares();
    }

    private void deleteAllData(){
        transactionRepository.deleteAll();
        shareRepository.deleteAll();
        portfolioRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void addUserAndShares() {
        User user1 = new User("oguzhan_bilgin", 1000.0);
        userRepository.save(user1);

        User user2 = new User("jeff_bezos", 1500.0);
        userRepository.save(user2);

        User user3 = new User("tim_cook", 1000.0);
        userRepository.save(user3);

        User user4 = new User("sundar_pichai", 1500.0);
        userRepository.save(user4);



        Portfolio portfolio1 = new Portfolio();
        portfolio1.setUser(user1);
        portfolioRepository.save(portfolio1);

        Portfolio portfolio2 = new Portfolio();
        portfolio2.setUser(user2);
        portfolioRepository.save(portfolio2);

        Portfolio portfolio3 = new Portfolio();
        portfolio3.setUser(user3);
        portfolioRepository.save(portfolio3);

        Portfolio portfolio4 = new Portfolio();
        portfolio4.setUser(user4);
        portfolioRepository.save(portfolio4);




        Share share1 = new Share("Christmas Inc.", "CMA", new BigDecimal(50.0), new Double(100));
        share1.setPortfolio(portfolio1);
        shareRepository.save(share1);

        Share share2 = new Share("This case study is amazing!", "CSA", new BigDecimal(75.0), new Double(150));
        share2.setPortfolio(portfolio2);
        shareRepository.save(share2);


        Share share3 = new Share("Eva.guru", "EVG", new BigDecimal(80.0), new Double(300));
        share3.setPortfolio(portfolio3);
        shareRepository.save(share3);

        Share share4 = new Share("BMW Inc.", "BMW", new BigDecimal(100.0), new Double(220));
        share4.setPortfolio(portfolio4);
        shareRepository.save(share4);
    }
}
