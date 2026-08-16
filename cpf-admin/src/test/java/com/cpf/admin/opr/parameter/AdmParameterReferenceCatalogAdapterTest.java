package com.cpf.admin.opr.parameter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.gateway.api.CpfGatewayRegistryPort;
import com.cpf.common.parameter.api.CpfParameterReferenceCatalogPort.ReferenceQuery;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

class AdmParameterReferenceCatalogAdapterTest {
    @Test
    void configuredSecretMetadataIsSearchableWithoutExposingValue() {
        AdmParameterReferenceProperties properties=new AdmParameterReferenceProperties();
        var secret=new AdmParameterReferenceProperties.SecretRef();
        secret.setLabel("정산 API Credential"); secret.setProviderId("vault"); secret.setKey("settlement/api");
        properties.getSecrets().put("SETTLEMENT_API",secret);
        StaticListableBeanFactory factory=new StaticListableBeanFactory();
        var adapter=new AdmParameterReferenceCatalogAdapter(
                factory.getBeanProvider(CpfServiceRegistryQueryPort.class),
                factory.getBeanProvider(CpfGatewayRegistryPort.class),properties);
        var page=adapter.search(new ReferenceQuery("SECRET_REFERENCE",null,null,"정산",0,20,"operator"));
        assertTrue(page.installed()); assertTrue(page.available()); assertEquals(1,page.items().size());
        assertEquals("vault",page.items().getFirst().metadata().get("providerId"));
        assertEquals(false,page.items().getFirst().metadata().get("valueExposed"));
        assertFalse(page.items().getFirst().metadata().containsKey("value"));
    }
}
