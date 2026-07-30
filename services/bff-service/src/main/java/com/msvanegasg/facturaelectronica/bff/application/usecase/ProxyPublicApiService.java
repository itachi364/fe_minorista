package com.msvanegasg.facturaelectronica.bff.application.usecase;

import org.springframework.stereotype.Service;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.in.ProxyPublicApiUseCase;
import com.msvanegasg.facturaelectronica.bff.application.port.out.InternalServiceGateway;

@Service
public class ProxyPublicApiService implements ProxyPublicApiUseCase {

    private final InternalServiceGateway gateway;

    public ProxyPublicApiService(InternalServiceGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public ProxyResponse proxy(ProxyRequest request) {
        return gateway.exchange(request);
    }
}
