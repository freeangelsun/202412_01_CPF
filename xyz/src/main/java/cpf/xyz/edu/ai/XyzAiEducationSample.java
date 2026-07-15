package cpf.xyz.edu.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.pfw.common.ai.CpfAiProviderPort;
import cpf.pfw.common.ai.CpfAiSafetyGuard;
import cpf.pfw.common.ai.CpfEmbeddingPort;
import cpf.pfw.common.ai.CpfVectorStorePort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 외부 AI 자격정보 없이 PFW AI port 사용법을 검증하는 deterministic 교육 샘플입니다.
 *
 * <p>실제 provider adapter를 붙일 때도 이 클래스처럼 입력 안전성 검사, 구조화 출력 검증,
 * fallback, 사용량 계측, RAG 출처, 비동기 승인 경계를 orchestration 계층에서 유지합니다.</p>
 */
@Component
public class XyzAiEducationSample {
    private static final String RESPONSE_SCHEMA = "{answer:string,confidence:number}";

    private final ObjectMapper objectMapper;
    private final CpfAiProviderPort primaryProvider;
    private final CpfAiProviderPort fallbackProvider;
    private final CpfEmbeddingPort embeddingPort;
    private final CpfVectorStorePort vectorStorePort;
    private final ConcurrentMap<String, AiJob> jobs = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();
    private final AtomicLong fallbackCount = new AtomicLong();
    private final AtomicLong inputTokens = new AtomicLong();
    private final AtomicLong outputTokens = new AtomicLong();

    public XyzAiEducationSample(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.primaryProvider = new DeterministicProvider("cpf-deterministic-primary", true, objectMapper);
        this.fallbackProvider = new DeterministicProvider("cpf-deterministic-fallback", false, objectMapper);
        this.embeddingPort = new DeterministicEmbeddingAdapter();
        this.vectorStorePort = new InMemoryVectorStoreAdapter();
        indexReferenceDocuments();
    }

    /** 입력 마스킹, injection 방어, retry·fallback, JSON schema 검증을 한 흐름으로 보여줍니다. */
    public AiResult ask(AiQuestion question) {
        String safePrompt = CpfAiSafetyGuard.inspectAndMask(question.prompt());
        CpfAiProviderPort.CpfAiRequest request = request(safePrompt, question.toolEnabled(), List.of()).build();
        CpfAiProviderPort.CpfAiResponse response = invokeWithFallback(request);
        JsonNode structured = validateStructured(response.output());
        recordUsage(response.usage());
        requests.incrementAndGet();
        return new AiResult(
                structured.path("answer").asText(), structured.path("confidence").asDouble(),
                response.model(), response.toolCalls(), response.citations(), response.usage(), false);
    }

    /** streaming adapter가 순서와 완료 marker를 지키는지 확인합니다. */
    public List<CpfAiProviderPort.CpfAiChunk> stream(AiQuestion question) {
        String safePrompt = CpfAiSafetyGuard.inspectAndMask(question.prompt());
        List<CpfAiProviderPort.CpfAiChunk> chunks = primaryProvider.stream(
                request(safePrompt, question.toolEnabled(), List.of()).build());
        if (chunks.isEmpty() || !chunks.get(chunks.size() - 1).completed()) {
            throw new IllegalStateException("AI streaming 완료 marker가 없습니다.");
        }
        return chunks;
    }

