package com.cpf.education.operations.runtime.consumer.jdbc;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.EduExecutionCommand;
/** Separate registry key for read-only JDBC scenarios while sharing the actual JDBC implementation. */
/** JdbcQueryEduBusinessConsumer 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class JdbcQueryEduBusinessConsumer implements EduBusinessConsumer {
    private final JdbcEduBusinessConsumer delegate;
    public JdbcQueryEduBusinessConsumer(JdbcEduBusinessConsumer delegate){this.delegate=delegate;}
    @Override public EduConsumerType type(){return EduConsumerType.JDBC_QUERY;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long f){return delegate.query(b,c,f);}
}
