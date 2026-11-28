package com.newbig.web.service;

import com.newbig.web.controller.batch.dto.BatchConsumption;
import com.newbig.web.controller.sale.dto.SaleAddRequest;
import com.newbig.web.controller.sale.dto.SaleCloseRequest;
import com.newbig.web.controller.sale.dto.SaleDetailsResponse;
import com.newbig.web.controller.sale.dto.SaleOpenRequest;
import com.newbig.web.entity.*;
import com.newbig.web.enums.SaleStatus;
import com.newbig.web.exception.ConflictException;
import com.newbig.web.exception.ResourceNotFoundException;
import com.newbig.web.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

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

        items.forEach(sale::addSaleItem);
        movements.forEach(sale::addStockMovement);

        BigDecimal subtotal = sale.getSaleItems().stream()
                .map(SaleItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discount =
                sale.getDiscount().add(request.getDiscountUnit());

        sale.setSubtotal(subtotal);
        sale.setDiscount(discount);
        sale.setTotal(subtotal.subtract(discount));


        this.repository.save(sale);
        log.info("Item adicionado com sucesso!");
        return "Item adicionado com sucesso!";

    }

    @Transactional
    public Sale close(SaleCloseRequest request, Long id) {
        log.info("Inicializando o fechamento da venda id={}", id);

        Sale sale = this.repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não emcontrado com o id"));

        if (sale.getStatus() != SaleStatus.OPENED) {
            log.warn("Venda está com status fechado já");
            throw new ConflictException("Somente vendas abertas podem ser fechadas");
        }

        // pagamento
        if (request.getValueArrived().compareTo(sale.getTotal()) != 0) {
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

    @Transactional
    public SaleDetailsResponse findDetails(Long id) {
        return repository.findDetailsById(id)
                .map(SaleDetailsResponse::de)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));
    }

    @Transactional
    public Sale cancel(Long id, Long cashId, Long employeeId) {
        Sale sale = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venda não encontrada com o ID: " + id));

        if (!Objects.equals(sale.getCashier().getId(), cashId)) {
            throw new ConflictException("A venda pertence a outro caixa");
        }
        if (sale.getStatus() != SaleStatus.OPENED) {
            throw new ConflictException("Somente vendas abertas podem ser canceladas");
        }

        movementService.reverseSale(sale, employeeId);
        sale.setStatus(SaleStatus.CANCELLED);
        return repository.save(sale);
    }
}
