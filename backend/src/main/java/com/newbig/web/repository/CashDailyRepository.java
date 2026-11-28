package com.newbig.web.repository;

import com.newbig.web.entity.CashDailyLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashDailyRepository extends JpaRepository<CashDailyLog, Long> {
}
