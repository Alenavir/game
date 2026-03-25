package ru.alenavir.playerservice.exceptions;


public class NotFoundException extends PlayerException {
    public NotFoundException(String message) {
        super(message);
    }
}