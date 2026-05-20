package com.msvanegasg.facturaelectronica.tenant.application.usecase;

public class CompanyAlreadyExistsException extends RuntimeException {

    public CompanyAlreadyExistsException(String identificationNumber) {
        super("Company already exists for identification number " + identificationNumber);
    }
}
