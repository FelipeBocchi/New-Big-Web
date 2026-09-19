package com.new_big.web.controller.customer.dto;

import com.new_big.web.entity.Customer;

import java.time.LocalDate;

public record CustomerResponse(
        String name,
        String cpf,
        String phone,
        String email,
        Boolean active,
        LocalDate birthDate
) {
    public static CustomerResponse de(Customer customer) {
        return new CustomerResponse(
                customer.getName(),
                customer.getCpf(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getActive(),
                customer.getBirthDate()
        );
    }
}
