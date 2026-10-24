
package com.new_big.web.service;

import com.new_big.web.controller.batch.dto.BatchConsumption;
import com.new_big.web.entity.Product;
import com.new_big.web.entity.Sale;
import com.new_big.web.entity.SaleItem;
import com.new_big.web.repository.SaleItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleItemServiceTest {

    private static final Long PRODUCT_ID = 1L;
    private static final Long SALE_ID = 2L;

    private static final BigDecimal UNIT_PRICE =
            new BigDecimal("12.50");

    @Mock
    private SaleItemRepository repository;

    @InjectMocks
    private SaleItemService service;

    // ==========================================
    // SAVE
    // ==========================================

    @Test
    void caso_Salvar_Itens_Quando_Existirem_Consumos() {

        // Given — existe um produto, uma venda e dois consumos de lote.
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setSalePrice(UNIT_PRICE);

        Sale sale = new Sale();
        sale.setId(SALE_ID);

        List<BatchConsumption> consumptions = List.of(
                new BatchConsumption(101L, "Lt-01", 1),
                new BatchConsumption(102L, "Lt-02",2)
        );

        // When — o serviço transforma os consumos em itens da venda.
        List<SaleItem> result = service.save(
                consumptions,
                product,
                sale
        );

        // Then — cada consumo deve gerar um item com os valores corretos.
        assertAll(
                () -> assertEquals(2, result.size()),

                // Primeiro item: 2 unidades × R$ 12,50 = R$ 25,00.
                () -> assertAll(
                        "Primeiro item",
                        () -> assertEquals(
                                2,
                                result.get(0).getQuantity()
                        ),
                        () -> assertMoneyEquals(
                                UNIT_PRICE,
                                result.get(0).getUnitPrice()
                        ),
                        () -> assertMoneyEquals(
                                new BigDecimal("25.00"),
                                result.get(0).getTotal()
                        ),
                        () -> assertSame(
                                product,
                                result.get(0).getProduct()
                        ),
                        () -> assertSame(
                                sale,
                                result.get(0).getSale()
                        )
                ),

                // Segundo item: 3 unidades × R$ 12,50 = R$ 37,50.
                () -> assertAll(
                        "Segundo item",
                        () -> assertEquals(
                                3,
                                result.get(1).getQuantity()
                        ),
                        () -> assertMoneyEquals(
                                UNIT_PRICE,
                                result.get(1).getUnitPrice()
                        ),
                        () -> assertMoneyEquals(
                                new BigDecimal("37.50"),
                                result.get(1).getTotal()
                        ),
                        () -> assertSame(
                                product,
                                result.get(1).getProduct()
                        ),
                        () -> assertSame(
                                sale,
                                result.get(1).getSale()
                        )
                )
        );

        // Então cada item gerado deve ser salvo individualmente.
        verify(repository, times(2)).save(any(SaleItem.class));

        verify(repository).save(result.get(0));
        verify(repository).save(result.get(1));
    }

    @Test
    void caso_Salvar_Itens_Quando_Nao_Existirem_Consumos() {

        // Given — a lista de consumos está vazia.
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setSalePrice(UNIT_PRICE);

        Sale sale = new Sale();
        sale.setId(SALE_ID);

        List<BatchConsumption> consumptions = List.of();

        // When — o serviço tenta gerar os itens.
        List<SaleItem> result = service.save(
                consumptions,
                product,
                sale
        );

        // Then — nenhum item deve ser criado ou salvo.
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(repository);
    }

    // ==========================================
    // HELPERS
    // ==========================================

    private static void assertMoneyEquals(
            BigDecimal expected,
            BigDecimal actual
    ) {
        assertNotNull(
                actual,
                "O valor monetário não deveria ser nulo."
        );

        assertEquals(
                0,
                expected.compareTo(actual),
                () -> "Esperado: " + expected + ", recebido: " + actual
        );
    }
}
