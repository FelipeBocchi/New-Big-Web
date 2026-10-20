package com.new_big.web.repository;

import com.new_big.web.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCpf(String cpf);

    Optional<Customer> findByEmail(String email);

    List<Customer> findByActive(Boolean active);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
}
