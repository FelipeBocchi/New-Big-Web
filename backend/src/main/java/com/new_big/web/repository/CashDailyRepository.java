package com.new_big.web.repository;

import com.new_big.web.entity.CashDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashDailyRepository extends JpaRepository<CashDailyLog, Long> {
}
