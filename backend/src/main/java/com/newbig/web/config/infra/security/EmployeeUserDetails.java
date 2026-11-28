package com.newbig.web.config.infra.security;

import com.newbig.web.entity.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;

public class EmployeeUserDetails extends User {

    private final Long employeeId;

    public EmployeeUserDetails(Employee employee) {
        super(
                employee.getEmail(),
                employee.getPassword(),
                Boolean.TRUE.equals(employee.getActive()),
                true,
                true,
                true,
                authorities(employee)
        );

        this.employeeId = employee.getId();
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    private static Collection<? extends GrantedAuthority> authorities(
            Employee employee
    ) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (Boolean.TRUE.equals(employee.getAdmin())) {
            authorities.add(
                    new SimpleGrantedAuthority("ROLE_ADMIN")
            );
        }

        if (employee.getRole() != null) {
            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + employee.getRole().name()
                    )
            );
        }

        return authorities;
    }
}