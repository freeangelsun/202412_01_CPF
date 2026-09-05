-- CPF Service Registry provisioning. 정본 계약: cpf-tools/db/canonical/service-registry-provisioning.json
-- Generated Domain 의 service 가 중앙 Registry 에 없으면 그 Runtime 은 기동할 수 없다(fail-closed).
-- 등록 주체는 cpf bootstrap 의 Platform DB provisioning lifecycle 이다. Runtime 자가 등록은 금지한다.
INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, use_yn, created_by, updated_by) VALUES (:serviceId, :serviceName, :serviceType, :ownerModuleCode, :useYn, :createdBy, :updatedBy)
