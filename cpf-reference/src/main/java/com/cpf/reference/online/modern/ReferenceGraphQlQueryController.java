package com.cpf.reference.online.modern;

import com.cpf.core.api.health.CpfHealthSnapshotProvider;
import java.util.Map;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ReferenceGraphQlQueryController {
    private final ObjectProvider<CpfHealthSnapshotProvider> health;
    public ReferenceGraphQlQueryController(ObjectProvider<CpfHealthSnapshotProvider> health) { this.health = health; }
    @QueryMapping
    public Map<String,Object> cpfRuntimeHealth() {
        var provider = health.getIfAvailable();
        if (provider == null) return Map.of("status", "DISABLED", "instanceId", "n/a", "draining", false);
        var snapshot = provider.snapshot();
        return Map.of("status", snapshot.readiness().name(), "instanceId", snapshot.instanceId(), "draining", snapshot.draining());
    }
}
