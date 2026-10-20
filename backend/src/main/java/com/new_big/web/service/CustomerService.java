package com.new_big.web.service;

import com.new_big.web.controller.customer.dto.CustomerRequest;
import com.new_big.web.controller.customer.dto.CustomerResponse;
import com.new_big.web.entity.Customer;
import com.new_big.web.entity.Employee;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.management.RuntimeErrorException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    @Transactional
    public Customer save(CustomerRequest request) {
        log.info("Criando novo cliente. cpf={}", request.getCpf());

        if (repository.existsByCpf(request.getCpf())) {
            log.warn("Cadastro rejeitado: CPF já existente ({})", request.getCpf());
            throw new ConflictException("Já existe um cliente cadastrado com este CPF");
        }
        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Cadastro rejeitado: e-mail já existente ({})", request.getEmail());
            throw new ConflictException("Já existe um cliente cadastrado com este e-mail");
        }

        Customer customer = new Customer();
        updateCustomerFields(customer, request);

        Customer saved = repository.save(customer);
        log.info("Cliente criado com sucesso. id={}", saved.getId());
        return saved;

    }

    public Customer findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado com o ID: " + id));
    }

    public List<Customer> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Customer update(Long id, CustomerRequest request) {
        log.info("Atualizando cliente. id={}", id);
        Customer customer = findById(id);

        if (!customer.getCpf().equals(request.getCpf()) && repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe outro cliente cadastrado com este CPF");
        }

        if (!customer.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Já existe outro cliente cadastrado com este e-mail");
        }

        updateCustomerFields(customer, request);

        log.info("Cliente atualizado com sucesso. id={}", id);
        return repository.save(customer);
    }

    @Transactional
    public void inactivate(Long id) {

        Customer customer = findById(id);

        if(customer.getActive()) {
            log.info("Inativando cliente. id={}", id);
            customer.setActive(false);
        }else {
            log.info("Ativado cliente. id={}", id);
            customer.setActive(true);
        }

        repository.save(customer);
    }

    //  Service para o end-point de listagem dos clientes ativos e desativos
    public List<Customer> findByActive(Boolean active) {

        List<Customer> list = repository.findByActive(active);

        if (list.isEmpty()) throw new ResourceNotFoundException(  "Nenhum Cliente encontrado com active: " + active );

        return list;
    }

    // Método para evitar repetição de código
    private void updateCustomerFields(Customer customer, CustomerRequest request) {
        customer.setName(request.getName());
        customer.setCpf(request.getCpf());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setActive(request.getActive());
        customer.setBirthDate(request.getBirthDate());
    }

}
