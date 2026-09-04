package com.cpf.web.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.web.context.CpfOperationOwnerResolver.CpfOperationOwner;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

class CpfClasspathOperationOwnerResolverTest {
    @Test
    void selectsTheMostSpecificExplicitDescriptorWithoutLookingAtOperationIdPrefixes() throws Exception {
        CpfClasspathOperationOwnerResolver resolver = new CpfClasspathOperationOwnerResolver(List.of(
                new CpfOperationOwner("MBR", "member", null, "com.cpf.web"),
                new CpfOperationOwner("MBW", "backoffice", null, "com.cpf.web.runtime")));
        Method method = OwnedHandler.class.getDeclaredMethod("handle");

        CpfOperationOwner owner = resolver.resolve(new HandlerMethod(new OwnedHandler(), method), "EXS_NOT_A_SOURCE");

        assertThat(owner.systemCode()).isEqualTo("MBW");
        assertThat(owner.domainCode()).isEqualTo("backoffice");
    }

    static final class OwnedHandler { void handle() { } }
}
