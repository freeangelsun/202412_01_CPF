package com.cpf.local.batch;

import com.cpf.batch.agent.BatchHostAgentApplication;
import com.cpf.batch.centercut.runner.CenterCutRunnerApplication;
import com.cpf.batch.control.BatchControlServerApplication;
import com.cpf.batch.scheduler.BatchSchedulerApplication;
import com.cpf.batch.worker.BatchWorkerApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Scheduler·Worker·Center-Cut·Control Server·Agent를 하나의 개발 JVM에서 역할별 독립
 * Spring Context로 실행하는 Local Batch Launcher입니다.
 *
 * <p>각 역할은 자신의 {@code RuntimeRegistration}, DataSource, Policy Consumer를 유지하므로
 * 여러 {@code @SpringBootApplication}을 한 Context에 억지로 합쳐 Bean 충돌이나 Identity
 * 오염을 만들지 않습니다. 기본 Control/Scheduler/Worker는 한 JVM에서 기동하고,
 * Center-Cut과 Host-Agent는 명시적으로 선택합니다.</p>
 */
public final class CpfLocalBatchRuntimeApplication {
    private CpfLocalBatchRuntimeApplication() {
    }

    public static void main(String[] args) throws InterruptedException {
        StandardEnvironment bootstrapEnvironment = new StandardEnvironment();
        bootstrapEnvironment.getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        CpfLocalBatchRuntimeSafetyGuard.validate(bootstrapEnvironment);

        int controlPort = intProperty(bootstrapEnvironment, "cpf.local.batch.ports.control-server", 8090);
        int schedulerPort = intProperty(bootstrapEnvironment, "cpf.local.batch.ports.scheduler", 8091);
        int workerPort = intProperty(bootstrapEnvironment, "cpf.local.batch.ports.worker", 8092);
        int centerCutPort = intProperty(bootstrapEnvironment, "cpf.local.batch.ports.center-cut", 8093);
        int hostAgentPort = intProperty(bootstrapEnvironment, "cpf.local.batch.ports.host-agent", 8094);
        String controlBaseUrl = "http://127.0.0.1:" + controlPort;

        Map<String, Boolean> roles = new LinkedHashMap<>();
        roles.put("control-server", boolProperty(bootstrapEnvironment, "cpf.local.batch.modules.control-server", true));
        roles.put("scheduler", boolProperty(bootstrapEnvironment, "cpf.local.batch.modules.scheduler", true));
        roles.put("worker", boolProperty(bootstrapEnvironment, "cpf.local.batch.modules.worker", true));
        roles.put("center-cut", boolProperty(bootstrapEnvironment, "cpf.local.batch.modules.center-cut", false));
        roles.put("host-agent", boolProperty(bootstrapEnvironment, "cpf.local.batch.modules.host-agent", false));
        if (roles.values().stream().noneMatch(Boolean::booleanValue)) {
            throw new IllegalStateException("최소 하나의 Local Batch 역할을 활성화해야 합니다.");
        }

        List<ConfigurableApplicationContext> contexts = new ArrayList<>();
        try {
            if (roles.get("control-server")) {
                contexts.add(startWebRole(
                        BatchControlServerApplication.class,
                        controlPort,
                        roles,
                        controlBaseUrl,
                        args,
                        CpfLocalBatchRuntimeStatusController.class));
            }
            if (roles.get("scheduler")) {
                contexts.add(startWebRole(
                        BatchSchedulerApplication.class,
                        schedulerPort,
                        roles,
                        controlBaseUrl,
                        args));
            }
            if (roles.get("worker")) {
                contexts.add(startWebRole(
                        BatchWorkerApplication.class,
                        workerPort,
                        roles,
                        controlBaseUrl,
                        args));
            }
            if (roles.get("center-cut")) {
                contexts.add(startWebRole(
                        CenterCutRunnerApplication.class,
                        centerCutPort,
                        roles,
                        controlBaseUrl,
                        args));
            }
            if (roles.get("host-agent")) {
                contexts.add(startWebRole(
                        BatchHostAgentApplication.class,
                        hostAgentPort,
                        roles,
                        controlBaseUrl,
                        args));
            }
        } catch (RuntimeException startFailure) {
            closeReverse(contexts);
            throw startFailure;
        }

        CountDownLatch shutdown = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            closeReverse(contexts);
            shutdown.countDown();
        }, "cpf-local-batch-shutdown"));
        shutdown.await();
    }

    private static ConfigurableApplicationContext startWebRole(
            Class<?> applicationClass,
            int port,
            Map<String, Boolean> roles,
            String controlBaseUrl,
            String[] args,
            Class<?>... additionalSources) {
        List<Class<?>> sources = new ArrayList<>();
        sources.add(applicationClass);
        sources.addAll(List.of(additionalSources));
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("cpf.local.batch.enabled", "true");
        defaults.put("cpf.environment", "local");
        defaults.put("server.address", "127.0.0.1");
        defaults.put("server.port", Integer.toString(port));
        defaults.put("cpf.batch.control.base-url", controlBaseUrl);
        defaults.put("spring.main.banner-mode", "off");
        roles.forEach((role, enabled) -> defaults.put("cpf.local.batch.modules." + role, enabled.toString()));

        String[] roleArgs = java.util.Arrays.stream(args)
                .filter(argument -> !argument.startsWith("--server.port="))
                .toArray(String[]::new);
        return new SpringApplicationBuilder(sources.toArray(Class<?>[]::new))
                .web(WebApplicationType.SERVLET)
                .profiles("local")
                .properties(defaults)
                .initializers(new CpfLocalBatchRuntimeSafetyGuard())
                .run(roleArgs);
    }

    private static boolean boolProperty(StandardEnvironment environment, String key, boolean fallback) {
        return environment.getProperty(key, Boolean.class, fallback);
    }

    private static int intProperty(StandardEnvironment environment, String key, int fallback) {
        int value = environment.getProperty(key, Integer.class, fallback);
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException("유효하지 않은 Port입니다. key=" + key + ", value=" + value);
        }
        return value;
    }

    private static void closeReverse(List<ConfigurableApplicationContext> contexts) {
        for (int index = contexts.size() - 1; index >= 0; index--) {
            try {
                contexts.get(index).close();
            } catch (RuntimeException ignored) {
                // 다른 역할 Context 종료를 계속 수행합니다.
            }
        }
    }
}
