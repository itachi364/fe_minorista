package com.msvanegasg.facturaelectronica.bff.application.dto;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;

public record ProxyRequest(TargetService targetService, HttpMethod method, URI uri, HttpHeaders headers, byte[] body) {
}
