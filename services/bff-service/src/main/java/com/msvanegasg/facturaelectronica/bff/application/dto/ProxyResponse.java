package com.msvanegasg.facturaelectronica.bff.application.dto;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;

public record ProxyResponse(HttpStatusCode status, HttpHeaders headers, byte[] body) {
}
