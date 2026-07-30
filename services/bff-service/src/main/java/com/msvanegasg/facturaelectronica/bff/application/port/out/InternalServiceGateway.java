package com.msvanegasg.facturaelectronica.bff.application.port.out;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;

public interface InternalServiceGateway {

    ProxyResponse exchange(ProxyRequest request);
}
