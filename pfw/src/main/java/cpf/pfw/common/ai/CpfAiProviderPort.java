package cpf.pfw.common.ai;

import java.util.List;
import java.util.Map;

/**
 * LLM 공급자 구현을 업무 코드와 분리하는 PFW AI port입니다.
 *
 * <p>운영 adapter는 외부 SDK 응답을 이 계약으로 변환하고, 업무·EDU 코드는 공급자별 타입을 직접 참조하지 않습니다.</p>
 */
public interface CpfAiProviderPort {

    CpfAiResponse complete(CpfAiRequest request);

    List<CpfAiChunk> stream(CpfAiRequest request);

    record CpfAiRequest(
            String requestId,
            String systemPrompt,
            String userPrompt,
            String model,
            String responseSchema,
            List<CpfAiTool> tools,
            Map<String, String> metadata) {
        public CpfAiRequest {
            tools = tools == null ? List.of() : List.copyOf(tools);
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    record CpfAiResponse(
            String output,
            String model,
            String finishReason,
            List<CpfAiToolCall> toolCalls,
            List<CpfAiCitation> citations,
            CpfAiUsage usage) {
        public CpfAiResponse {
            toolCalls = toolCalls == null ? List.of() : List.copyOf(toolCalls);
            citations = citations == null ? List.of() : List.copyOf(citations);
        }
    }

    record CpfAiChunk(int sequence, String delta, boolean completed) {
    }

    record CpfAiTool(String name, String description, String inputSchema) {
    }

    record CpfAiToolCall(String callId, String toolName, String argumentsJson) {
    }

    record CpfAiCitation(String sourceId, String title, String location, double score) {
    }

    record CpfAiUsage(long inputTokens, long outputTokens, long totalTokens, long elapsedMillis) {
    }
}
