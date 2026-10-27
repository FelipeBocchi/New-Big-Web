package com.newbig.web.service;

import com.newbig.web.controller.employee.dto.EmployeeRequest;
import com.newbig.web.entity.Employee;
import com.newbig.web.enums.EmployeeRole;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository repository;

    @Transactional
    public Employee save(EmployeeRequest request) {
        log.info("Criando novo funcionário. cpf={}", request.getCpf());

        if (repository.existsByCpf(request.getCpf())) {
            log.warn("Cadastro rejeitado: CPF já existente ({})", request.getCpf());
            throw new ConflictException("Já existe um cliente cadastrado com este CPF");
        }
        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Cadastro rejeitado: e-mail já existente ({})", request.getEmail());
            throw new ConflictException("Já existe um cliente cadastrado com este e-mail");
        }

        Employee employee = new Employee();
        updateFields(employee, request);

        Employee saved = repository.save(employee);
        log.info("Funcionário criado com sucesso. id={}", saved.getId());
        return saved;
    }

    public Employee findById( Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com o ID: " + id));
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    @Transactional
    public Employee update(EmployeeRequest request, Long id) {
        log.info("Atualizando funcionário. id={}", id);
        Employee employee = findById(id);

        if (!employee.getCpf().equals(request.getCpf()) && repository.existsByCpf(request.getCpf())) {
            throw new ConflictException("Já existe outro funcionário cadastrado com este CPF");
        }

        if (!employee.getEmail().equals(request.getEmail()) && repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Já existe outro funcionário cadastrado com este e-mail");
        }

        updateFields(employee, request);
        log.info("Funcionário atualizado com sucesso. id={}", id);
        return repository.save(employee);

    }

    @Transactional
    public Employee updatePartial(EmployeeRequest employeeRequest, Long id) {
        log.info("Atualizando funcionário. id={}", id);
        Employee employee = this.findById(id);

        if (employeeRequest.getName() != null) employee.setName(employeeRequest.getName());

        // Chamadas para os métodos extraídos reduzem a complexidade do método principal
        atualizarCpfSeNecessario(employee, employeeRequest.getCpf());

        if (employeeRequest.getPhone() != null) employee.setPhone(employeeRequest.getPhone());

        atualizarEmailSeNecessario(employee, employeeRequest.getEmail());

        if (employeeRequest.getUsername() != null) employee.setUsername(employeeRequest.getUsername());
        if (employeeRequest.getPassword() != null) employee.setPassword(employeeRequest.getPassword());
        if (employeeRequest.getActive() != null) employee.setActive(employeeRequest.getActive());
        if (employeeRequest.getAdmin() != null) employee.setAdmin(employeeRequest.getAdmin());
        if (employeeRequest.getBirthDate() != null) employee.setBirthDate(employeeRequest.getBirthDate());
        if (employeeRequest.getRole() != null) employee.setRole(employeeRequest.getRole());

        return this.repository.save(employee);
    }

    // --- MÉTODOS PRIVADOS PARA REDUZIR A COMPLEXIDADE ---

    private void atualizarCpfSeNecessario(Employee employee, String novoCpf) {
        if (novoCpf == null) return;

        if (!employee.getCpf().equals(novoCpf) && repository.existsByCpf(novoCpf)) {
            throw new ConflictException("Já existe outro funcionário cadastrado com este CPF");
        }
        employee.setCpf(novoCpf);
    }

    private void atualizarEmailSeNecessario(Employee employee, String novoEmail) {
        if (novoEmail == null) return;

        if (!employee.getEmail().equals(novoEmail) && repository.existsByEmail(novoEmail)) {
            throw new ConflictException("Já existe outro funcionário cadastrado com este e-mail");
        }
        employee.setEmail(novoEmail);
    }

    @Transactional
    public void toggle(Long id) {
        Employee employee = findById(id);

        if(employee.getActive()) {
            log.info("Inativando funcionário. id={}", id);
            employee.setActive(false);
        }else {
            log.info("Ativado funcionário. id={}", id);
            employee.setActive(true);
        }

        repository.save(employee);
    }

    //  Service para o end-point de listagem dos funcionários ativos e desativos
    public List<Employee> findByActive( Boolean active) {

        List<Employee> list = repository.findByActive(active);

        if (list.isEmpty()) throw new ResourceNotFoundException(  "Nenhum funcionário encontrado com active: " + active );

        return list;
    }

    private void updateFields(Employee employee, EmployeeRequest request) {
        employee.setName(request.getName());
        employee.setCpf(request.getCpf());
        employee.setPhone(request.getPhone());
        employee.setEmail(request.getEmail());
        employee.setUsername(request.getUsername());
        employee.setPassword(request.getPassword()); // Lembrete: Encriptar com BCrypt no futuro
        employee.setActive(request.getActive());
        employee.setRole(request.getRole());
        employee.setAdmin(request.getAdmin());
    }


    public List<Employee> findByRoleAndActive(EmployeeRole role, Boolean active) {

        return this.repository.findByRoleAndActive(role, active);

    }

}
