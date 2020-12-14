package com.meter.energy.consumption.exception;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public List<ErrorMessageDto> handleBindException(BindException exception) {
        return exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(e -> new ErrorMessageDto(e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.toList());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public List<ErrorMessageDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(e -> new ErrorMessageDto(e.getField(), e.getDefaultMessage()))
                        .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ApplicationException.class)
    @ResponseBody
    public ErrorMessageDto handleApplicationException(
                                                      final ApplicationException exception) {
        return new ErrorMessageDto(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseBody
    public ErrorMessageDto handleSQLIntegrityConstraintViolationException(
                                                                          final SQLIntegrityConstraintViolationException exception) {
        return new ErrorMessageDto(HttpStatus.BAD_REQUEST, "Something went wrong : " + exception.getMessage());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ErrorMessageDto handleException(
                                           final Exception exception) {
        return new ErrorMessageDto(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong : " + exception.getMessage());
    }

}
