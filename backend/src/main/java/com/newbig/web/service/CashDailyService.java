package com.newbig.web.service;

import com.newbig.web.controller.cashdaily.dto.CashDailyClosedRequest;
import com.newbig.web.controller.cashdaily.dto.CashDailyOpenedRequest;
import com.newbig.web.entity.CashDailyLog;
import com.newbig.web.entity.Employee;
import com.newbig.web.enums.CashStatus;
import com.newbig.web.enums.PaymentMethod;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.CashDailyRepository;
import com.newbig.web.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashDailyService {

    private final CashDailyRepository repository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    @Transactional
    public CashDailyLog open(CashDailyOpenedRequest request) {

        if (request == null) {
            throw new ConflictException("Informe os dados de abertura");
        }
        log.info("Criando novo caixa pelo funcionário id={}", request.getEmployeeId());

        requirePositive(request.getOpeningAmount());

        Employee employee = findEmployee(request.getEmployeeId());

        CashDailyLog cash = new CashDailyLog();
        cash.setOpeningAmount(request.getOpeningAmount());
        cash.setExpectedClosingAmount(request.getOpeningAmount());
        cash.setTotal(BigDecimal.ZERO);
        cash.setOpenedAt(LocalDateTime.now(clock));
        cash.setOpenedBy(employee);
        cash.setStatus(CashStatus.OPEN);
        cash.setClosingAmount(null);
        cash.setClosedAt(null);
        cash.setClosedBy(null);
        cash.setSales(new ArrayList<>());

        CashDailyLog saved = this.repository.save(cash);
        log.info("Caixa criado com sucesso.");
        return saved;
    }

    // Essa lógica vai ser chamanda somente pela servece do Sale ao finalizar uma venda
    @Transactional
    public void addAmountToCash(Long id, BigDecimal amount, PaymentMethod method) {
        log.info("Inicializando o processo de adição do valor e tipo da venda feita nesse caixa id={}", id);
        requirePositive(amount);

        if (method == null) {
            throw new ConflictException("Informe o método de pagamento");
        }

        CashDailyLog cash = findInfoById(id);
        requireOpen(cash);

        if(cash.getStatus() == CashStatus.CLOSED) {
            log.warn("Caixa deve estar aberto para adicionar um valor a ele!");
            throw new ConflictException("Caixa deve estar aberto para adicionar um valor a ele!");
        }

        BigDecimal total = cash.getTotal().add(amount);
        BigDecimal expected = cash.getExpectedClosingAmount();

        if (method == PaymentMethod.CASH) {
            expected = expected.add(amount);
        }

        cash.setTotal(total);
        cash.setExpectedClosingAmount(expected);


        repository.save(cash);
    }

    @Transactional
    public CashDailyLog close(CashDailyClosedRequest request, Long id) {
        log.info("Fechando caixa. id={}", id);

        if (request == null) {
            throw new ConflictException("Informe os dados de fechamento");
        }

        BigDecimal received = request.getClosingAmount();

        if (received == null || received.signum() < 0) {
            throw new ConflictException(
                    "O valor de fechamento não pode ser nulo ou negativo"
            );
        }

        CashDailyLog cash = findInfoById(id);
        requireOpen(cash);

        if (cash.getExpectedClosingAmount().compareTo(received) != 0) {
            throw new ConflictException(
                    "Valor informado difere do dinheiro esperado no caixa"
            );
        }

        // Busca o funcionário antes de modificar a entidade.
        Employee employee = findEmployee(request.getEmployeeId());

        cash.setClosingAmount(received);
        cash.setClosedAt(LocalDateTime.now(clock));
        cash.setClosedBy(employee);
        cash.setStatus(CashStatus.CLOSED);

        return repository.save(cash);
    }

    public List<CashDailyLog> findAll() { return this.repository.findAll(); }

    public CashDailyLog findInfoById(Long id) {
        if (id == null || id <= 0) {
            throw new ConflictException("Informe um ID de caixa válido");
        }

        return repository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Caixa não encontrado com o ID: " + id
                        )
                );
    }

    private Employee findEmployee(Long id) {
        if (id == null || id <= 0) {
            throw new ConflictException(
                    "Informe um ID de funcionário válido"
            );
        }

        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Funcionário não encontrado com o ID: " + id
                        )
                );
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ConflictException(
                    "O valor deve ser maior que zero"
            );
        }
    }

    private void requireOpen(CashDailyLog cash) {
        if (cash.getStatus() != CashStatus.OPEN) {
            throw new ConflictException("O caixa deve estar aberto");
        }

        if (cash.getClosingAmount() != null
                || cash.getClosedAt() != null
                || cash.getClosedBy() != null) {
            throw new ConflictException(
                    "Caixa aberto com dados de fechamento: regularize o cadastro"
            );
        }

        if (cash.getTotal() == null
                || cash.getExpectedClosingAmount() == null) {
            throw new ConflictException(
                    "Caixa com saldos não inicializados"
            );
        }
    }

}
