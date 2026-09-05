-- CPF Service Registry endpoint provisioning. 정본 계약: cpf-tools/db/canonical/service-registry-provisioning.json
-- endpoint code 의 정본은 Runtime Control 의 cpf.runtime.control.agent.endpoint-code 기본값이다.
-- Runtime 은 service 와 endpoint 가 모두 등록되어 있어야 기동한다(fail-closed).
INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, use_yn, created_by, updated_by) VALUES (:endpointCode, :serviceId, :endpointName, :endpointType, :baseUrl, :useYn, :createdBy, :updatedBy)
