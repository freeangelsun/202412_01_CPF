package com.cpf.data.persistence.transaction;
import com.cpf.data.persistence.api.annotation.CpfTx;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.transaction.PlatformTransactionManager;
import java.util.Map;
/** Data Transaction Runtime이 존재할 때 @CpfTx를 한 번만 실제 Transaction으로 활성화합니다. */
@AutoConfiguration @ConditionalOnClass({CpfTx.class,Advisor.class,PlatformTransactionManager.class}) @ConditionalOnBean(PlatformTransactionManager.class)
public class CpfTxAutoConfiguration {
 @Bean @Role(BeanDefinition.ROLE_INFRASTRUCTURE) Advisor cpfTxAdvisor(Map<String, PlatformTransactionManager> txManagers){return new DefaultPointcutAdvisor(new CpfTxPointcut(),new CpfTxMethodInterceptor(txManagers));}
}
