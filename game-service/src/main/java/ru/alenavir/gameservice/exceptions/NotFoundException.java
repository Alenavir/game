package ru.alenavir.gameservice.exceptions;


public class NotFoundException extends GameException {
    public NotFoundException(String message) {
        super(message);
    }
}