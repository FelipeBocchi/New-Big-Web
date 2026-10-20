package com.new_big.web.exception;

// Crie essa class para usar na servece para pegar uma messagem do erro e lançar no GlobalException
public class ConflictException extends RuntimeException {

    public ConflictException(String message){
        super(message);
    }
}
