package com.msvanegasg.facturaelectronica.identity.application.dto;

import java.util.Set;

public record CognitoSessionCommand(String subject, String email, String fullName, Set<String> groups) {
}
