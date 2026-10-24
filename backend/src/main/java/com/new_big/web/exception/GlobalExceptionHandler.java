package com.new_big.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata erros quando o recurso solicitado não foi encontrado.
    // Exemplo: buscar um cliente por um ID que não existe.
    // Retorna: HTTP 404 - NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound( ResourceNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);

        problem.setTitle("Recurso não encontrado");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    // Trata conflitos de regra de negócio.
    // Exemplo: tentar cadastrar um CPF ou e-mail que já existe.
    // Retorna: HTTP 409 - CONFLICT
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict( ConflictException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);

        problem.setTitle("Conflito");
        problem.setDetail(exception.getMessage());

        return problem;
    }

    // Trata erros de validação dos dados recebidos na requisição.
    // Exemplo: campo obrigatório vazio, CPF inválido, e-mail inválido etc.
    // Normalmente ocorre através do @Valid no Request DTO.
    // Retorna: HTTP 400 - BAD_REQUEST
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);

        problem.setTitle("Erro de validação");
        problem.setDetail("Um ou mais campos são inválidos.");

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        problem.setProperty("errors", errors);

        return problem;
    }

    // Trata erros relacionados ao acesso ao banco de dados.
    // Exemplo: falha de conexão ou indisponibilidade do banco.
    // Retorna: HTTP 503 - SERVICE_UNAVAILABLE
    @ExceptionHandler( DataFormatException.class)
    public ProblemDetail handleDatabase(DataFormatException exception) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);

        problem.setTitle("Serviço temporariamente indisponível");
        problem.setDetail("Não foi possível acessar o banco de dados.");

        return problem;
    }

    // Trata exceções inesperadas que não possuem um handler específico.
    // Serve como último nível de segurança para evitar expor detalhes internos.
    // Retorna: HTTP 500 - INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric( Exception ex) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        problem.setTitle("Erro interno");
        problem.setDetail("Ocorreu um erro inesperado no servidor.");

        return problem;
    }
}
