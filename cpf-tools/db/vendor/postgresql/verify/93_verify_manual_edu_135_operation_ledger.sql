-- Must return six rows; absence fails the lifecycle gate.
SELECT table_name FROM information_schema.tables WHERE table_schema=current_schema() AND upper(table_name) IN ('CPF_EDU_OPERATION','CPF_EDU_OPERATION_TARGET','CPF_EDU_OPERATION_AUDIT','CPF_EDU_OUTBOX','CPF_EDU_LEASE','CPF_EDU_BUSINESS_RECORD') ORDER BY table_name;
SELECT indexname FROM pg_indexes WHERE schemaname=current_schema() AND upper(indexname) IN ('IX_CPF_EDU_OPERATION_STATE','IX_CPF_EDU_OUTBOX_READY','IX_CPF_EDU_BUSINESS_STATE','IX_CPF_EDU_BUSINESS_FENCE') ORDER BY indexname;

-- REF-owned independent counterparty simulator persistence
SELECT COUNT(*) AS CPF_EDU_COUNTERPARTY_REQUEST_COUNT FROM CPF_EDU_COUNTERPARTY_REQUEST;
