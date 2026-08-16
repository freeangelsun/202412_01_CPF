USE batDB;
SELECT table_name FROM information_schema.tables WHERE table_schema='batDB' AND table_name IN ('bat_runtime_instance','bat_scheduler_lease','bat_center_cut_claim','bat_deployment_plan','bat_deployment_instance');
SELECT column_name FROM information_schema.columns WHERE table_schema='batDB' AND table_name='bat_execution_lease' AND column_name='fencing_token';

SELECT COUNT(*) AS runtime_command_attempt_table FROM information_schema.tables WHERE table_schema='batDB' AND table_name='bat_runtime_command_attempt';
SELECT COUNT(*) AS deployment_execution_table FROM information_schema.tables WHERE table_schema='batDB' AND table_name='bat_deployment_execution';
SELECT COUNT(*) AS deployment_instance_result_table FROM information_schema.tables WHERE table_schema='batDB' AND table_name='bat_deployment_instance_result';
SELECT COUNT(*) AS center_cut_execution_table FROM information_schema.tables WHERE table_schema='batDB' AND table_name='bat_center_cut_execution';
SELECT COUNT(*) AS center_cut_rate_window_table FROM information_schema.tables WHERE table_schema='batDB' AND table_name='bat_center_cut_rate_window';
SELECT COUNT(*) AS center_cut_execution_column FROM information_schema.columns WHERE table_schema='batDB' AND table_name='bat_center_cut_item' AND column_name='center_cut_execution_id';
