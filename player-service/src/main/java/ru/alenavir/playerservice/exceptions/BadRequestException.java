package ru.alenavir.playerservice.exceptions;

public class BadRequestException extends PlayerException {
    public BadRequestException(String message) {
        super(message);
    }
}