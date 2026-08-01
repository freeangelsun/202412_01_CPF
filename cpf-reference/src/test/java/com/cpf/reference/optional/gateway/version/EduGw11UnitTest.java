package com.cpf.reference.optional.gateway.version;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-GW-11 UnitTest — Command 멱등성·Attempt Ledger·응답 유실 */
public final class EduGw11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw11Handler(); }
}
