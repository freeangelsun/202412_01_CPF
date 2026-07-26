package com.cpf.core.api.tenant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CpfTenantContextTest { @Test void clearsThreadLocal(){ CpfTenantContext.set("T1"); assertEquals("T1",CpfTenantContext.require()); CpfTenantContext.clear(); assertNull(CpfTenantContext.current()); } }
