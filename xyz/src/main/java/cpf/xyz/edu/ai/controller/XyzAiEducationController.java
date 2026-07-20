package cpf.xyz.edu.ai.controller;

import cpf.pfw.common.ai.CpfAiProviderPort;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.xyz.edu.ai.XyzAiEducationSample;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** PFW AI port와 deterministic adapter를 실행해 보는 XYZ 교육 API입니다. */
@RestController
@RequestMapping("/xyz/edu/ai")
@Tag(name = "XYZ-EDU 16. AI", description = "LLM port, 구조화 출력, streaming, tool calling, RAG, 사람 승인 교육 샘플")
public class XyzAiEducationController {
    private final XyzAiEducationSample sample;

    public XyzAiEducationController(XyzAiEducationSample sample) {
        this.sample = sample;
    }

    @PostMapping("/structured")
    @CpfOnlineTransaction(id = "OXYZAA0058", name = "XYZAI구조화응답")
    @Operation(operationId = "xyzAiEducationStructured", summary = "AI 구조화 응답",
            description = "입력 마스킹과 injection 방어 후 JSON schema를 검증하고 retry·model fallback·token 사용량을 반환합니다.")
    public ResponseEntity<XyzAiEducationSample.AiResult> structured(
            @RequestBody XyzAiEducationSample.AiQuestion question) {
        return ResponseEntity.ok(sample.ask(question));
    }

    @PostMapping("/stream")
    @CpfOnlineTransaction(id = "OXYZAA0059", name = "XYZAI스트리밍")
    @Operation(operationId = "xyzAiEducationStream", summary = "AI streaming 응답",
            description = "deterministic provider의 순서화된 chunk와 마지막 완료 marker를 반환합니다.")
    public ResponseEntity<List<CpfAiProviderPort.CpfAiChunk>> stream(
            @RequestBody XyzAiEducationSample.AiQuestion question) {
        return ResponseEntity.ok(sample.stream(question));
    }

    @PostMapping("/rag")
    @CpfOnlineTransaction(id = "OXYZAA0061", name = "XYZAIRAG")
    @Operation(operationId = "xyzAiEducationRag", summary = "AI RAG와 출처",
            description = "embedding·vector store port로 CPF 문서를 검색하고 점수가 포함된 출처를 응답에 연결합니다.")
    public ResponseEntity<XyzAiEducationSample.AiResult> rag(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(sample.rag(request.get("question")));
    }

    @PostMapping("/jobs")
    @CpfOnlineTransaction(id = "OXYZAA0062", name = "XYZAI승인대기등록")
    @Operation(operationId = "xyzAiEducationSubmitJob", summary = "AI 사람 승인 대기 등록",
            description = "영향도 높은 AI 요청을 즉시 실행하지 않고 승인 대기 작업으로 등록합니다.")
    public ResponseEntity<XyzAiEducationSample.AiJob> submit(
            @RequestBody XyzAiEducationSample.AiQuestion question,
            @RequestParam String requester) {
        return ResponseEntity.accepted().body(sample.submitForApproval(question, requester));
    }

    @PostMapping("/jobs/{jobId}/approve")
    @CpfOnlineTransaction(id = "OXYZAA0063", name = "XYZAI승인실행")
    @Operation(operationId = "xyzAiEducationApproveJob", summary = "AI 승인 후 실행",
            description = "승인자와 사유를 기록한 뒤 승인 대기 AI 작업을 한 번만 실행합니다.")
    public ResponseEntity<XyzAiEducationSample.AiJob> approve(
            @PathVariable String jobId,
            @RequestParam String approver,
            @RequestParam String reason) {
        return ResponseEntity.ok(sample.approve(jobId, approver, reason));
    }

    @GetMapping("/metrics")
    @CpfOnlineTransaction(id = "OXYZAA0064", name = "XYZAIMetric조회")
    @Operation(operationId = "xyzAiEducationMetrics", summary = "AI 관제 지표",
            description = "ADM observability 연계 대상으로 요청·fallback·token·승인 대기 건수를 반환합니다.")
    public ResponseEntity<Map<String, Long>> metrics() {
        return ResponseEntity.ok(sample.metrics());
    }
}
