package fps.acc.bse.controller;

import fps.acc.bse.entity.AccMember;
import fps.acc.bse.service.AccMemberService;
import fps.acc.bse.service.CmnMemberProxyService;
import fps.acc.bse.service.MbrMemberClientService;
import fps.pfw.common.logging.FpsTransaction;
import fps.pfw.common.workflow.FpsWorkflow;
import fps.pfw.common.workflow.FpsWorkflowFailurePolicy;
import fps.pfw.common.workflow.FpsWorkflowStep;
import fps.cmn.smp.entity.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ACC 기본 업무그룹의 샘플 회원 조회 컨트롤러입니다.
 *
 * <p>이 컨트롤러는 세 가지 호출 방식을 보여줍니다.</p>
 * <p>첫째, ACC 자체 DB를 조회하는 방식입니다.</p>
 * <p>둘째, 같은 JVM에 포함된 CMN 공통 서비스를 직접 호출하는 방식입니다.</p>
 * <p>셋째, PFW 공통 {@code FpsWebClient}를 통해 별도 서비스인 MBR을 HTTP로 호출하는 방식입니다.</p>
 *
 * <p>각 API에는 {@link FpsTransaction}을 선언해 업무 거래ID와 거래명을 로그에 남기며,
 * MBR 연계 샘플에는 {@link FpsWorkflow}와 {@link FpsWorkflowStep}을 추가해
 * 서비스 간 워크플로우 추적 기준을 함께 보여줍니다.</p>
 */
@RestController
@RequestMapping("/members")
@Validated
@Tag(name = "ACC-BSE 계좌기본/회원연계", description = "ACC 샘플 회원 조회와 MBR 서비스 호출 API")
public class MemberController {

    private final AccMemberService accMemberService;
    private final CmnMemberProxyService cmnMemberProxyService;
    private final MbrMemberClientService mbrMemberClientService;

    @Autowired
    public MemberController(
            AccMemberService accMemberService,
            CmnMemberProxyService cmnMemberProxyService,
            MbrMemberClientService mbrMemberClientService) {
        // ACC 샘플 회원 테이블을 조회하는 서비스입니다.
        this.accMemberService = accMemberService;
        // CMN 모듈의 샘플 회원 서비스를 감싸는 프록시 서비스입니다.
        this.cmnMemberProxyService = cmnMemberProxyService;
        // PFW FpsWebClient를 사용해 MBR API를 호출하는 연계 서비스입니다.
        this.mbrMemberClientService = mbrMemberClientService;
    }

    /**
     * ACC DB의 샘플 회원 목록을 조회합니다.
     *
     * @return ACC 샘플 회원 목록과 HTTP 200 상태를 담은 응답입니다.
     */
    @GetMapping("/acc")
    @FpsTransaction(id = "ACC01BSE0001", name = "ACC기본회원목록조회")
    @Operation(summary = "ACC 샘플 회원 목록 조회", description = "ACC DB의 샘플 회원 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getAllAccMembers() {
        // accDB.acc_member 테이블을 조회해 ACC 주제영역의 샘플 회원 목록을 가져옵니다.
        List<AccMember> accMembers = accMemberService.getAllAccMembers();

        // 샘플 응답 구조를 만들기 위해 Map을 사용합니다. 실제 업무에서는 표준 응답 DTO 사용을 권장합니다.
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "ACC members fetched successfully");
        response.put("data", accMembers);

        return ResponseEntity.ok(response);
    }

    /**
     * CMN 모듈에 포함된 샘플 회원 목록을 조회합니다.
     *
     * @return CMN 샘플 회원 목록과 HTTP 200 상태를 담은 응답입니다.
     */
    @GetMapping("/cmn")
    @FpsTransaction(id = "ACC01BSE0002", name = "CMN회원목록조회샘플")
    @Operation(summary = "CMN 샘플 회원 목록 조회", description = "ACC에서 같은 JVM에 포함된 CMN 공통 서비스를 직접 호출합니다.")
    public ResponseEntity<Map<String, Object>> getAllCmnMembers() {
        // CMN은 별도 서버가 아니라 현재 ACC 애플리케이션에 라이브러리로 포함된 공통 모듈입니다.
        List<Member> cmnMembers = cmnMemberProxyService.getAllMembersFromCMN();

        // 호출 결과를 단순 Map으로 포장해 화면/테스트에서 확인하기 쉽게 반환합니다.
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "CMN members fetched successfully");
        response.put("data", cmnMembers);

        return ResponseEntity.ok(response);
    }

    /**
     * ACC 샘플 회원과 CMN 샘플 회원을 한 번에 조회합니다.
     *
     * @return ACC/CMN 양쪽 목록을 함께 담은 응답입니다.
     */
    @GetMapping("/all")
    @FpsTransaction(id = "ACC01BSE0003", name = "ACC_CMN회원통합조회샘플")
    @Operation(summary = "ACC/CMN 통합 목록 조회", description = "ACC 샘플 회원과 CMN 샘플 회원을 한 응답에서 비교합니다.")
    public ResponseEntity<Map<String, Object>> getAllMembers() {
        // ACC 주제영역 DB에서 관리하는 샘플 회원 목록입니다.
        List<AccMember> accMembers = accMemberService.getAllAccMembers();
        // CMN 공통 모듈에서 제공하는 샘플 회원 목록입니다.
        List<Member> cmnMembers = cmnMemberProxyService.getAllMembersFromCMN();

        // 두 출처의 데이터를 한 응답 안에 구분해서 담습니다.
        Map<String, Object> data = new HashMap<>();
        data.put("accMembers", accMembers);
        data.put("cmnMembers", cmnMembers);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "ACC and CMN members fetched successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    /**
     * ACC에서 MBR 회원 상세조회 API를 호출하는 샘플입니다.
     *
     * <p>공통 WebClient가 현재 거래 헤더와 워크플로우 헤더를 MBR로 자동 전달합니다.
     * 이 메서드는 다른 주제영역 호출이 실패했을 때 무조건 롤백하지 않고,
     * {@link FpsWorkflowFailurePolicy#VERIFY} 상태로 남겨 확인/대사 후 처리할 수 있음을 보여줍니다.</p>
     *
     * @param memberId MBR에서 조회할 회원 ID입니다. 양수만 허용합니다.
     * @return MBR 회원 상세조회 결과를 담은 응답입니다.
     */
    @GetMapping("/mbr/detail")
    @FpsTransaction(id = "ACC08BSE0001", name = "MBR회원상세조회연계샘플")
    @FpsWorkflow(id = "ACC08BSE9001", name = "MBR회원상세조회연계워크플로우")
    @FpsWorkflowStep(name = "MBR회원상세조회호출", failurePolicy = FpsWorkflowFailurePolicy.VERIFY)
    @Operation(summary = "MBR 회원 상세 연계 조회", description = "PFW FpsWebClient로 MBR을 호출하고 거래/워크플로우 헤더를 전파합니다.")
    public ResponseEntity<Map<String, Object>> getMbrMemberDetail(
            @RequestParam(name = "memberId")
            @Positive(message = "회원 ID는 양수여야 합니다.")
            Integer memberId) {

        // 응답 Map은 샘플용입니다. 실제 업무 API에서는 표준 응답 객체를 적용하는 것이 좋습니다.
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "MBR member detail fetched successfully");
        // FpsWebClient가 X-Transaction-Id, X-Trace-Id, X-Workflow-* 헤더를 MBR 호출에 자동 전파합니다.
        response.put("data", mbrMemberClientService.getMemberDetail(memberId));

        return ResponseEntity.ok(response);
    }
}
