package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import java.net.URI;
import java.util.Collections;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.in.ProxyPublicApiUseCase;
import com.msvanegasg.facturaelectronica.bff.domain.model.BffRouteResolver;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class BffProxyController {

    private final ProxyPublicApiUseCase proxyUseCase;
    private final BffRouteResolver routeResolver;

    public BffProxyController(ProxyPublicApiUseCase proxyUseCase) {
        this.proxyUseCase = proxyUseCase;
        this.routeResolver = new BffRouteResolver();
    }

    @RequestMapping("/api/v1/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest servletRequest, @RequestBody(required = false) byte[] body) {
        String path = servletRequest.getRequestURI();
        TargetService targetService = routeResolver.resolve(path);
        URI targetUri = URI.create(path + queryString(servletRequest));
        HttpMethod method = HttpMethod.valueOf(servletRequest.getMethod());
        ProxyResponse response = proxyUseCase.proxy(new ProxyRequest(targetService, method, targetUri,
                copyHeaders(servletRequest), body));
        return ResponseEntity.status(response.status())
                .headers(response.headers())
                .body(response.body());
    }

    private static String queryString(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null || query.isBlank() ? "" : "?" + query;
    }

    private static HttpHeaders copyHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> headers.put(name,
                Collections.list(request.getHeaders(name))));
        return headers;
    }
}
