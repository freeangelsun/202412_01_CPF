package com.cpf.core.common.transaction;

import com.cpf.core.common.logging.CpfTransaction;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Spring MVC mapping과 CPF 온라인 실행 annotation을 스캔해 CPF 거래 메타를 갱신합니다.
 *
 * <p>거래 메타 scan은 운영 편의 기능이므로 DB 미적용, 권한 부족, 부분 mapping 오류가
 * 있어도 서비스 기동 자체를 실패시키지 않습니다. 실패 상태는 결과와 로그로 남기고 ADM에서
 * 재스캔할 수 있게 합니다.</p>
 */
public class CpfTransactionMetaScanner {
    private static final Logger log = LoggerFactory.getLogger(CpfTransactionMetaScanner.class);

    private final RequestMappingHandlerMapping handlerMapping;
    private final CpfTransactionMetaRepository repository;

    public CpfTransactionMetaScanner(
            RequestMappingHandlerMapping handlerMapping,
            CpfTransactionMetaRepository repository) {
        this.handlerMapping = handlerMapping;
        this.repository = repository;
    }

    public CpfTransactionMetaScanResult scanAndUpsert(String requestUser) {
        if (!repository.tableAvailable()) {
            return new CpfTransactionMetaScanResult(false, 0, 0, 0, List.of(), "cpf_transaction_meta 테이블을 사용할 수 없습니다.");
        }
        try {
            List<CpfTransactionMeta> metas = detect();
            int upserted = repository.upsertAll(metas, requestUser);
            int inactivated = repository.markMissingInactive(
                    metas.stream().map(CpfTransactionMeta::transactionId).toList(),
                    requestUser);
            return new CpfTransactionMetaScanResult(
                    true,
                    metas.size(),
                    upserted,
                    inactivated,
                    metas.stream().map(CpfTransactionMeta::transactionId).distinct().sorted().toList(),
                    "거래 메타 scan을 완료했습니다.");
        } catch (Exception ex) {
            log.warn("CPF 거래 메타 scan을 건너뜁니다. message={}", ex.getMessage());
            return new CpfTransactionMetaScanResult(false, 0, 0, 0, List.of(), ex.getMessage());
        }
    }

    public List<CpfTransactionMeta> detect() {
        List<CpfTransactionMeta> metas = new ArrayList<>();
        handlerMapping.getHandlerMethods().forEach((info, handlerMethod) -> {
            OnlineMetadata transaction = findTransaction(handlerMethod);
            if (transaction == null) {
                return;
            }
            for (String path : paths(info)) {
                for (String httpMethod : methods(info)) {
                    metas.add(toMeta(transaction, info, handlerMethod, httpMethod, path));
                }
            }
        });
        return metas.stream()
                .sorted(Comparator.comparing(CpfTransactionMeta::transactionId)
                        .thenComparing(CpfTransactionMeta::apiPath)
                        .thenComparing(CpfTransactionMeta::httpMethod))
                .toList();
    }

    private OnlineMetadata findTransaction(HandlerMethod handlerMethod) {
        CpfOnlineTransaction standard = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), CpfOnlineTransaction.class);
        if (standard == null) {
            standard = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), CpfOnlineTransaction.class);
        }
        if (standard != null) {
            return new OnlineMetadata(standard.id(), standard.name());
        }
        CpfTransaction methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), CpfTransaction.class);
        if (methodAnnotation != null) {
            return new OnlineMetadata(methodAnnotation.id(), methodAnnotation.name());
        }
        CpfTransaction typeAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), CpfTransaction.class);
        return typeAnnotation == null ? null : new OnlineMetadata(typeAnnotation.id(), typeAnnotation.name());
    }

    private CpfTransactionMeta toMeta(
            OnlineMetadata transaction,
            RequestMappingInfo info,
            HandlerMethod handlerMethod,
            String httpMethod,
            String path) {
        String transactionId = transaction.id().trim();
        String controllerClass = handlerMethod.getBeanType().getName();
        String handlerName = handlerMethod.getMethod().getName();
        return new CpfTransactionMeta(
                transactionId,
                transaction.name().trim(),
                moduleCode(transactionId, controllerClass),
                domainCode(transactionId),
                httpMethod,
                path,
                controllerClass,
                handlerName,
                handlerMethod.getBeanType().getSimpleName() + "." + handlerName,
                null,
                "N",
                null,
                "Y");
    }

    private List<String> paths(RequestMappingInfo info) {
        Set<String> paths = new TreeSet<>();
        if (info.getPathPatternsCondition() != null) {
            paths.addAll(info.getPathPatternsCondition().getPatternValues());
        }
        if (info.getPatternsCondition() != null) {
            paths.addAll(info.getPatternsCondition().getPatterns());
        }
        if (paths.isEmpty()) {
            paths.add("/");
        }
        return List.copyOf(paths);
    }

    private List<String> methods(RequestMappingInfo info) {
        Set<RequestMethod> requestMethods = info.getMethodsCondition().getMethods();
        if (requestMethods == null || requestMethods.isEmpty()) {
            return List.of("ANY");
        }
        return requestMethods.stream().map(RequestMethod::name).sorted().toList();
    }

    private String moduleCode(String transactionId, String controllerClass) {
        if (transactionId != null && transactionId.matches("^[OB][A-Z]{3}-.*")) {
            return transactionId.substring(1, 4);
        }
        if (transactionId != null && transactionId.length() >= 3) {
            return transactionId.substring(0, 3).toUpperCase();
        }
        String[] packages = controllerClass.split("\\.");
        return packages.length > 1 ? packages[1].toUpperCase() : "CPF";
    }

    private String domainCode(String transactionId) {
        if (transactionId != null && transactionId.matches("^[OB][A-Z]{3}-[A-Z0-9]{3}-.*")) {
            return transactionId.substring(5, 8);
        }
        if (transactionId != null && transactionId.length() >= 8) {
            return transactionId.substring(5, 8).toUpperCase();
        }
        return null;
    }

    private record OnlineMetadata(String id, String name) {
    }
}
