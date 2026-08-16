package com.cpf.integration.http.internal.servicecall;

@FunctionalInterface
interface ServiceCallAttemptObserver {
    void onAttempt(ServiceCallAttemptEvent event);

    static ServiceCallAttemptObserver noOp() { return ignored -> { }; }
}
