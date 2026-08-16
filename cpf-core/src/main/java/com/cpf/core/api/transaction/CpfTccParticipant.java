package com.cpf.core.api.transaction;

/**
 * Hold/Reservation의 업무 의미는 Framework가 구현하지 않고 실제 Business Consumer가 구현합니다.
 * 구현체는 Try/Confirm/Cancel을 각각 멱등하게 처리해야 합니다.
 */
public interface CpfTccParticipant<C> {
    CpfTccResult tryAction(CpfTccContext context, C command);
    CpfTccResult confirm(CpfTccContext context, C command);
    CpfTccResult cancel(CpfTccContext context, C command);
}
