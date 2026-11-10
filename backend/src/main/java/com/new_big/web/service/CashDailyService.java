package com.new_big.web.service;

import com.new_big.web.controller.cashDailyController.dto.CashDailyClosedRequest;
import com.new_big.web.controller.cashDailyController.dto.CashDailyOpenedRequest;
import com.new_big.web.entity.CashDailyLog;
import com.new_big.web.entity.Employee;
import com.new_big.web.enums.CashStatus;
import com.new_big.web.enums.PaymentMethod;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.CashDailyRepository;
import com.new_big.web.repository.EmployeeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashDailyService {

    private final CashDailyRepository repository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public CashDailyLog open(CashDailyOpenedRequest request) {

        log.info("Criando novo caixa pelo funcionário id={}", request.getEmployeeId());

        Employee employee = this.employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com o id"));

        CashDailyLog cash = new CashDailyLog();

        cash.setOpeningAmount(request.getOpeningAmount());
        cash.setClosingAmount(null);
        cash.setExpectedClosingAmount(request.getOpeningAmount());
        cash.setTotal(BigDecimal.ZERO);
        cash.setOpenedAt(LocalDateTime.now());
        cash.setClosedAt(null);
        cash.setStatus(CashStatus.OPEN);
        cash.setOpenedBy(employee);
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
        CashDailyLog cash = this.findInfoById(id);

        if(cash.getStatus() == CashStatus.CLOSED) {
            log.warn("Caixa deve estar aberto para adicionar um valor a ele!");
            throw new ConflictException("Caixa deve estar aberto para adicionar um valor a ele!");
        }

        BigDecimal total = cash.getTotal();
        if (method == PaymentMethod.CASH) {
            BigDecimal d = cash.getExpectedClosingAmount();
            cash.setExpectedClosingAmount(d.add(amount));
            cash.setTotal(total.add(amount));
        } else {
            cash.setTotal(total.add(amount));
        }

        log.info("Alterações feitas com sucesso!");
        this.repository.save(cash);
    }

    @Transactional
    public CashDailyLog close(CashDailyClosedRequest request, Long id) {
        log.info("Fechando caixa. id={}", id);
        CashDailyLog cash = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caixa não encontrado no banco com esse id"));

        if (cash.getStatus() == CashStatus.CLOSED) {
            log.warn("Caixa ainda não foi ou esta aberto!");
            throw new ConflictException("Caixa ainda não foi ou esta aberto!");
        }

        // Regra de negócio de fechamento de caixa, se o valor que o funcionário informar não bater com a conta interna gera um erro
        if (!cash.getExpectedClosingAmount().equals(request.getClosingAmount())) {
            log.warn("Valor experado no caixa em dinheiro não é compativel com o valor informado");
            throw new ConflictException("Valor experado no caixa em dinheiro não é compativel com o valor informado");
        }

        if (cash.getClosingAmount() == null) cash.setClosingAmount(request.getClosingAmount());
        if (cash.getClosedAt() == null) cash.setClosedAt(LocalDateTime.now());
        if (cash.getClosedBy() == null) {
            Employee employee = this.employeeRepository.findById(request.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado com ess id"));

            cash.setClosedBy(employee);
        }

        cash.setStatus(CashStatus.CLOSED);

        log.info("Caixa fechado com sucesso!");
        return this.repository.save(cash);
    }

    public List<CashDailyLog> findAll() { return this.repository.findAll(); }

    public CashDailyLog findInfoById(Long id) {

        return this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possivel encontrar um caixa com esse id"));
    }

}
