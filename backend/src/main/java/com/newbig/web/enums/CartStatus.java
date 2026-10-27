package com.newbig.web.enums;

public enum CartStatus {
    OPEN,        // cliente ainda escaneando produtos
    FINALIZED,   // cliente encerrou, aguardando caixa
    CONVERTED,   // já virou uma Sale (impede reuso)
    EXPIRED      // passou do expiresAt sem finalizar
}
