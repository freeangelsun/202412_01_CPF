package cpf.acc.bse.controller;

import cpf.acc.bse.entity.AccMember;
import cpf.acc.bse.service.AccMemberService;
import cpf.acc.bse.service.CmnMemberProxyService;
import cpf.acc.bse.service.MbrMemberClientService;
import cpf.cmn.smp.entity.Member;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.pfw.common.workflow.FpsWorkflow;
import cpf.pfw.common.workflow.FpsWorkflowFailurePolicy;
import cpf.pfw.common.workflow.FpsWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
@Validated
@Tag(name = "ACC-BSE Member", description = "ACC, CMN, and MBR member integration sample APIs")
public class MemberController {

    private final AccMemberService accMemberService;
    private final CmnMemberProxyService cmnMemberProxyService;
    private final MbrMemberClientService mbrMemberClientService;

    public MemberController(
            AccMemberService accMemberService,
            CmnMemberProxyService cmnMemberProxyService,
            MbrMemberClientService mbrMemberClientService) {
        this.accMemberService = accMemberService;
        this.cmnMemberProxyService = cmnMemberProxyService;
        this.mbrMemberClientService = mbrMemberClientService;
    }

    @GetMapping("/acc")
    @FpsTransaction(id = "ACC01BSE0001", name = "ACCMemberList")
    @Operation(summary = "ACC member list", description = "Reads sample members from the ACC database.")
    public ResponseEntity<Map<String, Object>> getAllAccMembers() {
        List<AccMember> accMembers = accMemberService.getAllAccMembers();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "ACC members fetched successfully");
        response.put("data", accMembers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/cmn")
    @FpsTransaction(id = "ACC01BSE0002", name = "CMNMemberList")
    @Operation(summary = "CMN member list", description = "Reads sample members from the CMN module service.")
    public ResponseEntity<Map<String, Object>> getAllCmnMembers() {
        List<Member> cmnMembers = cmnMemberProxyService.getAllMembersFromCMN();

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "CMN members fetched successfully");
        response.put("data", cmnMembers);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @FpsTransaction(id = "ACC01BSE0003", name = "ACCCMNMemberList")
    @Operation(summary = "ACC and CMN member list", description = "Reads sample members from both ACC and CMN.")
    public ResponseEntity<Map<String, Object>> getAllMembers() {
        List<AccMember> accMembers = accMemberService.getAllAccMembers();
        List<Member> cmnMembers = cmnMemberProxyService.getAllMembersFromCMN();

        Map<String, Object> data = new HashMap<>();
        data.put("accMembers", accMembers);
        data.put("cmnMembers", cmnMembers);

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "ACC and CMN members fetched successfully");
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/mbr/detail")
    @FpsTransaction(id = "ACC08BSE0001", name = "MBRMemberDetail")
    @FpsWorkflow(id = "ACC08BSE9001", name = "MBRMemberDetailWorkflow")
    @FpsWorkflowStep(name = "CallMBRMemberDetail", failurePolicy = FpsWorkflowFailurePolicy.VERIFY)
    @Operation(summary = "MBR member detail", description = "Calls MBR through PFW FpsWebClient and propagates CPF headers.")
    public ResponseEntity<Map<String, Object>> getMbrMemberDetail(
            @RequestParam(name = "memberId")
            @Positive(message = "memberId must be positive")
            Integer memberId) {

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.OK.value());
        response.put("message", "MBR member detail fetched successfully");
        response.put("data", mbrMemberClientService.getMemberDetail(memberId));

        return ResponseEntity.ok(response);
    }
}