    /** embedding과 vector store port로 검색한 문맥과 출처를 응답에 연결합니다. */
    public AiResult rag(String question) {
        String safeQuestion = CpfAiSafetyGuard.inspectAndMask(question);
        List<Double> queryVector = embeddingPort.embed("cpf-deterministic-embedding", List.of(safeQuestion)).get(0).vector();
        List<CpfVectorStorePort.CpfVectorMatch> matches = vectorStorePort.search(queryVector, 2, Map.of("domain", "CPF"));
        List<CpfAiProviderPort.CpfAiCitation> citations = matches.stream()
                .map(match -> new CpfAiProviderPort.CpfAiCitation(
                        match.documentId(), match.title(), match.metadata().getOrDefault("location", "unknown"), match.score()))
                .toList();
        String context = matches.stream().map(CpfVectorStorePort.CpfVectorMatch::content).reduce("", (a, b) -> a + "\n" + b);
        CpfAiProviderPort.CpfAiResponse response = invokeWithFallback(request(safeQuestion, false, citations)
                .withMetadata(Map.of("ragContext", context))
                .build());
        JsonNode structured = validateStructured(response.output());
        recordUsage(response.usage());
        requests.incrementAndGet();
        return new AiResult(
                structured.path("answer").asText(), structured.path("confidence").asDouble(),
                response.model(), response.toolCalls(), citations, response.usage(), false);
    }

    /** 비용이나 영향이 큰 AI 결과를 즉시 적용하지 않고 사람 승인 대기 상태로 저장합니다. */
    public AiJob submitForApproval(AiQuestion question, String requester) {
        String safePrompt = CpfAiSafetyGuard.inspectAndMask(question.prompt());
        String jobId = UUID.randomUUID().toString();
        AiJob job = new AiJob(jobId, "PENDING_APPROVAL", safePrompt, required(requester, "requester"), null, null, Instant.now());
        jobs.put(jobId, job);
        return job;
    }

    /** 승인자와 사유를 남긴 뒤에만 실제 AI provider를 호출합니다. */
    public AiJob approve(String jobId, String approver, String reason) {
        AiJob before = jobs.get(jobId);
        if (before == null) {
            throw new IllegalArgumentException("AI 비동기 작업을 찾을 수 없습니다. jobId=" + jobId);
        }
        if (!"PENDING_APPROVAL".equals(before.status())) {
            return before;
        }
        String approval = required(approver, "approver") + ":" + required(reason, "reason");
        AiResult result = ask(new AiQuestion(before.prompt(), false));
        AiJob completed = new AiJob(
                before.jobId(), "COMPLETED", before.prompt(), before.requester(), approval, result, before.createdAt());
        jobs.put(jobId, completed);
        return completed;
    }

    /** ADM 관제 adapter가 수집할 수 있는 provider·token·fallback 지표입니다. */
    public Map<String, Long> metrics() {
        return Map.of(
                "requests", requests.get(),
                "fallbacks", fallbackCount.get(),
                "inputTokens", inputTokens.get(),
                "outputTokens", outputTokens.get(),
                "pendingApprovals", jobs.values().stream().filter(job -> "PENDING_APPROVAL".equals(job.status())).count());
    }

