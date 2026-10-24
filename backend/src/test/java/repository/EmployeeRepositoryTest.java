package com.new_big.web.repository;

import com.new_big.web.entity.Employee;
import com.new_big.web.enums.EmployeeRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve retornar verdadeiro ao verificar existência de funcionário por CPF já cadastrado")
    void deveVerificarSeExistePorCpfComSucesso() {
        // Arrange (Dado)
        Employee employee = new Employee();
        employee.setName("Lucas Mendes");
        employee.setCpf("12345678901");
        employee.setEmail("lucas@email.com");
        employee.setUsername("lucas.mendes");
        employee.setPassword("123456");
        employee.setActive(true);
        employee.setAdmin(false);

        entityManager.persistAndFlush(employee);

        // Act (Quando)
        boolean exists = employeeRepository.existsByCpf("12345678901");

        // Assert (Então)
        assertTrue(exists);
    }

    @Test
    @DisplayName("Deve retornar verdadeiro ao verificar existência de funcionário por e-mail já cadastrado")
    void deveVerificarSeExistePorEmailComSucesso() {
        // Arrange (Dado)
        Employee employee = new Employee();
        employee.setName("Juliana Costa");
        employee.setCpf("98765432100");
        employee.setEmail("juliana@email.com");
        employee.setUsername("juliana.costa");
        employee.setPassword("123456");
        employee.setActive(true);
        employee.setAdmin(false);

        entityManager.persistAndFlush(employee);

        // Act (Quando)
        boolean exists = employeeRepository.existsByEmail("juliana@email.com");

        // Assert (Então)
        assertTrue(exists);
    }

    @Test
    @DisplayName("Deve listar funcionários filtrando pelo status ativo")
    void deveListarPorStatusAtivo() {
        // Arrange (Dado)
        Employee activeEmployee = new Employee();
        activeEmployee.setName("Ativo");
        activeEmployee.setCpf("11111111111");
        activeEmployee.setEmail("ativo@email.com");
        activeEmployee.setUsername("ativo");
        activeEmployee.setPassword("123456");
        activeEmployee.setActive(true);
        activeEmployee.setAdmin(false);

        Employee inactiveEmployee = new Employee();
        inactiveEmployee.setName("Inativo");
        inactiveEmployee.setCpf("22222222222");
        inactiveEmployee.setEmail("inativo@email.com");
        inactiveEmployee.setUsername("inativo");
        inactiveEmployee.setPassword("123456");
        inactiveEmployee.setActive(false);
        inactiveEmployee.setAdmin(false);

        entityManager.persist(activeEmployee);
        entityManager.persist(inactiveEmployee);
        entityManager.flush();

        // Act (Quando)
        List<Employee> activeList = employeeRepository.findByActive(true);

        // Assert (Então)
        assertFalse(activeList.isEmpty());
        assertTrue(activeList.stream().allMatch(Employee::getActive));
    }

    @Test
    @DisplayName("Deve listar funcionários filtrando por cargo (Role) e status ativo")
    void deveListarPorRoleEActiveComSucesso() {
        // Arrange (Dado)
        Employee employee = new Employee();
        employee.setName("Caixa Ativo");
        employee.setCpf("33333333333");
        employee.setEmail("caixa@email.com");
        employee.setUsername("caixa");
        employee.setPassword("123456");
        employee.setActive(true);
        employee.setAdmin(false);
        employee.setRole(EmployeeRole.CASHIER);

        entityManager.persistAndFlush(employee);

        // Act (Quando)
        List<Employee> result = employeeRepository.findByRoleAndActive(EmployeeRole.CASHIER, true);

        // Assert (Então)
        assertFalse(result.isEmpty());
        assertEquals(EmployeeRole.CASHIER, result.get(0).getRole());
        assertTrue(result.get(0).getActive());
    }
}