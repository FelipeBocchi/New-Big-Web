package com.new_big.web.repository;

import com.new_big.web.entity.Batch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Boolean existsByBatchCode(String batchCode);

    List<Batch> findByExpiration(Boolean expiration);
}
