package com.new_big.web.repository;

import com.new_big.web.entity.Batch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BatchRepository extends JpaRepository<Batch, Long> {

    Boolean existsByBatchCode(String batchCode);

    List<Batch> findByExpiration(Boolean expiration);

    // Query que será consultada na venda para fazer o FEFO
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT b FROM Batch b
        WHERE b.product.id = :productId
                AND b.quantityIsZero = false
                AND b.expiration = false
                AND b.quantity > 0
        ORDER BY b.expirationAt ASC, b.id ASC
        """)
    List<Batch> findAvailableBatchesFefo(
            @Param("productId") Long productId,
            @Param("today") LocalDate today
    );

    //  AND b.expirationAt > :today
}
