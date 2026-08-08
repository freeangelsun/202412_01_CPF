package com.cpf.starter.data.lock.valkey;
import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.starter.data.lock.valkey.internal.DefaultCpfLockManager;
import com.cpf.core.spi.locking.CpfLockAuditSink;
import com.cpf.core.spi.locking.CpfLockStore;
import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
@AutoConfiguration
@EnableConfigurationProperties(CpfValkeyLockProperties.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix="cpf.data.lock.valkey",name="enabled",havingValue="true")
public class CpfValkeyLockAutoConfiguration {
 @Bean @ConditionalOnMissingBean(CpfLockStore.class) CpfLockStore cpfValkeyLockStore(StringRedisTemplate redis,CpfValkeyLockProperties p){return new ValkeyCpfLockStore(redis,p.getNamespace(),p.getCasRetries());}
 @Bean @ConditionalOnMissingBean(CpfLockAuditSink.class) CpfLockAuditSink cpfLockAuditSink(){return CpfLockAuditSink.unavailable();}
 @Bean @ConditionalOnMissingBean(CpfLockManager.class) CpfLockManager cpfLockManager(CpfLockStore store,CpfLockAuditSink audit){return new DefaultCpfLockManager(store,audit,Clock.systemUTC());}
}
