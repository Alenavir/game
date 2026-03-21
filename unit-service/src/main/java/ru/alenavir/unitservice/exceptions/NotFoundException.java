package ru.alenavir.unitservice.exceptions;


public class NotFoundException extends UnitException {
    public NotFoundException(String message) {
        super(message);
    }
}