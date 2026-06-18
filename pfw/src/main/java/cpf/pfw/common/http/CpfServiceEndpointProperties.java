package cpf.pfw.common.http;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Service endpoint configuration for inter-service HTTP calls.
 *
 * <p>Example: {@code cpf.services.mbr.base-url=http://localhost:8081}</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cpf")
public class CpfServiceEndpointProperties {

    /** Map keyed by service id such as {@code mbr}, {@code acc}, {@code cmn}, and {@code adm}. */
    private Map<String, ServiceEndpoint> services = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ServiceEndpoint {

        /** Base URL of the target service. In production this should point to VIP, DNS, or Kubernetes Service. */
        private String baseUrl;

        /** Human-readable service description for documentation and diagnostics. */
        private String description;
    }
}
