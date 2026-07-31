create table CPF_BATCH_EXECUTION_CONTROL (
    cpf_execution_id varchar(80) primary key,
    job_id varchar(80) not null,
    definition_version bigint not null,
    approval_id varchar(120) not null,
    operator_id varchar(120) not null,
    reason varchar(500) not null,
    idempotency_key varchar(200) not null unique,
    fencing_token bigint not null,
    job_instance_id bigint,
    job_execution_id bigint,
    control_status varchar(40) not null,
    unknown_reason varchar(100),
    unknown_detail varchar(4000),
    created_at timestamp(6) not null default current_timestamp(6),
    updated_at timestamp(6) not null default current_timestamp(6) on update current_timestamp(6),
    constraint CK_CPF_BAT_FENCING_POS check (fencing_token > 0),
    key IX_CPF_BAT_EXEC_JOB (job_id, definition_version, created_at),
    key IX_CPF_BAT_EXEC_SB (job_execution_id)
) engine=InnoDB;
create table CPF_BATCH_EXECUTION_LINK (
    cpf_execution_id varchar(80) not null,
    link_key varchar(80) not null,
    job_id varchar(80) not null,
    definition_version bigint not null,
    spring_job_instance_id bigint not null,
    spring_job_execution_id bigint not null,
    spring_step_execution_id bigint,
    spring_status varchar(40) not null,
    fencing_token bigint not null,
    created_at timestamp(6) not null default current_timestamp(6),
    updated_at timestamp(6) not null default current_timestamp(6) on update current_timestamp(6),
    primary key (cpf_execution_id, link_key),
    key IX_CPF_BAT_LINK_SB (spring_job_execution_id, spring_step_execution_id),
    constraint FK_CPF_BAT_EXEC_LINK foreign key (cpf_execution_id) references CPF_BATCH_EXECUTION_CONTROL(cpf_execution_id)
) engine=InnoDB;
create table CPF_BATCH_APPROVED_LAUNCH (
    approval_id varchar(120) primary key,
    job_id varchar(80) not null,
    definition_version bigint not null,
    definition_checksum char(64) not null,
    approval_status varchar(20) not null,
    launch_request_json longtext not null,
    effective_from timestamp(6) not null,
    effective_until timestamp(6),
    approved_by varchar(120) not null,
    approved_at timestamp(6) not null,
    row_version bigint not null default 0,
    unique key UK_CPF_BAT_APPROVED_DEF (job_id, definition_version, definition_checksum),
    constraint CK_CPF_BAT_APPROVAL_STATUS check (approval_status in ('APPROVED','REVOKED','EXPIRED'))
) engine=InnoDB;
set @cpf_batch_metadata_count := (select count(*) from information_schema.tables where table_schema = database() and table_name in ('BATCH_JOB_EXECUTION','BATCH_STEP_EXECUTION'));
set @cpf_batch_assert_sql := if(@cpf_batch_metadata_count = 2, 'select 1', 'signal sqlstate ''45000'' set message_text = ''Spring Batch JobRepository tables are required before V82''');
prepare cpf_batch_assert_stmt from @cpf_batch_assert_sql;
execute cpf_batch_assert_stmt;
deallocate prepare cpf_batch_assert_stmt;
