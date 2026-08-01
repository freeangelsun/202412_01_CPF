-- Must return six rows; absence fails the lifecycle gate.
SELECT table_name FROM information_schema.tables WHERE table_schema=database() AND upper(table_name) IN ('CPF_EDU_OPERATION','CPF_EDU_OPERATION_TARGET','CPF_EDU_OPERATION_AUDIT','CPF_EDU_OUTBOX','CPF_EDU_LEASE','CPF_EDU_BUSINESS_RECORD') ORDER BY table_name;
SELECT index_name FROM information_schema.statistics WHERE table_schema=database() AND upper(index_name) IN ('IX_CPF_EDU_OPERATION_STATE','IX_CPF_EDU_OUTBOX_READY','IX_CPF_EDU_BUSINESS_STATE','IX_CPF_EDU_BUSINESS_FENCE') GROUP BY index_name ORDER BY index_name;

-- REF-owned independent counterparty simulator persistence
SELECT COUNT(*) AS CPF_EDU_COUNTERPARTY_REQUEST_COUNT FROM CPF_EDU_COUNTERPARTY_REQUEST;
