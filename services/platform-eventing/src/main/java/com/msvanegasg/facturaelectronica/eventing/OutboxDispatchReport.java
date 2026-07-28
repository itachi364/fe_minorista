package com.msvanegasg.facturaelectronica.eventing;

public record OutboxDispatchReport(int attempted, int published, int failed) {

    public int total() {
        return published + failed;
    }
}