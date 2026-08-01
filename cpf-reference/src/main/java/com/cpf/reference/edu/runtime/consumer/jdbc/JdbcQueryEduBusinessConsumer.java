package com.cpf.reference.edu.runtime.consumer.jdbc;
import com.cpf.reference.edu.runtime.consumer.*;
import com.cpf.reference.edu.runtime.model.EduExecutionCommand;
/** Separate registry key for read-only JDBC scenarios while sharing the actual JDBC implementation. */
public final class JdbcQueryEduBusinessConsumer implements EduBusinessConsumer {
    private final JdbcEduBusinessConsumer delegate;
    public JdbcQueryEduBusinessConsumer(JdbcEduBusinessConsumer delegate){this.delegate=delegate;}
    @Override public EduConsumerType type(){return EduConsumerType.JDBC_QUERY;}
    @Override public EduBusinessConsumerResult invoke(EduConsumerBinding b,EduExecutionCommand c,long f){return delegate.query(b,c,f);}
}
