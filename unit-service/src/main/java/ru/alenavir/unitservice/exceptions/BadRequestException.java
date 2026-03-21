package ru.alenavir.unitservice.exceptions;

public class BadRequestException extends UnitException {
    public BadRequestException(String message) {
        super(message);
    }
}