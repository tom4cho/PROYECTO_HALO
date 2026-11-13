package com.example.service;

/**
 * Excepción que transporta el mensaje de rechazo por inconsistencia de rama
 * o por entidad inexistente.
 */
public class ReglaRamaException extends RuntimeException {

    public ReglaRamaException(String message) {
        super(message);
    }
}
