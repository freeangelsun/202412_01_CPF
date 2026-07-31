CREATE TABLE scheduled_tasks (
 task_name VARCHAR(100) NOT NULL, task_instance VARCHAR(100) NOT NULL, task_data BYTEA,
 execution_time TIMESTAMP WITH TIME ZONE NOT NULL, picked BOOLEAN NOT NULL, picked_by VARCHAR(100),
 last_success TIMESTAMP WITH TIME ZONE, last_failure TIMESTAMP WITH TIME ZONE,
 consecutive_failures INTEGER, last_heartbeat TIMESTAMP WITH TIME ZONE, version BIGINT NOT NULL, priority SMALLINT,
 PRIMARY KEY (task_name, task_instance)
);
CREATE INDEX scheduled_tasks_execution_time_idx ON scheduled_tasks(execution_time);
