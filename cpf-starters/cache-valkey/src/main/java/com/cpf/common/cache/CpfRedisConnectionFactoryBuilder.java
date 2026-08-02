package com.cpf.common.cache;

import com.cpf.core.api.security.secret.CpfSecretProvider;
import com.cpf.core.api.security.secret.CpfSecretReference;
import com.cpf.core.api.security.secret.CpfSecretValue;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import java.util.List;

/** Spring Boot auto-configured raw client 대신 CPF Security 정책으로 Lettuce ConnectionFactory를 생성합니다. */
final class CpfRedisConnectionFactoryBuilder {
    private CpfRedisConnectionFactoryBuilder() { }

    static LettuceConnectionFactory build(CpfRedisProperties properties, CpfSecretProvider secretProvider) {
        properties.validate();
        RedisConfiguration configuration = switch (properties.getTopology()) {
            case STANDALONE -> standalone(properties);
            case SENTINEL -> sentinel(properties);
            case CLUSTER -> cluster(properties);
        };
        char[] password = resolvePassword(properties, secretProvider);
        try {
            applyCredential(configuration, properties.getUsername(), password);
            LettuceClientConfiguration.LettuceClientConfigurationBuilder client =
                    LettuceClientConfiguration.builder()
                            .commandTimeout(properties.getCommandTimeout())
                            .shutdownTimeout(properties.getShutdownTimeout());
            if (properties.isTls()) client.useSsl();
            LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration, client.build());
            factory.setValidateConnection(true);
            return factory;
        } finally {
            if (password != null) java.util.Arrays.fill(password, '\0');
        }
    }

    private static RedisStandaloneConfiguration standalone(CpfRedisProperties p) {
        HostPort node = HostPort.parse(p.getNodes().getFirst());
        RedisStandaloneConfiguration c = new RedisStandaloneConfiguration(node.host(), node.port());
        c.setDatabase(p.getDatabase());
        return c;
    }

    private static RedisSentinelConfiguration sentinel(CpfRedisProperties p) {
        RedisSentinelConfiguration c = new RedisSentinelConfiguration();
        c.master(p.getMaster());
        p.getNodes().stream().map(HostPort::parse)
                .forEach(node -> c.sentinel(node.host(), node.port()));
        c.setDatabase(p.getDatabase());
        return c;
    }

    private static RedisClusterConfiguration cluster(CpfRedisProperties p) {
        return new RedisClusterConfiguration(p.getNodes());
    }

    private static char[] resolvePassword(CpfRedisProperties p, CpfSecretProvider provider) {
        if (p.getSecretReference().isBlank()) return null;
        String[] parts = p.getSecretReference().split(":", 2);
        if (parts.length != 2) throw new IllegalStateException("Redis secret-reference는 provider:key 형식이어야 합니다.");
        if (provider == null || !provider.providerId().equalsIgnoreCase(parts[0])) {
            throw new IllegalStateException("Redis Secret Provider를 찾을 수 없습니다: " + parts[0]);
        }
        try (CpfSecretValue value = provider.resolve(new CpfSecretReference(parts[0], parts[1]))) {
            return value.copy();
        }
    }

    private static void applyCredential(RedisConfiguration configuration, String username, char[] password) {
        if (configuration instanceof RedisConfiguration.WithAuthentication auth) {
            if (username != null && !username.isBlank()) auth.setUsername(username);
            if (password != null && password.length > 0) auth.setPassword(RedisPassword.of(password));
        }
    }

    private record HostPort(String host, int port) {
        static HostPort parse(String value) {
            if (value == null || value.isBlank()) throw new IllegalArgumentException("Redis node는 host:port 형식이어야 합니다.");
            String node=value.trim();
            int separator=node.startsWith("[") ? node.indexOf("]:") + 1 : node.lastIndexOf(':');
            if (separator <= 0 || separator >= node.length()-1) throw new IllegalArgumentException("Redis node는 host:port 형식이어야 합니다.");
            String host=node.startsWith("[") ? node.substring(1,separator-1) : node.substring(0,separator);
            if (host.isBlank() || host.chars().anyMatch(Character::isWhitespace)) throw new IllegalArgumentException("Redis host 형식이 올바르지 않습니다.");
            int port = Integer.parseInt(node.substring(separator+1));
            if (port < 1 || port > 65535) throw new IllegalArgumentException("Redis port 범위가 올바르지 않습니다.");
            return new HostPort(host, port);
        }
    }
}
