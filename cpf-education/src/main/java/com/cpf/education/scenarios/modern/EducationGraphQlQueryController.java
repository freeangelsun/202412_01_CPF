package com.cpf.education.scenarios.modern;
import com.cpf.platform.operations.api.health.CpfHealthSnapshotProvider;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
/** EducationGraphQlQueryController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationGraphQlQueryController {
    private final ObjectProvider<CpfHealthSnapshotProvider> health;
    public EducationGraphQlQueryController(ObjectProvider<CpfHealthSnapshotProvider> health) { this.health = health; }
    @QueryMapping
    public Map<String,Object> cpfRuntimeHealth() {
        var provider = health.getIfAvailable();
        if (provider == null) return Map.of("status", "DISABLED", "instanceId", "n/a", "draining", false);
        var snapshot = provider.snapshot();
        return Map.of("status", snapshot.readiness().name(), "instanceId", snapshot.instanceId(), "draining", snapshot.draining());
    }
}
