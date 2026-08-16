create table CPF_BATCH_EXECUTION_CONTROL (
    cpf_execution_id varchar(80) primary key,
    job_id varchar(80) not null,
    definition_version bigint not null,
    approval_id varchar(120) not null,
    operator_id varchar(120) not null,
    reason varchar(500) not null,
    idempotency_key varchar(200) not null unique,
    fencing_token bigint not null,
    job_instance_id bigint null,
    job_execution_id bigint null,
    control_status varchar(40) not null,
    unknown_reason varchar(100) null,
    unknown_detail varchar(4000) null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    constraint CK_CPF_BAT_FENCING_POS check (fencing_token > 0)
);
create index IX_CPF_BAT_EXEC_JOB on CPF_BATCH_EXECUTION_CONTROL(job_id, definition_version, created_at);
create index IX_CPF_BAT_EXEC_SB on CPF_BATCH_EXECUTION_CONTROL(job_execution_id);
create table CPF_BATCH_EXECUTION_LINK (
    cpf_execution_id varchar(80) not null,
    link_key varchar(80) not null,
    job_id varchar(80) not null,
    definition_version bigint not null,
    spring_job_instance_id bigint not null,
    spring_job_execution_id bigint not null,
    spring_step_execution_id bigint null,
    spring_status varchar(40) not null,
    fencing_token bigint not null,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    primary key (cpf_execution_id, link_key),
    constraint FK_CPF_BAT_EXEC_LINK foreign key (cpf_execution_id) references CPF_BATCH_EXECUTION_CONTROL(cpf_execution_id)
);
create index IX_CPF_BAT_LINK_SB on CPF_BATCH_EXECUTION_LINK(spring_job_execution_id, spring_step_execution_id);
create table CPF_BATCH_APPROVED_LAUNCH (
    approval_id varchar(120) primary key,
    job_id varchar(80) not null,
    definition_version bigint not null,
    definition_checksum char(64) not null,
    approval_status varchar(20) not null,
    launch_request_json text not null,
    effective_from timestamp with time zone not null,
    effective_until timestamp with time zone null,
    approved_by varchar(120) not null,
    approved_at timestamp with time zone not null,
    row_version bigint not null default 0,
    constraint UK_CPF_BAT_APPROVED_DEF unique (job_id, definition_version, definition_checksum),
    constraint CK_CPF_BAT_APPROVAL_STATUS check (approval_status in ('APPROVED','REVOKED','EXPIRED'))
);
do $$ begin
    if to_regclass('batch_job_execution') is null or to_regclass('batch_step_execution') is null then
        raise exception 'Spring Batch JobRepository tables are required before V82';
    end if;
end $$;
