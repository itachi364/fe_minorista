package com.msvanegasg.facturaelectronica.bff.application.port.in;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;

public interface ProxyPublicApiUseCase {

    ProxyResponse proxy(ProxyRequest request);
}
