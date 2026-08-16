package com.cpf.education.operations.runtime.consumer;
/** Concrete integration mechanisms allowed for executable Manual EDU scenarios. */
/** EduConsumerType 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum EduConsumerType { JDBC_QUERY, JDBC_COMMAND, SPRING_BATCH, REFERENCE_GATEWAY, HTTP, FILE, PROCESS, OUTBOX }
