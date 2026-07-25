package com.cpf.core.common.product;

import com.cpf.core.api.product.CpfCapability;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class CpfCapabilityRegistryTest {
 @Test void installedAndLicensedIntersectionIsActive(){
  CpfCapabilityRegistry r=new CpfCapabilityRegistry(Set.of(CpfCapability.CORE_RUNTIME,CpfCapability.MULTI_TENANT),()->Set.of(CpfCapability.CORE_RUNTIME));
  assertTrue(r.enabled(CpfCapability.CORE_RUNTIME)); assertFalse(r.enabled(CpfCapability.MULTI_TENANT));
 }
}
