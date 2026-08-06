SELECT COUNT(*) AS reconciliation_audit_table_count FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='bat_reconciliation_audit';
