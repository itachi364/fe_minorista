package com.msvanegasg.facturaelectronica.catalog.application.port.out;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogAuditEventCommand;

public interface CatalogAuditEventPort {

    void register(CatalogAuditEventCommand command);

    static CatalogAuditEventPort noop() {
        return command -> {
        };
    }
}
