package com.newbig.web.repository;

import com.newbig.web.entity.Employee;
import com.newbig.web.enums.EmployeeRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    List<Employee> findByActive(Boolean active);

    // essa consulta vai ser acessada pelo login
    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e WHERE e.role =: role AND e.active =: active")
    List<Employee> findByRoleAndActive(
            @Param("role")EmployeeRole role,
            @Param("active") Boolean active
            );

}
