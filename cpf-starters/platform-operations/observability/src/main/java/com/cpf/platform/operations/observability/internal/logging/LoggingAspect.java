package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.security.api.CpfMaskingRuntime;

import com.cpf.platform.operations.observability.api.logging.CpfLogLevel;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort.SegmentScope;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.cpf.core.api.error.DefaultCpfResponseCodeResolver;
import com.cpf.core.api.error.CpfException;
import com.cpf.core.api.error.CpfResolvedResponse;
import com.cpf.core.api.error.CpfResponseCodeResolver;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import com.cpf.foundation.execution.api.CpfSharedApi;
import com.cpf.foundation.context.header.CpfHeaderAuditLogger;
import com.cpf.platform.operations.observability.internal.logging.header.CpfHeaderPropagator;
import com.cpf.platform.operations.observability.internal.logging.header.CpfHeaderSnapshot;
import com.cpf.foundation.context.header.CpfTrustedProxyPolicy;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.internal.logging.policy.LogPolicyResolver;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import com.cpf.foundation.workflow.CpfWorkflow;
import com.cpf.foundation.workflow.CpfWorkflowContext;
import com.cpf.foundation.workflow.CpfWorkflowFailurePolicy;
import com.cpf.foundation.workflow.CpfWorkflowMetadata;
import com.cpf.foundation.workflow.CpfWorkflowStatus;
import com.cpf.foundation.workflow.CpfWorkflowStep;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
import java.time.Clock;
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
    private final CpfTraceSamplingPolicy traceSamplingPolicy;
    private final CpfResponseCodeResolver responseCodeResolver;
    private final ObjectProvider<LogPolicyResolver> logPolicyResolverProvider;
    private final CpfTransactionSegmentPort transactionSegments;
    private final Clock clock;

    public LoggingAspect(
            ApplicationEventPublisher eventPublisher,
            Environment environment,
            DynamicTransactionLogLevelService dynamicLogLevelService,
            ObjectProvider<CpfTraceSamplingPolicy> traceSamplingPolicyProvider,
            ObjectProvider<CpfResponseCodeResolver> responseCodeResolverProvider,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider,
            CpfTransactionSegmentPort transactionSegments,
            ObjectProvider<Clock> clockProvider) {
        this.eventPublisher = eventPublisher;
        this.environment = environment;
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.traceSamplingPolicy = traceSamplingPolicyProvider.getIfAvailable(CpfTraceSamplingPolicy::new);
        this.responseCodeResolver = responseCodeResolverProvider.getIfAvailable(DefaultCpfResponseCodeResolver::new);
        this.logPolicyResolverProvider = logPolicyResolverProvider;
        this.transactionSegments = Objects.requireNonNull(transactionSegments, "transactionSegments");
        this.clock = clockProvider.getIfUnique(Clock::systemUTC);
    }

    @Around("@annotation(com.cpf.foundation.execution.api.CpfOnlineTransaction) || "
            + "@within(com.cpf.foundation.execution.api.CpfOnlineTransaction) || "
            + "execution(* com.cpf.integration.api.domaincall.CpfDomainOperation+.invoke(..))")
    public Object logTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now(clock);
        long startNanos = System.nanoTime();
        HttpServletRequest request = currentRequest();

        String transactionId = TransactionContext.getOrCreateTransactionId();
        String traceId = TransactionContext.getOrCreateTraceId();
        String spanId = TransactionContext.getOrCreateSpanId();
        TransactionContext.refreshCanonicalMdc();
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
                headerValue(transactionHeader, value -> value.getClientIp()),
                request != null ? clientIp(request) : null,
                "N/A");
        String userAgent = request != null ? request.getHeader("User-Agent") : null;
        String businessTransactionId = onlineExecution != null ? onlineExecution.id() : "UNKNOWN";
        String businessTransactionName = onlineExecution != null ? onlineExecution.name() : controller;
        // Capture the parent before start() pushes the newly created child frame.
        String parentTransactionSegmentId = firstText(
                com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentContext.currentSegmentId(),
                com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentContext
                        .incomingParentSegmentId(transactionHeader));
        SegmentScope transactionSegment = transactionSegments.start(
                CpfTransactionSegmentPort.Role.MAIN,
                CpfTransactionSegmentPort.Direction.INBOUND,
                firstText(TransactionContext.currentSystemCode(), moduleId),
                TransactionContext.callerSystemCode(),
                TransactionContext.targetSystemCode(),
                uri,
                businessTransactionName);
        // SegmentScope.success()/fail() removes its thread-local frame.  Preserve the durable
        // segment relationship before ending the scope so the asynchronous File Log consumer
        // receives the very same segmentId/parentSegmentId stored in CPF_TRANSACTION_SEGMENT.
        String transactionSegmentId = transactionSegment.transactionSegmentId();
        LogPolicyDecision logPolicy = resolveOnlineLogPolicy(businessTransactionId);
        if (!logPolicy.requestBodySave()) {
            requestBody = null;
        }
        String menuId = firstText(request != null ? request.getParameter("menuId") : null, businessTransactionId);
        TransactionContext.putStandardExecution(businessTransactionId, businessTransactionName);
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
            LocalDateTime endTime = LocalDateTime.now(clock);
            long durationMs = elapsedMillis(startNanos);
            ResponseMetadata responseMetadata = resolveResponseMetadata(result, moduleId);
            boolean success = responseMetadata.httpStatus() < 400;
            if (success) {
                transactionSegment.success();
            } else {
                transactionSegment.fail("HTTP_" + responseMetadata.httpStatus(), responseMetadata.responseCode());
            }
            String rawResponse = CpfMaskingRuntime.mask(String.valueOf(result));
            String response = success
                    ? (logPolicy.responseBodySave() ? rawResponse : null)
                    : canonicalErrorResponse(responseMetadata, transactionId);

            logByPolicy(
                    logPolicy,
                    CpfLogLevel.INFO,
                    "Transaction completed. transactionId={}, traceId={}, spanId={}, moduleId={}, sequenceNo={}, workflowStatus={}, compensationYn={}, httpStatus={}, responseCode={}, messageCode={}, durationMs={}",
                    transactionId,
                    traceId,
                    spanId,
                    moduleId,
                    sequenceNo,
                    workflowStatusName(workflowMetadata, success),
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
                    success ? "SUCCESS" : "FAILURE",
                    transactionHeader,
                    httpMethod,
                    uri,
                    controller,
                    executionMetadata,
                    workflowMetadata,
                    success,
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
            boolean traceSampled = traceSamplingPolicy.shouldSample(
                    transactionId, businessTransactionId, moduleId, success, dynamicLogLevelRule);
            publishTransactionLog(record, details(record, transactionHeader, dynamicLogLevelRule, logPolicy,
                    traceSampled, transactionSegmentId, parentTransactionSegmentId), logPolicy);

            return result;
        } catch (Throwable ex) {
            transactionSegment.fail(ex.getClass().getSimpleName(), ex.getMessage());
            LocalDateTime endTime = LocalDateTime.now(clock);
            long durationMs = elapsedMillis(startNanos);
            ErrorMetadata errorMetadata = resolveErrorMetadata(ex, request != null ? request.getLocale() : Locale.KOREAN);
            String errorMessage = CpfMaskingRuntime.mask(errorMetadata.errorMessage());
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

            String errorResponse = canonicalErrorResponse(errorMetadata, transactionId);

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
                    errorResponse,
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
            boolean traceSampled = traceSamplingPolicy.shouldSample(
                    transactionId, businessTransactionId, moduleId, false, dynamicLogLevelRule);
            publishTransactionLog(record, details(record, transactionHeader, dynamicLogLevelRule, logPolicy,
                    traceSampled, transactionSegmentId, parentTransactionSegmentId), logPolicy);

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

        com.cpf.foundation.runtime.CpfInstanceIdentity.Identity runtimeIdentity = com.cpf.foundation.runtime.CpfInstanceIdentity.current();
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
                .apiVersion(headerValue(transactionHeader, value -> value.getApiVersion()))
                .clientId(headerValue(transactionHeader, value -> value.getClientId()))
                .clientVersion(headerValue(transactionHeader, value -> value.getClientVersion()))
                .callerSystemCode(TransactionContext.callerSystemCode())
                .targetSystemCode(TransactionContext.targetSystemCode())
                .originalSystemCode(TransactionContext.originalSystemCode())
                .systemCode(TransactionContext.currentSystemCode())
                .callerChannel(TransactionContext.callerChannel())
                .targetChannel(TransactionContext.targetChannel())
                .targetOperationId(TransactionContext.observedOperationId())
                .callerInstanceId(headerValue(transactionHeader, value -> value.getCallerInstanceId()))
                .correlationId(headerValue(transactionHeader, value -> value.getCorrelationId()))
                .idempotencyKey(headerValue(transactionHeader, value -> value.getIdempotencyKey()))
                .locale(headerValue(transactionHeader, value -> value.getLocale()))
                .timezone(headerValue(transactionHeader, value -> value.getTimezone()))
                .requestType(headerValue(transactionHeader, value -> value.getRequestType()))
                .originalChannel(TransactionContext.originalChannel())
                .currentChannel(TransactionContext.currentChannel())
                .memberNo(headerValue(transactionHeader, value -> value.getMemberNo()))
                .customerNo(headerValue(transactionHeader, value -> value.getCustomerNo()))
                .screenId(headerValue(transactionHeader, value -> value.getScreenId()))
                .deviceId(headerValue(transactionHeader, value -> value.getDeviceId()))
                .clientRequestTime(headerValue(transactionHeader, value -> value.getClientRequestTime()))
                .wasId(headerValue(transactionHeader, value -> value.getWasId()))
                .instanceId(runtimeIdentity.instanceId())
                .hostName(runtimeIdentity.hostName())
                .hostIp(runtimeIdentity.hostIp())
                .processId(runtimeIdentity.processId())
                .threadName(runtimeIdentity.threadName())
                .reservedField1(headerValue(transactionHeader, value -> value.getReservedField1()))
                .reservedField2(headerValue(transactionHeader, value -> value.getReservedField2()))
                .reservedField3(headerValue(transactionHeader, value -> value.getReservedField3()))
                .reservedField4(headerValue(transactionHeader, value -> value.getReservedField4()))
                .reservedField5(headerValue(transactionHeader, value -> value.getReservedField5()))
                .httpMethod(httpMethod)
                .uri(uri)
                .controller(controller)
                .executionPackage(executionMetadata.packageName())
                .executionClass(executionMetadata.className())
                .executionMethod(executionMetadata.methodName())
                .executionSignature(CpfMaskingRuntime.truncate(executionMetadata.signature(), 1000))
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
                .messageContent(CpfMaskingRuntime.mask(messageContent))
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .externalMessage(CpfMaskingRuntime.mask(externalMessage))
                .internalMessage(CpfMaskingRuntime.mask(internalMessage))
                .execUser(execUser)
                .clientIp(clientIp)
                .userAgent(CpfMaskingRuntime.truncate(userAgent, 500))
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
            LogPolicyDecision logPolicy,
            boolean traceSampled,
            String transactionSegmentId,
            String parentTransactionSegmentId) {
        Map<String, String> details = new LinkedHashMap<>();
        addAutomaticManagementMetadata(details, record);
        putDetail(details, "trace.sampled", traceSampled);
        // `transactionSegment.*` is the File Log contract.  Keep `segment.id` as the
        // compatibility projection used by existing diagnostics, but bind both to the durable
        // database segment rather than a telemetry span or an already-popped thread-local.
        putDetail(details, "transactionSegment.id", transactionSegmentId);
        putDetail(details, "parentSegment.id", parentTransactionSegmentId);
        putDetail(details, "segment.id", transactionSegmentId);
        if (!traceSampled) {
            putDetail(details, "transaction.id", record.getTransactionId());
            putDetail(details, "trace.id", record.getTraceId());
            putDetail(details, "span.id", record.getSpanId());
            putDetail(details, "module.id", record.getModuleId());
            putDetail(details, "business.transaction.id", record.getBusinessTransactionId());
            putDetail(details, "transaction.status", record.getLogType());
            putDetail(details, "duration.ms", record.getDurationMs());
            return details;
        }
        putDetail(details, "transaction.id", record.getTransactionId());
        putDetail(details, "execution.id", com.cpf.core.api.context.CpfContexts.currentExecutionId());
        var cpfContext = com.cpf.core.api.context.CpfContexts.current();
        putDetail(details, "execution.attempt", cpfContext == null ? 0 : cpfContext.execution().attempt());
        putDetail(details, "trace.id", record.getTraceId());
        putDetail(details, "span.id", record.getSpanId());
        putDetail(details, "parentSpan.id", record.getParentSpanId());
        putDetail(details, "sequence.no", record.getSequenceNo());
        putDetail(details, "module.id", record.getModuleId());
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
        com.cpf.foundation.execution.api.CpfOnlineTransaction publicStandard =
                resolveAnnotation(joinPoint, com.cpf.foundation.execution.api.CpfOnlineTransaction.class);
        if (publicStandard != null) {
            return new OnlineExecutionMetadata(publicStandard.operationId(), publicStandard.name());
        }
        CpfOnlineTransaction standard = resolveAnnotation(joinPoint, CpfOnlineTransaction.class);
        if (standard != null) {
            return new OnlineExecutionMetadata(standard.operationId(), standard.name());
        }
        com.cpf.foundation.execution.api.CpfSharedApi publicShared =
                resolveAnnotation(joinPoint, com.cpf.foundation.execution.api.CpfSharedApi.class);
        if (publicShared != null) {
            return new OnlineExecutionMetadata(publicShared.id(), publicShared.name());
        }
        CpfSharedApi shared = resolveAnnotation(joinPoint, CpfSharedApi.class);
        if (shared != null) {
            return new OnlineExecutionMetadata(shared.id(), shared.name());
        }
        CpfTransaction legacy = resolveAnnotation(joinPoint, CpfTransaction.class);
        if (legacy != null) {
            return new OnlineExecutionMetadata(legacy.id(), legacy.name());
        }
        return resolveDomainOperation(joinPoint);
    }

    /**
     * Integration은 Observability의 선택 기능이므로 이 모듈에 역방향 compile dependency를 만들지 않습니다.
     * Pointcut이 실제 Public SPI 구현의 invoke에만 진입한 뒤 그 SPI가 보장하는 metadata를 읽습니다.
     */
    private OnlineExecutionMetadata resolveDomainOperation(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        if (target == null || !implementsInterface(target.getClass(),
                "com.cpf.integration.api.domaincall.CpfDomainOperation")) {
            return null;
        }
        try {
            Object operationId = target.getClass().getMethod("operationId").invoke(target);
            String value = operationId == null ? null : operationId.toString().trim();
            if (!hasText(value)) {
                throw new IllegalStateException("CPF_DOMAIN_OPERATION_ID_REQUIRED");
            }
            return new OnlineExecutionMetadata(value, value);
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException("CPF_DOMAIN_OPERATION_METADATA_UNAVAILABLE", failure);
        }
    }

    private static boolean implementsInterface(Class<?> type, String interfaceName) {
        if (type == null) {
            return false;
        }
        for (Class<?> candidate : type.getInterfaces()) {
            if (interfaceName.equals(candidate.getName()) || implementsInterface(candidate, interfaceName)) {
                return true;
            }
        }
        return implementsInterface(type.getSuperclass(), interfaceName);
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


    /**
     * CPF가 이미 알고 있는 Runtime/System/Capability metadata를 개발자 입력 없이 모든 거래 로그에 자동 부착합니다.
     * Header/Body 원문은 기존 LogPolicy/Masking 경계가 소유하며 이 메서드는 식별/운영 metadata만 취급합니다.
     */
    private void addAutomaticManagementMetadata(Map<String, String> details, TransactionLogRecord record) {
        String application = firstConfigured("spring.application.name", "cpf.application", "cpf.app.name");
        String currentChannel = firstConfigured("cpf.system-code", "cpf.system.id", "cpf.generated-domain.system-code");
        if (!hasText(currentChannel)) currentChannel = firstText(application, record != null ? record.getModuleId() : null, "CPF");
        String domainCode = firstConfigured("cpf.domain-code", "cpf.domain.code", "cpf.generated-domain.domain-code");
        if (!hasText(domainCode)) domainCode = firstConfigured("cpf.generated-domain.system-code");
        String module = firstConfigured("cpf.framework.module-id", "cpf.module", "cpf.runtime.role");
        if (!hasText(module) && record != null) module = record.getModuleId();

        putDetail(details, "runtime.currentChannel", currentChannel);
        putDetail(details, "runtime.domainCode", domainCode);
        putDetail(details, "runtime.application", application);
        putDetail(details, "runtime.module", module);
        putDetail(details, "runtime.instanceId", record != null ? record.getInstanceId() : null);
        putDetail(details, "runtime.instanceToken", transactionInstanceToken(record));
        putDetail(details, "capability.starters", MDC.get("cpf.used.starters"));
        putDetail(details, "capability.ids", MDC.get("cpf.used.capabilities"));
        putDetail(details, "capability.providers", MDC.get("cpf.used.providers"));
        putDetail(details, "capability.operations", MDC.get("cpf.used.operations"));
    }

    private String firstConfigured(String... keys) {
        if (keys == null) return null;
        for (String key : keys) {
            String value = environment.getProperty(key);
            if (hasText(value)) return value.trim();
        }
        return null;
    }

    private static String transactionInstanceToken(TransactionLogRecord record) {
        if (record == null || !CpfTransactionIds.isCanonical(record.getTransactionId())) {
            return null;
        }
        return CpfTransactionIds.instanceToken(record.getTransactionId());
    }

    /**
     * 로그의 moduleId 를 정합니다.
     *
     * <p>moduleId 는 **Module Namespace 값**이며 Business SystemCode 가 아니다(Harness 30.19).
     * 이전 구현은 SystemCode 정규화로 3자리 규격에 맞추고, 없으면
     * package/class 이름에서 추론하고, 그래도 없으면 {@code CPF} 로 대체했다. 이는 Module /
     * DB Prefix / SystemCode Namespace 를 뒤섞는 동작이라 제거한다. 선언된 module id 를 그대로 쓰고,
     * 없으면 application 이름을, 그마저 없으면 미상으로 남긴다 — 가상 Identity 를 만들지 않는다.</p>
     */
    private String resolveModuleId(ProceedingJoinPoint joinPoint) {
        String configuredModuleId = environment.getProperty("cpf.framework.module-id");
        if (hasText(configuredModuleId)) {
            return configuredModuleId.trim();
        }
        String appName = environment.getProperty("spring.application.name");
        if (hasText(appName)) {
            return appName.trim();
        }
        return "N/A";
    }

    private String requestParameters(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (hasText(queryString)) {
            return CpfMaskingRuntime.mask(queryString);
        }

        if (request.getParameterMap().isEmpty()) {
            return "None";
        }

        String parameters = request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + Arrays.toString(entry.getValue()))
                .collect(Collectors.joining("&"));
        return CpfMaskingRuntime.mask(parameters);
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

        return value.equals("[]") ? "None" : CpfMaskingRuntime.mask(value);
    }

    private ResponseMetadata resolveResponseMetadata(Object result, String moduleId) {
        int httpStatus = resolveHttpStatus(result, 200);
        Object body = result instanceof ResponseEntity<?> responseEntity ? responseEntity.getBody() : result;
        String normalizedModuleId = hasText(moduleId) && !"N/A".equals(moduleId) ? moduleId : "CPF";
        String responseCode = firstText(bodyProperty(body, "statusCode"), bodyProperty(body, "code"));
        String fallbackCode = httpStatus >= 400
                ? "E" + normalizedModuleId + "990000"
                : "S" + normalizedModuleId + "000000";
        String normalizedResponseCode = firstText(responseCode, fallbackCode);
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
            return responseCodeResolver.resolve(cpfException.getErrorCode(), Locale.KOREAN, cpfException.getMessageArguments(), cpfException.getDetail()).httpStatus();
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
                    firstText(cpfException.fallbackError().defaultExternalMessage(), resolved.externalMessage()),
                    firstText(cpfException.fallbackError().defaultInternalMessage(), resolved.internalMessage()),
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

    private String canonicalErrorResponse(ResponseMetadata metadata, String transactionId) {
        return "{\"code\":\"" + jsonEscape(metadata.responseCode())
                + "\",\"message\":\"" + jsonEscape(metadata.externalMessage())
                + "\",\"transactionId\":\"" + jsonEscape(transactionId)
                + "\",\"executionId\":\""
                + jsonEscape(com.cpf.core.api.context.CpfContexts.currentExecutionId()) + "\"}";
    }

    private String canonicalErrorResponse(ErrorMetadata metadata, String transactionId) {
        return "{\"code\":\"" + jsonEscape(metadata.responseCode())
                + "\",\"message\":\"" + jsonEscape(metadata.externalMessage())
                + "\",\"transactionId\":\"" + jsonEscape(transactionId)
                + "\",\"executionId\":\""
                + jsonEscape(com.cpf.core.api.context.CpfContexts.currentExecutionId()) + "\"}";
    }

    private String jsonEscape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
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
        return request.getRequestURI() + "?" + CpfMaskingRuntime.mask(queryString);
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
        if (className == null) return "N/A";
        String requiredClassName = Objects.requireNonNull(className);
        int lastDot = requiredClassName.lastIndexOf('.');
        return lastDot > 0 ? requiredClassName.substring(0, lastDot) : "N/A";
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
