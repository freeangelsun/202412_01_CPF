package com.cpf.batch.control.compat;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import com.cpf.core.api.execution.CpfSharedApi;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

/**
 * ADM이 분리 WAS topology에서 사용하는 BAT Center-Cut 조회 전용 Owner API입니다.
 */
@RestController
@RequestMapping("/bat/internal/center-cut")
@CpfSharedApi(
        id = "SBATCT0001",
        name = "BATCenterCutOperations",
        ownerDomain = "BAT",
        description = "BAT standard Center-Cut operational read contract",
        allowedCallers = {"ADM"})
public final class BatCenterCutInternalController {
    private final BatCenterCutOperationsService operations;
    private final BatVerifiedActorResolver actorResolver;

    public BatCenterCutInternalController(
            BatCenterCutOperationsService operations,
            BatVerifiedActorResolver actorResolver) {
        this.operations = operations;
        this.actorResolver = actorResolver;
    }

    @PostMapping("/{operation}")
    public Object invoke(
            @PathVariable String operation,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest request) {
        requireAdm(request);
        Map<String, Object> values = payload == null ? Map.of() : payload;
        return switch (operation) {
            case "findJobs" -> {
                requireOnly(values, Set.of());
                yield operations.findJobs();
            }
            case "findJobDetail" -> {
                requireOnly(values, Set.of("centerCutJobId"));
                yield operations.findJobDetail(requiredText(values, "centerCutJobId"));
            }
            case "findParameters" -> {
                requireOnly(values, Set.of("centerCutJobId"));
                yield operations.findParameters(requiredText(values, "centerCutJobId"));
            }
            case "findSummary" -> {
                requireOnly(values, Set.of("centerCutJobId"));
                yield operations.findSummary(requiredText(values, "centerCutJobId"));
            }
            case "findTargets" -> {
                requireOnly(values, Set.of("centerCutJobId", "statusCode", "limit"));
                yield operations.findTargets(
                        requiredText(values, "centerCutJobId"),
                        optionalText(values, "statusCode"),
                        limit(values));
            }
            case "findResults" -> {
                requireOnly(values, Set.of("centerCutJobId", "resultStatus", "limit"));
                yield operations.findResults(
                        requiredText(values, "centerCutJobId"),
                        optionalText(values, "resultStatus"),
                        limit(values));
            }
            case "findResultDetail" -> {
                requireOnly(values, Set.of("resultId"));
                yield operations.findResultDetail(requiredText(values, "resultId"));
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported BAT Center-Cut operation: " + operation);
        };
    }

    private void requireAdm(HttpServletRequest request) {
        var identity = actorResolver.identity(request);
        if (!"ADM".equalsIgnoreCase(identity.callerService())) {
            throw new AccessDeniedException(
                    "BAT Center-Cut Owner API only accepts authenticated ADM callers");
        }
    }

    private static void requireOnly(Map<String, Object> values, Set<String> allowed) {
        Set<String> unexpected = values.keySet().stream()
                .filter(key -> !allowed.contains(key))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (!unexpected.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unsupported BAT Center-Cut request fields: " + unexpected);
        }
    }

    private static String requiredText(Map<String, Object> values, String key) {
        String value = optionalText(values, key);
        if (value == null) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private static String optionalText(Map<String, Object> values, String key) {
        Object value = values.get(key);
        return value == null || String.valueOf(value).isBlank()
                ? null
                : String.valueOf(value).trim();
    }

    private static int limit(Map<String, Object> values) {
        Object value = values.get("limit");
        if (value == null) {
            return BatCenterCutOperationsService.DEFAULT_LIMIT;
        }
        int parsed;
        try {
            parsed = Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException failure) {
            throw new IllegalArgumentException("limit must be a number", failure);
        }
        if (parsed < 1 || parsed > BatCenterCutOperationsService.MAX_LIMIT) {
            throw new IllegalArgumentException(
                    "limit must be between 1 and "
                            + BatCenterCutOperationsService.MAX_LIMIT);
        }
        return parsed;
    }
}
