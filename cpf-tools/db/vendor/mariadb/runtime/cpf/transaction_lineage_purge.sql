-- Purge only rows proven archived; bind :purge_before.
DELETE FROM cpf_transaction_lineage t WHERE t.occurred_at < :purge_before AND t.archived_at IS NOT NULL AND EXISTS (SELECT 1 FROM cpf_transaction_lineage_archive a WHERE a.lineage_id=t.lineage_id AND a.occurred_at=t.occurred_at);
