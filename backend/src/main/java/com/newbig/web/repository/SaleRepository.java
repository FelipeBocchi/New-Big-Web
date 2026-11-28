package com.newbig.web.repository;

import com.newbig.web.entity.Sale;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sale from Sale sale where sale.id = :id")
    Optional<Sale> findByIdForUpdate(@Param("id") Long id);

    @Query("""
        select sale from Sale sale
        left join fetch sale.saleItems item
        left join fetch item.product
        where sale.id = :id
        """)
    Optional<Sale> findDetailsById(@Param("id") Long id);
}
