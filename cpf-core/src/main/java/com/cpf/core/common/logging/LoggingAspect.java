package com.cpf.core.common.logging;

import com.cpf.core.common.system.CpfSystemCodes;
import com.cpf.core.common.exception.DefaultCpfMessageResolver;
import com.cpf.core.common.exception.DefaultCpfResponseCodeResolver;
import com.cpf.core.common.exception.CpfException;
import com.cpf.core.common.exception.CpfMessageResolver;
import com.cpf.core.common.exception.CpfResolvedResponse;
import com.cpf.core.common.exception.CpfResponseCodeResolver;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.execution.CpfSharedApi;
import com.cpf.core.common.header.CpfHeaderAuditLogger;
import com.cpf.core.common.header.CpfHeaderPropagator;
import com.cpf.core.common.header.CpfHeaderSnapshot;
import com.cpf.core.common.header.CpfTrustedProxyPolicy;
import com.cpf.core.common.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.policy.LogPolicyResolver;
import com.cpf.core.common.logging.policy.LogPolicyTargetType;
import com.cpf.core.common.workflow.CpfWorkflow;
import com.cpf.core.common.workflow.CpfWorkflowContext;
import com.cpf.core.common.workflow.CpfWorkflowFailurePolicy;
import com.cpf.core.common.workflow.CpfWorkflowMetadata;
import com.cpf.core.common.workflow.CpfWorkflowStatus;
import com.cpf.core.common.workflow.CpfWorkflowStep;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    private final ApplicationEventPublisher eventPublisher;
    private final Environment environment;
    private final DynamicTransactionLogLevelService dynamicLogLevelService;
    private final CpfMessageResolver messageResolver;
    private final CpfResponseCodeResolver responseCodeResolver;
    private final ObjectProvider<LogPolicyResolver> logPolicyResolverProvider;

    public LoggingAspect(
            ApplicationEventPublisher eventPublisher,
            Environment environment,
            DynamicTransactionLogLevelService dynamicLogLevelService,
            ObjectProvider<CpfMessageResolver> messageResolverProvider,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider) {
        this.eventPublisher = eventPublisher;
        this.environment = environment;
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.messageResolver = messageResolverProvider.getIfAvailable(DefaultCpfMessageResolver::new);
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultCpfResponseCodeResolver::new);
        this.logPolicyResolverProvider = logPolicyResolverProvider;
    }

    @Around("execution(* com.cpf..*Controller.*(..))")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        long startNanos = System.nanoTime();
        HttpServletRequest request = currentRequest();

        String transactionId = TransactionContext.getOrCreateTransactionId();
        String traceId = TransactionContext.getOrCreateTraceId();
        String spanId = TransactionContext.getOrCreateSpanId();
        String parentSpanId = TransactionContext.currentParentSpanId();
        int sequenceNo = TransactionContext.nextSequenceNo();
        TransactionHeader transactionHeader = TransactionContext.currentHeader();
        OnlineExecutionMetadata onlineExecution = resolveOnlineExecution(joinPoint);

        String moduleId = resolveModuleId(joinPoint);
        String controller = joinPoint.getSignature().toShortString();
        ExecutionMetadata executionMetadata = resolveExecutionMetadata(joinPoint);
        String httpMethod = request != null ? request.getMethod() : "N/A";
        String uri = request != null ? buildRequestUri(request) : controller;
        String parameters = request != null ? requestParameters(request) : "N/A";
        String requestBody = serializeArgs(joinPoint.getArgs());
        String execUser = resolveExecUser(request);
        String clientIp = firstText(
                headerValue(transactionHeader, TransactionHeader::getClientIp),
                request != null ? clientIp(request) : null,
                "N/A");
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String businessTransactionId = onlineExecution != null ? onlineExecution.id() : "UNKNOWN";
        String businessTransactionName = onlineExecution != null ? onlineExecution.name() : controller;
        LogPolicyDecision logPolicy = resolveOnlineLogPolicy(businessTransactionId);
        if (!logPolicy.requestBodySave()) {
            requestBody = null;
        }
        String menuId = firstText(request != null ? request.getParameter("menuId") : null, businessTransactionId);
        TransactionContext.putBusinessTransaction(businessTransactionId, businessTransactionName);
        CpfWorkflowMetadata workflowMetadata = resolveWorkflowMetadata(
                joinPoint,
                transactionId,
                businessTransactionId,
                businessTransactionName);
        DynamicLogLevelRule dynamicLogLevelRule = dynamicLogLevelService
                .resolve(transactionId, businessTransactionId, moduleId)
                .orElse(null);
        TransactionContext.putDynamicLogLevel(dynamicLogLevelRule != null ? dynamicLogLevelRule.logLevel().name() : null);

        CpfWorkflowContext.apply(workflowMetadata);

        logByPolicy(
                logPolicy,
                CpfLogLevel.INFO,
                "Transaction started. transactionId={}, businessTransactionId={}, businessTransactionName={}, "
                        + "traceId={}, spanId={}, moduleId={}, sequenceNo={}, workflowInstanceId={}, workflowStepId={}, "
                        + "method={}, uri={}, controller={}, executionClass={}, executionMethod={}, clientIp={}, "
                        + "parameters={}, requestBody={}",
                transactionId,
                businessTransactionId,
                businessTransactionName,
                traceId,
                spanId,
                moduleId,
                sequenceNo,
                valueOrNone(workflowMetadata.getWorkflowInstanceId()),
                valueOrNone(workflowMetadata.getWorkflowStepId()),
                httpMethod,
                uri,
                controller,
                executionMetadata.className(),
                executionMetadata.methodName(),
                clientIp,
                parameters,
                requestBody);
        logDynamic(
                dynamicLogLevelRule,
                "Dynamic transaction diagnostic enabled. transactionId={}, businessTransactionId={}, moduleId={}, logLevel={}, reason={}, expiresAt={}",
                transactionId,
                businessTransactionId,
                moduleId,
                dynamicLogLevelRule != null ? dynamicLogLevelRule.logLevel() : null,
                dynamicLogLevelRule != null ? dynamicLogLevelRule.reason() : null,
                dynamicLogLevelRule != null ? dynamicLogLevelRule.expiresAt() : null);

        try {
            Object result = joinPoint.proceed();
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = elapsedMillis(startNanos);
            ResponseMetadata responseMetadata = resolveResponseMetadata(result, moduleId);
            String rawResponse = SensitiveDataMasker.mask(String.valueOf(result));
            String response = logPolicy.responseBodySave() ? rawResponse : null;

            logByPolicy(
                    logPolicy,
                    CpfLogLevel.INFO,
                    "Transaction completed. transactionId={}, traceId={}, spanId={}, moduleId={}, sequenceNo={}, workflowStatus={}, compensationYn={}, httpStatus={}, responseCode={}, messageCode={}, durationMs={}",
                    transactionId,
                    traceId,
                    spanId,
                    moduleId,
                    sequenceNo,
                    workflowStatusName(workflowMetadata, true),
                    compensationYn(workflowMetadata),
                    responseMetadata.httpStatus(),
                    responseMetadata.responseCode(),
                    responseMetadata.messageCode(),
                    durationMs);
            logDynamic(
                    dynamicLogLevelRule,
                    "Dynamic transaction diagnostic completed. transactionId={}, businessTransactionId={}, httpStatus={}, responseCode={}, messageCode={}, durationMs={}, response={}",
                    transactionId,
                    businessTransactionId,
                    responseMetadata.httpStatus(),
                    responseMetadata.responseCode(),
                    responseMetadata.messageCode(),
                    durationMs,
                    response);

            TransactionLogRecord record = buildLogRecord(
                    transactionId,
                    traceId,
                    spanId,
                    parentSpanId,
                    sequenceNo,
                    moduleId,
                    menuId,
                    businessTransactionId,
                    businessTransactionName,
                    "SUCCESS",
                    transactionHeader,
                    httpMethod,
                    uri,
                    controller,
                    executionMetadata,
                    workflowMetadata,
                    true,
                    parameters,
                    requestBody,
                    response,
                    responseMetadata.httpStatus(),
                    responseMetadata.responseCode(),
                    responseMetadata.messageCode(),
                    responseMetadata.messageContent(),
                    null,
                    null,
                    responseMetadata.externalMessage(),
                    responseMetadata.internalMessage(),
                    execUser,
                    clientIp,
                    userAgent,
                    startTime,
                    endTime,
                    durationMs);
            publishTransactionLog(record, details(record, transactionHeader, dynamicLogLevelRule, logPolicy), logPolicy);

            return result;
        } catch (Throwable ex) {
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = elapsedMillis(startNanos);
            ErrorMetadata errorMetadata = resolveErrorMetadata(ex, request != null ? request.getLocale() : Locale.KOREAN);
            String errorMessage = SensitiveDataMasker.mask(errorMetadata.errorMessage());
            String internalErrorMessage = logPolicy.errorStackSave() ? errorMetadata.internalMessage() : null;

            if (logPolicy.errorStackSave()) {
                logByPolicy(
                        logPolicy,
                        CpfLogLevel.ERROR,
                        "Transaction failed. transactionId={}, traceId={}, spanId={}, moduleId={}, sequenceNo={}, workflowStatus={}, compensationYn={}, durationMs={}, error={}",
                        transactionId,
                        traceId,
                        spanId,
                        moduleId,
                        sequenceNo,
                        workflowStatusName(workflowMetadata, false),
                        compensationYn(workflowMetadata),
                        durationMs,
                        errorMessage,
                        ex);
            } else {
                logByPolicy(
                        logPolicy,
                        CpfLogLevel.ERROR,
                        "Transaction failed. transactionId={}, traceId={}, spanId={}, moduleId={}, sequenceNo={}, workflowStatus={}, compensationYn={}, durationMs={}, error={}",
                        transactionId,
                        traceId,
                        spanId,
                        moduleId,
                        sequenceNo,
                        workflowStatusName(workflowMetadata, false),
                        compensationYn(workflowMetadata),
                        durationMs,
                        errorMessage);
            }
            logDynamic(
                    dynamicLogLevelRule,
                    "Dynamic transaction diagnostic failed. transactionId={}, businessTransactionId={}, httpStatus={}, responseCode={}, messageCode={}, durationMs={}, errorCode={}, error={}",
                    transactionId,
                    businessTransactionId,
                    errorMetadata.httpStatus(),
                    errorMetadata.responseCode(),
                    errorMetadata.messageCode(),
                    durationMs,
                    errorMetadata.errorCode(),
                    errorMessage);

            TransactionLogRecord record = buildLogRecord(
                    transactionId,
                    traceId,
                    spanId,
                    parentSpanId,
                    sequenceNo,
                    moduleId,
                    menuId,
                    businessTransactionId,
                    businessTransactionName,
                    "FAILURE",
                    transactionHeader,
                    httpMethod,
                    uri,
                    controller,
                    executionMetadata,
                    workflowMetadata,
                    false,
                    parameters,
                    requestBody,
                    null,
                    errorMetadata.httpStatus(),
                    errorMetadata.responseCode(),
                    errorMetadata.messageCode(),
                    errorMetadata.externalMessage(),
                    errorMessage,
                    errorMetadata.errorCode(),
                    errorMetadata.externalMessage(),
                    internalErrorMessage,
                    execUser,
                    clientIp,
                    userAgent,
                    startTime,
                    endTime,
                    durationMs);
            publishTransactionLog(record, details(record, transactionHeader, dynamicLogLevelRule, logPolicy), logPolicy);

            throw ex;
        }
    }

    private TransactionLogRecord buildLogRecord(
            String transactionId,
            String traceId,
            String spanId,
            String parentSpanId,
            int sequenceNo,
            String moduleId,
            String menuId,
            String businessTransactionId,
            String businessTransactionName,
            String logType,
            TransactionHeader transactionHeader,
            String httpMethod,
            String uri,
            String controller,
            ExecutionMetadata executionMetadata,
            CpfWorkflowMetadata workflowMetadata,
            boolean success,
            String parameters,
            String requestBody,
            String response,
            int httpStatus,
            String responseCode,
            String messageCode,
            String messageContent,
            String errorMessage,
            String errorCode,
            String externalMessage,
            String internalMessage,
            String execUser,
            String clientIp,
            String userAgent,
            LocalDateTime startTime,
            LocalDateTime endTime,
            long durationMs) {

        ServerInstanceIdentity.Identity serverIdentity = ServerInstanceIdentity.current();
        return TransactionLogRecord.builder()
                .transactionId(transactionId)
                .traceId(traceId)
                .spanId(spanId)
                .parentSpanId(parentSpanId)
                .sequenceNo(sequenceNo)
                .moduleId(moduleId)
                .menuId(menuId)
                .businessTransactionId(businessTransactionId)
                .businessTransactionName(businessTransactionName)
                .logType(logType)
                .apiVersion(headerValue(transactionHeader, TransactionHeader::getApiVersion))
                .clientAppId(headerValue(transactionHeader, TransactionHeader::getClientAppId))
                .clientVersion(headerValue(transactionHeader, TransactionHeader::getClientVersion))
                .callerService(headerValue(transactionHeader, TransactionHeader::getCallerService))
                .callerInstanceId(headerValue(transactionHeader, TransactionHeader::getCallerInstanceId))
                .correlationId(headerValue(transactionHeader, TransactionHeader::getCorrelationId))
                .idempotencyKey(headerValue(transactionHeader, TransactionHeader::getIdempotencyKey))
                .locale(headerValue(transactionHeader, TransactionHeader::getLocale))
                .timezone(headerValue(transactionHeader, TransactionHeader::getTimezone))
                .requestType(headerValue(transactionHeader, TransactionHeader::getRequestType))
                .originalChannelCode(headerValue(transactionHeader, TransactionHeader::getOriginalChannelCode))
                .channelCode(headerValue(transactionHeader, TransactionHeader::getChannelCode))
                .memberNo(headerValue(transactionHeader, TransactionHeader::getMemberNo))
                .customerNo(headerValue(transactionHeader, TransactionHeader::getCustomerNo))
                .screenId(headerValue(transactionHeader, TransactionHeader::getScreenId))
                .deviceId(headerValue(transactionHeader, TransactionHeader::getDeviceId))
                .clientRequestTime(headerValue(transactionHeader, TransactionHeader::getClientRequestTime))
                .wasId(headerValue(transactionHeader, TransactionHeader::getWasId))
                .serverInstanceId(serverIdentity.serverInstanceId())
                .hostName(serverIdentity.hostName())
                .processId(serverIdentity.processId())
                .threadName(serverIdentity.threadName())
                .reservedField1(headerValue(transactionHeader, TransactionHeader::getReservedField1))
                .reservedField2(headerValue(transactionHeader, TransactionHeader::getReservedField2))
                .reservedField3(headerValue(transactionHeader, TransactionHeader::getReservedField3))
                .reservedField4(headerValue(transactionHeader, TransactionHeader::getReservedField4))
                .reservedField5(headerValue(transactionHeader, TransactionHeader::getReservedField5))
                .httpMethod(httpMethod)
                .uri(uri)
                .controller(controller)
                .executionPackage(executionMetadata.packageName())
                .executionClass(executionMetadata.className())
                .executionMethod(executionMetadata.methodName())
                .executionSignature(SensitiveDataMasker.truncate(executionMetadata.signature(), 1000))
                .workflowId(workflowMetadata.getWorkflowId())
                .workflowName(workflowMetadata.getWorkflowName())
                .workflowInstanceId(workflowMetadata.getWorkflowInstanceId())
                .workflowStepId(workflowMetadata.getWorkflowStepId())
                .workflowStepName(workflowMetadata.getWorkflowStepName())
                .workflowStatus(workflowStatusName(workflowMetadata, success))
                .workflowFailurePolicy(workflowMetadata.getFailurePolicy() != null
                        ? workflowMetadata.getFailurePolicy().name()
                        : null)
                .compensationYn(compensationYn(workflowMetadata))
                .compensationTransactionId(workflowMetadata.getCompensationTransactionId())
                .compensationTargetTransactionId(workflowMetadata.getCompensationTargetTransactionId())
                .compensationStatus(compensationStatusName(workflowMetadata, success))
                .parameters(parameters)
                .requestBody(requestBody)
                .response(response)
                .httpStatus(httpStatus)
                .responseCode(responseCode)
                .messageCode(messageCode)
                .messageContent(SensitiveDataMasker.mask(messageContent))
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .externalMessage(SensitiveDataMasker.mask(externalMessage))
                .internalMessage(SensitiveDataMasker.mask(internalMessage))
                .execUser(execUser)
                .clientIp(clientIp)
                .userAgent(SensitiveDataMasker.truncate(userAgent, 500))
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .build();
    }

    private void publishTransactionLog(TransactionLogRecord record, Map<String, String> details, LogPolicyDecision logPolicy) {
        if (logPolicy != null && !logPolicy.dbLogEnabled()) {
            logger.debug(
                    "Transaction DB log skipped by policy. transactionId={}, businessTransactionId={}, source={}",
                    record != null ? record.getTransactionId() : "N/A",
                    record != null ? record.getBusinessTransactionId() : "N/A",
                    logPolicy.resolvedSource());
        }
        eventPublisher.publishEvent(new TransactionLogEvent(this, record, details, logPolicy));
    }

    private Map<String, String> details(
            TransactionLogRecord record,
            TransactionHeader transactionHeader,
            DynamicLogLevelRule dynamicLogLevelRule,
            LogPolicyDecision logPolicy) {
        Map<String, String> details = new LinkedHashMap<>();
        putDetail(details, "transaction.id", record.getTransactionId());
        putDetail(details, "trace.id", record.getTraceId());
        putDetail(details, "span.id", record.getSpanId());
        putDetail(details, "parentSpan.id", record.getParentSpanId());
        putDetail(details, "sequence.no", record.getSequenceNo());
        putDetail(details, "module.id", record.getModuleId());
        putDetail(details, "server.instance.id", record.getServerInstanceId());
        putDetail(details, "server.host.name", record.getHostName());
        putDetail(details, "server.process.id", record.getProcessId());
        putDetail(details, "server.thread.name", record.getThreadName());
        putDetail(details, "business.transaction.id", record.getBusinessTransactionId());
        putDetail(details, "business.transaction.name", record.getBusinessTransactionName());
        putDetail(details, "workflow.id", record.getWorkflowId());
        putDetail(details, "workflow.name", record.getWorkflowName());
        putDetail(details, "workflow.instance.id", record.getWorkflowInstanceId());
        putDetail(details, "workflow.step.id", record.getWorkflowStepId());
        putDetail(details, "workflow.step.name", record.getWorkflowStepName());
        putDetail(details, "workflow.status", record.getWorkflowStatus());
        putDetail(details, "workflow.failure.policy", record.getWorkflowFailurePolicy());
        putDetail(details, "compensation.yn", record.getCompensationYn());
        putDetail(details, "compensation.transaction.id", record.getCompensationTransactionId());
        putDetail(details, "compensation.target.transaction.id", record.getCompensationTargetTransactionId());
        putDetail(details, "compensation.status", record.getCompensationStatus());
        putDetail(details, "execution.package", record.getExecutionPackage());
        putDetail(details, "execution.class", record.getExecutionClass());
        putDetail(details, "execution.method", record.getExecutionMethod());
        putDetail(details, "execution.signature", record.getExecutionSignature());
        putDetail(details, "controller.shortSignature", record.getController());
        putDetail(details, "http.method", record.getHttpMethod());
        putDetail(details, "http.uri", record.getUri());
        putDetail(details, "http.clientIp", record.getClientIp());
        putDetail(details, "http.userAgent", record.getUserAgent());
        putDetail(details, "parameters", record.getParameters());
        putDetail(details, "requestBody", record.getRequestBody());
        putDetail(details, "response", record.getResponse());
        putDetail(details, "response.httpStatus", record.getHttpStatus());
        putDetail(details, "response.code", record.getResponseCode());
        putDetail(details, "response.messageCode", record.getMessageCode());
        putDetail(details, "response.messageContent", record.getMessageContent());
        putDetail(details, "error", record.getErrorMessage());
        putDetail(details, "error.code", record.getErrorCode());
        putDetail(details, "error.externalMessage", record.getExternalMessage());
        putDetail(details, "error.internalMessage", record.getInternalMessage());
        if (dynamicLogLevelRule != null) {
            putDetail(details, "dynamicLog.rule.id", dynamicLogLevelRule.ruleId());
            putDetail(details, "dynamicLog.level", dynamicLogLevelRule.logLevel());
            putDetail(details, "dynamicLog.reason", dynamicLogLevelRule.reason());
            putDetail(details, "dynamicLog.expiresAt", dynamicLogLevelRule.expiresAt());
        }
        if (logPolicy != null) {
            putDetail(details, "logPolicy.targetType", logPolicy.targetType());
            putDetail(details, "logPolicy.targetId", logPolicy.targetId());
            putDetail(details, "logPolicy.fileLogLevel", logPolicy.fileLogLevel());
            putDetail(details, "logPolicy.dbLogEnabled", logPolicy.dbLogEnabledYn());
            putDetail(details, "logPolicy.dbLogLevel", logPolicy.dbLogLevel());
            putDetail(details, "logPolicy.requestBodySaveYn", logPolicy.requestBodySaveYn());
            putDetail(details, "logPolicy.responseBodySaveYn", logPolicy.responseBodySaveYn());
            putDetail(details, "logPolicy.errorStackSaveYn", logPolicy.errorStackSaveYn());
            putDetail(details, "logPolicy.maskingPolicyKey", logPolicy.maskingPolicyKey());
            putDetail(details, "logPolicy.resolvedSource", logPolicy.resolvedSource());
            putDetail(details, "logPolicy.overrideId", logPolicy.overrideId());
            putDetail(details, "logPolicy.policyId", logPolicy.policyId());
        }
        if (transactionHeader != null) {
            CpfHeaderSnapshot headerSnapshot = CpfHeaderPropagator.currentSnapshot(transactionHeader);
            putDetail(details, "headers", CpfHeaderAuditLogger.toJson(headerSnapshot.resolvedHeaders()));
            putDetail(details, "inboundHeaders", CpfHeaderAuditLogger.toJson(headerSnapshot.inboundHeaders()));
            putDetail(details, "resolvedHeaders", CpfHeaderAuditLogger.toJson(headerSnapshot.resolvedHeaders()));
            putDetail(details, "outboundHeaders", CpfHeaderAuditLogger.toJson(headerSnapshot.outboundHeaders()));
            putDetail(details, "responseHeaders", CpfHeaderAuditLogger.toJson(headerSnapshot.responseHeaders()));
            putDetail(details, "transactionHeader", transactionHeader.toString());
        }
        putDetail(details, "propagationHeaders", CpfHeaderAuditLogger.toJson(CpfHeaderPropagator.outboundHeaders()));
        putDetail(details, "workflowPropagationHeaders", CpfWorkflowContext.propagationHeaders().toString());
        return details;
    }

    private LogPolicyDecision resolveOnlineLogPolicy(String businessTransactionId) {
        LogPolicyResolver resolver = logPolicyResolverProvider.getIfAvailable();
        if (resolver == null) {
            return LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, businessTransactionId);
        }
        try {
            return resolver.resolveOnlineTransaction(businessTransactionId);
        } catch (RuntimeException ex) {
            logger.warn("Failed to resolve transaction log policy. businessTransactionId={}", businessTransactionId, ex);
            return LogPolicyDecision.cpfDefault(LogPolicyTargetType.ONLINE_TRANSACTION, businessTransactionId)
                    .withSource("SAFE_FALLBACK");
        }
    }

    private void logByPolicy(LogPolicyDecision logPolicy, CpfLogLevel fallbackLevel, String message, Object... arguments) {
        CpfLogLevel level = toLogLevel(logPolicy != null ? logPolicy.fileLogLevel() : null, fallbackLevel);
        switch (level) {
            case TRACE -> logger.trace(message, arguments);
            case DEBUG -> logger.debug(message, arguments);
            case INFO -> logger.info(message, arguments);
            case WARN -> logger.warn(message, arguments);
            case ERROR -> logger.error(message, arguments);
            case OFF -> {
            }
        }
    }

    private CpfLogLevel toLogLevel(String value, CpfLogLevel fallbackLevel) {
        if (!hasText(value)) {
            return fallbackLevel;
        }
        try {
            return CpfLogLevel.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return fallbackLevel;
        }
    }

    private void logDynamic(DynamicLogLevelRule rule, String message, Object... arguments) {
        if (rule == null || rule.logLevel() == null || rule.logLevel() == CpfLogLevel.OFF) {
            return;
        }

        switch (rule.logLevel()) {
            case TRACE -> logger.trace(message, arguments);
            case DEBUG -> logger.debug(message, arguments);
            case INFO -> logger.info(message, arguments);
            case WARN -> logger.warn(message, arguments);
            case ERROR -> logger.error(message, arguments);
            case OFF -> {
            }
        }
    }

    private void putDetail(Map<String, String> details, String key, Object value) {
        if (value != null) {
            details.put(key, String.valueOf(value));
        }
    }

    private OnlineExecutionMetadata resolveOnlineExecution(ProceedingJoinPoint joinPoint) {
        CpfOnlineTransaction standard = resolveAnnotation(joinPoint, CpfOnlineTransaction.class);
        if (standard != null) {
            return new OnlineExecutionMetadata(standard.id(), standard.name());
        }
        CpfSharedApi shared = resolveAnnotation(joinPoint, CpfSharedApi.class);
        if (shared != null) {
            return new OnlineExecutionMetadata(shared.id(), shared.name());
        }
        CpfTransaction legacy = resolveAnnotation(joinPoint, CpfTransaction.class);
        return legacy == null ? null : new OnlineExecutionMetadata(legacy.id(), legacy.name());
    }

    private record OnlineExecutionMetadata(String id, String name) {
    }

    private CpfWorkflowMetadata resolveWorkflowMetadata(
            ProceedingJoinPoint joinPoint,
            String transactionId,
            String businessTransactionId,
            String businessTransactionName) {

        CpfWorkflow workflow = resolveAnnotation(joinPoint, CpfWorkflow.class);
        CpfWorkflowStep step = resolveAnnotation(joinPoint, CpfWorkflowStep.class);
        CpfWorkflowMetadata incoming = CpfWorkflowContext.current();

        String workflowId = firstText(
                incoming != null ? incoming.getWorkflowId() : null,
                workflow != null ? workflow.id() : null);
        String workflowName = firstText(
                incoming != null ? incoming.getWorkflowName() : null,
                workflow != null ? workflow.name() : null);
        String workflowStepId = firstText(
                step != null ? step.id() : null,
                incoming != null ? incoming.getWorkflowStepId() : null);
        String workflowStepName = firstText(
                step != null ? step.name() : null,
                incoming != null ? incoming.getWorkflowStepName() : null);

        boolean workflowDeclared = workflow != null || step != null || incoming != null && incoming.isActive();
        if (!hasText(workflowStepId) && workflowDeclared && !"UNKNOWN".equals(businessTransactionId)) {
            workflowStepId = businessTransactionId;
        }
        if (!hasText(workflowStepName) && workflowDeclared) {
            workflowStepName = businessTransactionName;
        }

        CpfWorkflowFailurePolicy failurePolicy = step != null
                ? step.failurePolicy()
                : incoming != null ? incoming.getFailurePolicy() : null;
        if (failurePolicy == null && workflowDeclared) {
            failurePolicy = CpfWorkflowFailurePolicy.FAIL;
        }

        boolean compensation = step != null && step.compensation()
                || incoming != null && incoming.isCompensation();
        String compensationTransactionId = firstText(
                step != null ? step.compensationTransactionId() : null,
                incoming != null ? incoming.getCompensationTransactionId() : null);
        String compensationTargetTransactionId = firstText(
                step != null ? step.compensationTargetTransactionId() : null,
                incoming != null ? incoming.getCompensationTargetTransactionId() : null);

        String workflowInstanceId = firstText(
                incoming != null ? incoming.getWorkflowInstanceId() : null,
                workflowDeclared ? transactionId : null);

        return CpfWorkflowMetadata.builder()
                .workflowId(workflowId)
                .workflowName(workflowName)
                .workflowInstanceId(workflowInstanceId)
                .workflowStepId(workflowStepId)
                .workflowStepName(workflowStepName)
                .failurePolicy(failurePolicy)
                .compensation(compensation)
                .compensationTransactionId(compensationTransactionId)
                .compensationTargetTransactionId(compensationTargetTransactionId)
                .build();
    }

    private <T extends Annotation> T resolveAnnotation(ProceedingJoinPoint joinPoint, Class<T> annotationType) {
        if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
            return null;
        }

        Method method = resolveTargetMethod(joinPoint, methodSignature);
        T methodAnnotation = method.getAnnotation(annotationType);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        Class<?> targetClass = joinPoint.getTarget() != null
                ? joinPoint.getTarget().getClass()
                : method.getDeclaringClass();
        T classAnnotation = targetClass.getAnnotation(annotationType);
        if (classAnnotation != null) {
            return classAnnotation;
        }

        return method.getDeclaringClass().getAnnotation(annotationType);
    }

    private ExecutionMetadata resolveExecutionMetadata(ProceedingJoinPoint joinPoint) {
        if (!(joinPoint.getSignature() instanceof MethodSignature methodSignature)) {
            String className = joinPoint.getSignature().getDeclaringTypeName();
            return new ExecutionMetadata(packageName(className), className, joinPoint.getSignature().getName(), controllerSignature(joinPoint));
        }

        Method method = resolveTargetMethod(joinPoint, methodSignature);
        Class<?> declaringClass = method.getDeclaringClass();
        return new ExecutionMetadata(
                declaringClass.getPackageName(),
                declaringClass.getName(),
                method.getName(),
                method.toGenericString());
    }

    private Method resolveTargetMethod(ProceedingJoinPoint joinPoint, MethodSignature methodSignature) {
        Method method = methodSignature.getMethod();
        Object target = joinPoint.getTarget();
        if (target == null) {
            return method;
        }

        try {
            return target.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException ex) {
            return method;
        }
    }

    private String workflowStatusName(CpfWorkflowMetadata metadata, boolean success) {
        if (metadata == null || !metadata.isActive()) {
            return CpfWorkflowStatus.NONE.name();
        }
        if (success) {
            return metadata.isCompensation()
                    ? CpfWorkflowStatus.COMPENSATED.name()
                    : CpfWorkflowStatus.COMPLETED.name();
        }

        CpfWorkflowFailurePolicy policy = metadata.getFailurePolicy();
        if (policy == null) {
            return CpfWorkflowStatus.FAILED.name();
        }

        return switch (policy) {
            case RETRY -> CpfWorkflowStatus.RETRY_PENDING.name();
            case COMPENSATE -> CpfWorkflowStatus.COMPENSATING.name();
            case PENDING -> CpfWorkflowStatus.PENDING.name();
            case VERIFY -> CpfWorkflowStatus.VERIFY_REQUIRED.name();
            case MANUAL -> CpfWorkflowStatus.MANUAL_REQUIRED.name();
            case IGNORE -> CpfWorkflowStatus.IGNORED.name();
            case FAIL -> CpfWorkflowStatus.FAILED.name();
        };
    }

    private String compensationStatusName(CpfWorkflowMetadata metadata, boolean success) {
        if (metadata == null || !metadata.isActive()) {
            return null;
        }

        if (metadata.isCompensation()) {
            return success
                    ? CpfWorkflowStatus.COMPENSATED.name()
                    : CpfWorkflowStatus.COMPENSATION_FAILED.name();
        }
        if (!success && metadata.getFailurePolicy() == CpfWorkflowFailurePolicy.COMPENSATE) {
            return CpfWorkflowStatus.COMPENSATING.name();
        }
        return null;
    }

    private String compensationYn(CpfWorkflowMetadata metadata) {
        return metadata != null && metadata.isCompensation() ? "Y" : "N";
    }

    private String resolveModuleId(ProceedingJoinPoint joinPoint) {
        String configuredModuleId = environment.getProperty("cpf.framework.module-id");
        if (hasText(configuredModuleId)) {
            return CpfSystemCodes.normalize(configuredModuleId, CpfSystemCodes.CORE);
        }

        String declaringType = joinPoint.getSignature().getDeclaringTypeName();
        String inferredModuleId = CpfSystemCodes.inferFromTypeName(declaringType);
        if (hasText(inferredModuleId)) {
            return inferredModuleId;
        }

        String appName = environment.getProperty("spring.application.name");
        if (hasText(appName)) {
            return CpfSystemCodes.normalize(appName, CpfSystemCodes.CORE);
        }
        return "N/A";
    }

    private String requestParameters(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (hasText(queryString)) {
            return SensitiveDataMasker.mask(queryString);
        }

        if (request.getParameterMap().isEmpty()) {
            return "None";
        }

        String parameters = request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                .collect(Collectors.joining("&"));
        return SensitiveDataMasker.mask(parameters);
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "None";
        }

        String value = Arrays.stream(args)
                .filter(Objects::nonNull)
                .filter(arg -> !(arg instanceof ServletRequest))
                .filter(arg -> !(arg instanceof ServletResponse))
                .filter(arg -> !(arg instanceof BindingResult))
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]"));

        return value.equals("[]") ? "None" : SensitiveDataMasker.mask(value);
    }

    private ResponseMetadata resolveResponseMetadata(Object result, String moduleId) {
        int httpStatus = resolveHttpStatus(result, 200);
        Object body = result instanceof ResponseEntity<?> responseEntity ? responseEntity.getBody() : result;
        String normalizedModuleId = hasText(moduleId) && !"N/A".equals(moduleId) ? moduleId : "CPF";
        String responseCode = bodyProperty(body, "statusCode");
        String normalizedResponseCode = firstText(responseCode, "S" + normalizedModuleId + "000000");
        CpfResolvedResponse resolved = responseCodeResolver.resolve(normalizedResponseCode, Locale.KOREAN, Map.of(), null);
        String messageCode = firstText(bodyProperty(body, "messageCode"), resolved.messageCode());
        String bodyMessage = firstText(bodyProperty(body, "messageContent"), bodyProperty(body, "message"));
        String messageContent = firstText(bodyMessage, resolved.externalMessage());
        return new ResponseMetadata(
                httpStatus,
                normalizedResponseCode,
                messageCode,
                messageContent,
                firstText(bodyMessage, resolved.externalMessage()),
                resolved.internalMessage());
    }

    private int resolveHttpStatus(Object result, int defaultCode) {
        if (result instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getStatusCode().value();
        }
        return defaultCode;
    }

    private int resolveErrorResponseCode(Throwable ex) {
        if (ex instanceof CpfException cpfException && cpfException.getErrorCode() != null) {
            return cpfException.getErrorCode().getHttpStatus().value();
        }
        if (ex instanceof ResponseStatusException responseStatusException) {
            return responseStatusException.getStatusCode().value();
        }
        if (ex instanceof ErrorResponse errorResponse) {
            return errorResponse.getStatusCode().value();
        }
        if (ex instanceof MethodArgumentNotValidException
                || ex instanceof MissingServletRequestParameterException
                || ex instanceof MethodArgumentTypeMismatchException
                || ex instanceof IllegalArgumentException) {
            return 400;
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return 405;
        }
        return 500;
    }

    private ErrorMetadata resolveErrorMetadata(Throwable ex, Locale locale) {
        if (ex instanceof CpfException cpfException) {
            CpfResolvedResponse resolved = cpfException.getErrorCode() != null
                    ? responseCodeResolver.resolve(cpfException.getErrorCode(), locale, cpfException.getMessageArguments(), cpfException.getDetail())
                    : responseCodeResolver.resolve(cpfException.getResponseCode(), locale, cpfException.getMessageArguments(), cpfException.getDetail());
            return new ErrorMetadata(
                    resolved.httpStatus(),
                    resolved.responseCode(),
                    resolved.messageCode(),
                    resolved.errorCode(),
                    firstText(cpfException.getExternalMessage(), resolved.externalMessage()),
                    firstText(cpfException.getInternalMessage(), resolved.internalMessage()),
                    resolved.errorMessage());
        }

        String internalMessage = firstText(ex.getMessage(), ex.getClass().getName());
        int httpStatus = resolveErrorResponseCode(ex);
        return new ErrorMetadata(
                httpStatus,
                "ECPF990000",
                "MCPF990000",
                ex.getClass().getSimpleName(),
                "요청 처리 중 내부 오류가 발생했습니다.",
                internalMessage,
                internalMessage);
    }

    private String resolveExecUser(HttpServletRequest request) {
        if (request == null) {
            return "N/A";
        }

        return firstText(
                request.getHeader("X-User-Id"),
                firstText(request.getParameter("execUser"), request.getParameter("requesterId")),
                "N/A");
    }

    private String buildRequestUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (!hasText(queryString)) {
            return request.getRequestURI();
        }
        return request.getRequestURI() + "?" + SensitiveDataMasker.mask(queryString);
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        return CpfTrustedProxyPolicy.resolveClientIp(request);
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String headerValue(TransactionHeader transactionHeader, java.util.function.Function<TransactionHeader, String> accessor) {
        return transactionHeader != null ? accessor.apply(transactionHeader) : null;
    }

    private String firstText(String first, String second, String fallback) {
        return hasText(first) ? first : (hasText(second) ? second : fallback);
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private String bodyProperty(Object body, String propertyName) {
        if (body == null || body instanceof String) {
            return null;
        }
        try {
            BeanWrapper wrapper = new BeanWrapperImpl(body);
            if (!wrapper.isReadableProperty(propertyName)) {
                return null;
            }
            Object value = wrapper.getPropertyValue(propertyName);
            return value == null ? null : String.valueOf(value);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private String valueOrNone(String value) {
        return hasText(value) ? value : "None";
    }

    private String packageName(String className) {
        int lastDot = className != null ? className.lastIndexOf('.') : -1;
        return lastDot > 0 ? className.substring(0, lastDot) : "N/A";
    }

    private String controllerSignature(ProceedingJoinPoint joinPoint) {
        return joinPoint.getSignature() != null ? joinPoint.getSignature().toShortString() : "N/A";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record ExecutionMetadata(
            String packageName,
            String className,
            String methodName,
            String signature) {
    }

    private record ResponseMetadata(
            int httpStatus,
            String responseCode,
            String messageCode,
            String messageContent,
            String externalMessage,
            String internalMessage) {
    }

    private record ErrorMetadata(
            int httpStatus,
            String responseCode,
            String messageCode,
            String errorCode,
            String externalMessage,
            String internalMessage,
            String errorMessage) {
    }
}
