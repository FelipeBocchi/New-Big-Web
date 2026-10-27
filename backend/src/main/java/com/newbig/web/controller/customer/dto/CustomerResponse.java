package com.newbig.web.controller.customer.dto;

import com.newbig.web.entity.Customer;

import java.time.LocalDate;

public record CustomerResponse(
        Long id, //vai ser removido
        String name,
        String cpf,
        String phone,
        String email,
        Boolean active,
        LocalDate birthDate
) {
    public static CustomerResponse de(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getCpf(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getActive(),
                customer.getBirthDate()
        );
    }
}
