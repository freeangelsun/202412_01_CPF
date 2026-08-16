create table CPF_BATCH_EXECUTION_CONTROL (
    cpf_execution_id varchar2(80 char) primary key,
    job_id varchar2(80 char) not null,
    definition_version number(19) not null,
    approval_id varchar2(120 char) not null,
    operator_id varchar2(120 char) not null,
    reason varchar2(500 char) not null,
    idempotency_key varchar2(200 char) not null unique,
    fencing_token number(19) not null,
    job_instance_id number(19),
    job_execution_id number(19),
    control_status varchar2(40 char) not null,
    unknown_reason varchar2(100 char),
    unknown_detail varchar2(4000 char),
    created_at timestamp with time zone default systimestamp not null,
    updated_at timestamp with time zone default systimestamp not null,
    constraint CK_CPF_BAT_FENCING_POS check (fencing_token > 0)
);
create index IX_CPF_BAT_EXEC_JOB on CPF_BATCH_EXECUTION_CONTROL(job_id, definition_version, created_at);
create index IX_CPF_BAT_EXEC_SB on CPF_BATCH_EXECUTION_CONTROL(job_execution_id);
create table CPF_BATCH_EXECUTION_LINK (
    cpf_execution_id varchar2(80 char) not null,
    link_key varchar2(80 char) not null,
    job_id varchar2(80 char) not null,
    definition_version number(19) not null,
    spring_job_instance_id number(19) not null,
    spring_job_execution_id number(19) not null,
    spring_step_execution_id number(19),
    spring_status varchar2(40 char) not null,
    fencing_token number(19) not null,
    created_at timestamp with time zone default systimestamp not null,
    updated_at timestamp with time zone default systimestamp not null,
    constraint PK_CPF_BATCH_EXECUTION_LINK primary key (cpf_execution_id, link_key),
    constraint FK_CPF_BAT_EXEC_LINK foreign key (cpf_execution_id) references CPF_BATCH_EXECUTION_CONTROL(cpf_execution_id)
);
create index IX_CPF_BAT_LINK_SB on CPF_BATCH_EXECUTION_LINK(spring_job_execution_id, spring_step_execution_id);
create table CPF_BATCH_APPROVED_LAUNCH (
    approval_id varchar2(120 char) primary key,
    job_id varchar2(80 char) not null,
    definition_version number(19) not null,
    definition_checksum char(64 char) not null,
    approval_status varchar2(20 char) not null,
    launch_request_json clob not null,
    effective_from timestamp with time zone not null,
    effective_until timestamp with time zone,
    approved_by varchar2(120 char) not null,
    approved_at timestamp with time zone not null,
    row_version number(19) default 0 not null,
    constraint UK_CPF_BAT_APPROVED_DEF unique (job_id, definition_version, definition_checksum),
    constraint CK_CPF_BAT_APPROVAL_STATUS check (approval_status in ('APPROVED','REVOKED','EXPIRED'))
);
declare
    v_count number;
begin
    select count(*) into v_count from user_tables where table_name in ('BATCH_JOB_EXECUTION','BATCH_STEP_EXECUTION');
    if v_count <> 2 then raise_application_error(-20082, 'Spring Batch JobRepository tables are required before V82'); end if;
end;
/
