package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.PaymentMethodCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.PaymentMethod;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.PaymentMethodRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.PaymentMethodResponse;

public final class PaymentMethodRestMapper {

    private PaymentMethodRestMapper() {
    }

    public static PaymentMethodCommand toCommand(PaymentMethodRequest dto) {
        return new PaymentMethodCommand(dto.getNombre(), dto.getDescripcion());
    }

    public static PaymentMethodResponse toResponse(PaymentMethod paymentMethod) {
        return PaymentMethodResponse.builder()
                .idMetodoPago(paymentMethod.id())
                .nombre(paymentMethod.name())
                .descripcion(paymentMethod.description())
                .activo(paymentMethod.active())
                .build();
    }

    public static PaymentMethodRequest toRequest(PaymentMethod paymentMethod) {
        return PaymentMethodRequest.builder()
                .nombre(paymentMethod.name())
                .descripcion(paymentMethod.description())
                .build();
    }
}
