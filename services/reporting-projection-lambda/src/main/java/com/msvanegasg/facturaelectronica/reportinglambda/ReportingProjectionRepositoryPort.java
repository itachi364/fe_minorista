package com.msvanegasg.facturaelectronica.reportinglambda;

public interface ReportingProjectionRepositoryPort {

    boolean projectIfNew(ReportingProjectionRequest request);
}