    private CpfAiProviderPort.CpfAiResponse invokeWithFallback(CpfAiProviderPort.CpfAiRequest request) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                return primaryProvider.complete(request);
            } catch (RuntimeException ex) {
                last = ex;
            }
        }
        fallbackCount.incrementAndGet();
        try {
            return fallbackProvider.complete(request);
        } catch (RuntimeException fallbackFailure) {
            fallbackFailure.addSuppressed(last);
            throw fallbackFailure;
        }
    }

    private CpfAiRequestBuilder request(String prompt, boolean toolEnabled, List<CpfAiProviderPort.CpfAiCitation> citations) {
        List<CpfAiProviderPort.CpfAiTool> tools = toolEnabled
                ? List.of(new CpfAiProviderPort.CpfAiTool(
                "lookupPolicy", "CPF 운영 정책을 조회합니다.", "{policyCode:string}"))
                : List.of();
        return new CpfAiRequestBuilder(prompt, tools, citations);
    }

    private JsonNode validateStructured(String output) {
        String safeOutput = CpfAiSafetyGuard.validateOutput(output);
        try {
            JsonNode node = objectMapper.readTree(safeOutput);
            if (!node.hasNonNull("answer") || !node.path("answer").isTextual()
                    || !node.has("confidence") || !node.path("confidence").isNumber()) {
                throw new IllegalArgumentException("AI 구조화 응답 schema가 일치하지 않습니다.");
            }
            return node;
        } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
            throw new IllegalArgumentException("AI 구조화 응답이 JSON 형식이 아닙니다.", ex);
        }
    }

    private void recordUsage(CpfAiProviderPort.CpfAiUsage usage) {
        if (usage != null) {
            inputTokens.addAndGet(usage.inputTokens());
            outputTokens.addAndGet(usage.outputTokens());
        }
    }

    private void indexReferenceDocuments() {
        List<String> texts = List.of(
                "PFW는 프레임워크 기술 코어와 공통 port를 소유합니다.",
                "CMN은 프레임워크를 사용하는 프로젝트의 업무 공통 기능을 소유합니다.");
        List<CpfEmbeddingPort.CpfEmbedding> embeddings = embeddingPort.embed("cpf-deterministic-embedding", texts);
        vectorStorePort.upsert(List.of(
                document("CPF-PFW", "PFW 소유권", texts.get(0), embeddings.get(0).vector(), "아키텍처 가이드/PFW"),
                document("CPF-CMN", "CMN 소유권", texts.get(1), embeddings.get(1).vector(), "아키텍처 가이드/CMN")));
    }

    private CpfVectorStorePort.CpfVectorDocument document(
            String id, String title, String content, List<Double> vector, String location) {
        return new CpfVectorStorePort.CpfVectorDocument(
                id, title, content, vector, Map.of("domain", "CPF", "location", location));
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " 값은 필수입니다.");
        }
        return value.trim();
    }

    public record AiQuestion(String prompt, boolean toolEnabled) {
    }

    public record AiResult(
            String answer,
            double confidence,
            String model,
            List<CpfAiProviderPort.CpfAiToolCall> toolCalls,
            List<CpfAiProviderPort.CpfAiCitation> citations,
            CpfAiProviderPort.CpfAiUsage usage,
            boolean realProvider) {
    }

    public record AiJob(
            String jobId,
            String status,
            String prompt,
            String requester,
            String approval,
            AiResult result,
            Instant createdAt) {
    }

    private final class CpfAiRequestBuilder {
        private final String prompt;
        private final List<CpfAiProviderPort.CpfAiTool> tools;
        private final List<CpfAiProviderPort.CpfAiCitation> citations;
        private Map<String, String> metadata = Map.of();

        private CpfAiRequestBuilder(
                String prompt,
                List<CpfAiProviderPort.CpfAiTool> tools,
                List<CpfAiProviderPort.CpfAiCitation> citations) {
            this.prompt = prompt;
            this.tools = tools;
            this.citations = citations;
        }

        private CpfAiRequestBuilder withMetadata(Map<String, String> value) {
            metadata = Map.copyOf(value);
            return this;
        }

        private CpfAiProviderPort.CpfAiRequest build() {
            Map<String, String> resolved = new LinkedHashMap<>(metadata);
            if (!citations.isEmpty()) {
                resolved.put("citationCount", String.valueOf(citations.size()));
            }
            return new CpfAiProviderPort.CpfAiRequest(
                    UUID.randomUUID().toString(), "CPF 안전 응답 규칙을 준수하세요.", prompt,
                    "cpf-deterministic-primary", RESPONSE_SCHEMA, tools, resolved);
        }
    }

    private static final class DeterministicProvider implements CpfAiProviderPort {
        private final String model;
        private final boolean primary;
        private final ObjectMapper objectMapper;

        private DeterministicProvider(String model, boolean primary, ObjectMapper objectMapper) {
            this.model = model;
            this.primary = primary;
            this.objectMapper = objectMapper;
        }

        @Override
        public CpfAiResponse complete(CpfAiRequest request) {
            if (primary && request.userPrompt().contains("simulate-primary-failure")) {
                throw new IllegalStateException("교육용 primary provider 실패");
            }
            List<CpfAiToolCall> toolCalls = request.tools().isEmpty()
                    ? List.of()
                    : List.of(new CpfAiToolCall("tool-1", "lookupPolicy", "{\"policyCode\":\"CPF-ARCH\"}"));
            String context = request.metadata().getOrDefault("ragContext", "").trim();
            String answer = context.isBlank()
                    ? "deterministic 응답: " + request.userPrompt()
                    : "검색 문맥 기반 응답: " + context.lines().findFirst().orElse(context);
            try {
                String output = objectMapper.writeValueAsString(Map.of("answer", answer, "confidence", 1.0));
                long input = tokens(request.userPrompt());
                long outputTokens = tokens(output);
                return new CpfAiResponse(
                        output, model, "STOP", toolCalls, List.of(),
                        new CpfAiUsage(input, outputTokens, input + outputTokens, 1));
            } catch (com.fasterxml.jackson.core.JsonProcessingException ex) {
                throw new IllegalStateException("교육용 AI JSON 생성에 실패했습니다.", ex);
            }
        }

        @Override
        public List<CpfAiChunk> stream(CpfAiRequest request) {
            String[] tokens = complete(request).output().split("(?<=\\G.{12})");
            List<CpfAiChunk> chunks = new ArrayList<>();
            for (int index = 0; index < tokens.length; index++) {
                chunks.add(new CpfAiChunk(index + 1, tokens[index], index == tokens.length - 1));
            }
            return List.copyOf(chunks);
        }

        private long tokens(String value) {
            return Math.max(1, (value.length() + 3L) / 4L);
        }
    }

    private static final class DeterministicEmbeddingAdapter implements CpfEmbeddingPort {
        @Override
        public List<CpfEmbedding> embed(String model, List<String> texts) {
            List<CpfEmbedding> result = new ArrayList<>();
            for (int index = 0; index < texts.size(); index++) {
                String text = texts.get(index);
                double length = Math.max(1, text.length());
                List<Double> vector = List.of(
                        text.chars().filter(Character::isLetter).count() / length,
                        text.chars().filter(Character::isDigit).count() / length,
                        text.chars().filter(ch -> ch > 127).count() / length,
                        Math.min(length, 1_000) / 1_000.0);
                result.add(new CpfEmbedding(index, vector, Math.max(1, text.length() / 4L)));
            }
            return List.copyOf(result);
        }
    }

    private static final class InMemoryVectorStoreAdapter implements CpfVectorStorePort {
        private final ConcurrentMap<String, CpfVectorDocument> documents = new ConcurrentHashMap<>();

        @Override
        public void upsert(List<CpfVectorDocument> values) {
            values.forEach(value -> documents.put(value.documentId(), value));
        }

        @Override
        public List<CpfVectorMatch> search(List<Double> queryVector, int limit, Map<String, String> filters) {
            return documents.values().stream()
                    .filter(document -> filters.entrySet().stream()
                            .allMatch(filter -> filter.getValue().equals(document.metadata().get(filter.getKey()))))
                    .map(document -> new CpfVectorMatch(
                            document.documentId(), document.title(), document.content(),
                            cosine(queryVector, document.vector()), document.metadata()))
                    .sorted(Comparator.comparingDouble(CpfVectorMatch::score).reversed())
                    .limit(Math.max(1, Math.min(limit, 20)))
                    .toList();
        }

        private double cosine(List<Double> left, List<Double> right) {
            double dot = 0;
            double leftNorm = 0;
            double rightNorm = 0;
            for (int index = 0; index < Math.min(left.size(), right.size()); index++) {
                dot += left.get(index) * right.get(index);
                leftNorm += left.get(index) * left.get(index);
                rightNorm += right.get(index) * right.get(index);
            }
            return leftNorm == 0 || rightNorm == 0 ? 0 : dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
        }
    }
}
