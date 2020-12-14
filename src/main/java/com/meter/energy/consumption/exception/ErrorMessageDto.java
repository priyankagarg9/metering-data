package com.meter.energy.consumption.exception;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(Include.NON_NULL)
@ApiModel(value = "Generic Error Message", description = "A generic error message in case of failures.")
public class ErrorMessageDto {

    @ApiModelProperty(value = "The error message.")
    private String message;

    @ApiModelProperty(value = "The field.")
    private String field;

    @ApiModelProperty(value = "The response status.")
    private String status;

    public ErrorMessageDto() {}

    public ErrorMessageDto(final String message) {
        this.message = message;
    }

    public ErrorMessageDto(final HttpStatus status, final String message) {
        this.message = message;
        this.status = status.toString();
    }

    public ErrorMessageDto(final String field, final String message) {
        this.message = message;
        this.field = field;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

}
