package com.oguzhanbilgin.TradingApi.Service;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitRunner implements CommandLineRunner {

    private final InitService initService;

    public InitRunner(InitService initService) {
        this.initService = initService;
    }


    @Override
    public void run(String... args) throws Exception {
        initService.initializeData();
    }
}
