package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchConsumption;
import com.new_big.web.controller.saleController.dto.SaleAddRequest;
import com.new_big.web.controller.saleController.dto.SaleCloseRequest;
import com.new_big.web.controller.saleController.dto.SaleOpenRequest;
import com.new_big.web.entity.*;
import com.new_big.web.enums.SaleStatus;
import com.new_big.web.exception.ConflictException;
import com.new_big.web.exception.ResourceNotFoundException;
import com.new_big.web.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository repository;
    private final CashDailyRepository cashDailyRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final BatchService batchService;
    private final StockMovementService movementService;
    private final SaleItemService itemService;
    private final CashDailyService cashService;

    @Transactional
    public Sale open(SaleOpenRequest request) {

        log.info("Abrindo uma nova venda, caixa: id={}", request.getCashId());

        CashDailyLog cash = this.cashDailyRepository.findById(request.getCashId())
                .orElseThrow(() -> new ResourceNotFoundException("Caixa não emcontrado com o id"));

        Employee employee = this.employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não emcontrado com o id"));

        Sale sale = new Sale();
        if (request.getCustomerId() != null) {
            Customer customer = this.customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente não emcontrado com o id"));
            sale.setCustomer(customer);
        } else sale.setCustomer(null);


        sale.setSubtotal(BigDecimal.valueOf(0));
        sale.setDiscount(BigDecimal.valueOf(0));
        sale.setTotal(BigDecimal.valueOf(0));
        sale.setStatus(SaleStatus.OPENED);
        sale.setPaymentMethod(null);
        sale.setEmployee(employee);
        sale.setCashier(cash);

        Sale saved = this.repository.save(sale);
        log.info("Nova venda aberta com sucesso!");
        return saved;
    }

    public List<Sale> findAll() { return this.repository.findAll(); }

    @Transactional
    public String addItems(SaleAddRequest request) {

        log.info("Adicionando um novo produto a venda id={}", request.getId());
        Sale sale = this.repository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o id"));

        if (sale.getStatus() != SaleStatus.OPENED) {
            throw new ConflictException("Venda não está aberta!");
        }

        if (!this.productRepository.existsByBarcode(request.getBarcode())) {
            log.warn("Código de barro informado não existe no banco! codebar={}", request.getBarcode());
            throw new ConflictException("Código de barro informado não existe no banco!");
        }
        Product product = this.productRepository.findByBarcode(request.getBarcode());

        List<BatchConsumption> consumptions = this.batchService.fefo(product.getId(), request.getQuantity());

        List<SaleItem> items = this.itemService.save(consumptions, product, sale);

        List<StockMovement> movements = this.movementService.saveMovementSale(consumptions, sale);

        BigDecimal subTotal = BigDecimal.ZERO;
        for (SaleItem i : items) {
            subTotal = subTotal.add(i.getTotal());
        }

        BigDecimal total = subTotal.subtract(request.getDiscountUnit());

        BigDecimal discont = sale.getDiscount().add(request.getDiscountUnit());
        sale.setDiscount(discont);
        sale.setSubtotal(subTotal);
        sale.setTotal(total);

        //sale.setTotal(BigDecimal.valueOf(100));

        items.forEach(sale::addSaleItem);

        movements.forEach(sale::addStockMovement);

        Sale saved = this.repository.save(sale);
        log.info("Item adicionado com sucesso!");
        return "Item adicionado com sucesso!";

    }

    @Transactional
    public Sale close(SaleCloseRequest request, Long id) {
        log.info("Inicializando o fechamento da venda id={}", id);

        Sale sale = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não emcontrado com o id"));

        // pagamento
        if (!request.getValueArrived().equals(sale.getTotal())) {
            log.warn("Valor recebido não bate com o valor total da venda");
            throw new ResourceNotFoundException("Valor recebido não bate com o valor total da venda");
        }

        // futuramente vai precisar ter uma função separado para o front chamar quando for cartão, pix ou dinheiro, mas agora vamos manter assim

        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setStatus(SaleStatus.COMPLETED);

        // vai passar os valores que deu na venda para o caixa aberto no momento
        this.cashService.addAmountToCash(request.getCashId(), sale.getTotal(), sale.getPaymentMethod());

        Sale saved = this.repository.save(sale);
        log.info("Venda fechada com sucesso!");
        return saved;
    }

}
