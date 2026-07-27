-- CPF PostgreSQL structural verification
SELECT current_database() AS current_database, current_schema() AS current_schema;
SELECT COUNT(*) AS cpf_table_count FROM information_schema.tables WHERE table_schema = current_schema() AND table_type='BASE TABLE';
