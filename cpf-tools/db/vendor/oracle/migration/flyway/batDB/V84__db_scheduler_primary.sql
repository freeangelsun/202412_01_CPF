CREATE TABLE scheduled_tasks (
 task_name VARCHAR2(100) NOT NULL, task_instance VARCHAR2(100) NOT NULL, task_data BLOB,
 execution_time TIMESTAMP WITH TIME ZONE NOT NULL, picked NUMBER(1) NOT NULL, picked_by VARCHAR2(100),
 last_success TIMESTAMP WITH TIME ZONE, last_failure TIMESTAMP WITH TIME ZONE,
 consecutive_failures NUMBER(10), last_heartbeat TIMESTAMP WITH TIME ZONE, version NUMBER(19) NOT NULL, priority NUMBER(5),
 CONSTRAINT scheduled_tasks_pk PRIMARY KEY (task_name, task_instance), CONSTRAINT scheduled_tasks_picked_ck CHECK (picked IN (0,1))
);
CREATE INDEX scheduled_tasks_execution_time_idx ON scheduled_tasks(execution_time);
