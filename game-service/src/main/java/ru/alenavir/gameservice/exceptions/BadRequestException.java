package ru.alenavir.gameservice.exceptions;

public class BadRequestException extends GameException {
    public BadRequestException(String message) {
        super(message);
    }
}