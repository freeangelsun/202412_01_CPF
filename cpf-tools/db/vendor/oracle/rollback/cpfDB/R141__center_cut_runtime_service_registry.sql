-- CPF R141: recovery-only removal of the CEC(Center-Cut Runner) Runtime Service identity.
-- Reverses V141 exactly; no other registry row is touched.

DELETE FROM OPS_SERVICE_INSTANCE WHERE service_id = 'CEC';
DELETE FROM OPS_SERVICE_ROUTING_POLICY WHERE service_id = 'CEC' AND endpoint_code = 'CEC_API';
DELETE FROM OPS_SERVICE_ENDPOINT WHERE endpoint_code = 'CEC_API' AND service_id = 'CEC';
DELETE FROM OPS_SERVICE WHERE service_id = 'CEC';
