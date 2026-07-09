package cpf.exs.edu.endpoint;

import java.util.Map;

/**
 * 기관별 endpoint 설정을 업무 adapter에서 선택하는 샘플입니다.
 */
public class ExsEndpointEducationSample {

    public Map<String, String> endpoint(String institutionCode) {
        return Map.of(
                "institutionCode", institutionCode,
                "endpointCode", "EXS_" + institutionCode,
                "runtime", "external-runtime-required");
    }
}
