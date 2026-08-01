package com.cpf.reference.edu.runtime.consumer;
/** Concrete integration mechanisms allowed for executable Manual EDU scenarios. */
public enum EduConsumerType { JDBC_QUERY, JDBC_COMMAND, SPRING_BATCH, REFERENCE_GATEWAY, HTTP, FILE, PROCESS, OUTBOX }
