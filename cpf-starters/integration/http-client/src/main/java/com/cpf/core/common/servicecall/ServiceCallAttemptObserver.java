package com.cpf.core.common.servicecall;

@FunctionalInterface
interface ServiceCallAttemptObserver {
    void onAttempt(ServiceCallAttemptEvent event);

    static ServiceCallAttemptObserver noOp() { return ignored -> { }; }
}
