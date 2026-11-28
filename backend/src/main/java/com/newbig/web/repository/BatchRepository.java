package com.newbig.web.repository;

import com.newbig.web.entity.Batch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
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
                AND b.expirationAt > :today
        ORDER BY b.expirationAt ASC, b.id ASC
        """)
    List<Batch> findAvailableBatchesFefo(
            @Param("productId") Long productId,
            @Param("today") LocalDate today
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Batch b where b.id in :ids order by b.expirationAt asc, b.id asc")
    List<Batch> findByIdsForUpdate(@Param("ids") Collection<Long> ids);
}
