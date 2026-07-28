package com.msvanegasg.facturaelectronica.identity.application.dto;

public record CreateUserCommand(String email, String fullName, String password) {
}
