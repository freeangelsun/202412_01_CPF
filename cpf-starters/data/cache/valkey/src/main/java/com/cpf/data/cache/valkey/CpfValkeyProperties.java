package com.cpf.data.cache.valkey;
import com.cpf.data.cache.rediscommon.CpfRedisLikeProviderProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
@ConfigurationProperties("cpf.data.cache.valkey")
/** CpfValkeyProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfValkeyProperties extends CpfRedisLikeProviderProperties {
    public void validate() { super.validate("VALKEY"); }
}
