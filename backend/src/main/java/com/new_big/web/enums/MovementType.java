package com.new_big.web.enums;

public enum MovementType {
    ENTRY,       // entrada de lote (recebimento de fornecedor)
    SALE_EXIT,   // saída por venda
    LOSS,        // perda (vencimento, quebra, descarte)
    ADJUSTMENT   // ajuste manual de inventário (correção de contagem)
}
