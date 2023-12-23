package com.oguzhanbilgin.TradingApi.Repository;

import com.oguzhanbilgin.TradingApi.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
