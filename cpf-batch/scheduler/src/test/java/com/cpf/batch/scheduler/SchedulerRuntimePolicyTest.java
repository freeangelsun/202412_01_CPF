package com.cpf.batch.scheduler;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SchedulerRuntimePolicyTest {
    @Test
    void sharedRuntimePolicyChangesDispatchAndCalendarGates() {
        SchedulerDispatchService service = new SchedulerDispatchService(null, null, null, null, module -> null);
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        service.setRuntimePolicy(policy);
        assertTrue(service.runtimeEnabled());
        assertTrue(service.calendarRuntimeEnabled());
        policy.replaceSchedule(1L, false);
        policy.replaceCalendar(2L, false);
        assertFalse(service.runtimeEnabled());
        assertFalse(service.calendarRuntimeEnabled());
    }
}
