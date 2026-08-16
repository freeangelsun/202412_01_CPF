CREATE TABLE scheduled_tasks (
 task_name VARCHAR(100) NOT NULL, task_instance VARCHAR(100) NOT NULL, task_data BLOB,
 execution_time TIMESTAMP(6) NOT NULL, picked TINYINT(1) NOT NULL, picked_by VARCHAR(100),
 last_success TIMESTAMP(6) NULL, last_failure TIMESTAMP(6) NULL,
 consecutive_failures INT, last_heartbeat TIMESTAMP(6) NULL, version BIGINT NOT NULL, priority SMALLINT,
 PRIMARY KEY (task_name, task_instance), KEY scheduled_tasks_execution_time_idx (execution_time)
) ENGINE=InnoDB;
