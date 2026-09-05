-- CPF Service Registry endpoint provisioning. 정본 계약: cpf-tools/db/canonical/service-registry-provisioning.json
-- endpoint code 의 정본은 Runtime Control 의 cpf.runtime.control.agent.endpoint-code 기본값이다.
-- Runtime 은 service 와 endpoint 가 모두 등록되어 있어야 기동한다(fail-closed).
SELECT service_id || '|' || use_yn FROM OPS_SERVICE_ENDPOINT WHERE endpoint_code = :endpointCode
