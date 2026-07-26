package com.cpf.member.operation;

import com.cpf.member.common.base.MbrBaseController;
import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.execution.CpfSharedApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 분리 WAS에서 ADM이 사용하는 MBR 내부 운영 API입니다.
 * 외부 Gateway 공개 API가 아니며 CPF 내부 서비스 신원 검증 대상입니다.
 */
@RestController
@RequestMapping("/mbr/internal/admin/operations")
@Tag(name = "MBR-Admin-Operations", description = "ADM Control Plane용 MBR Owner 내부 운영 API")
@CpfSharedApi(
        id = "SMBRAD0001",
        name = "MbrAdminOperations",
        ownerDomain = "MBR",
        description = "ADM Control Plane용 MBR Owner 운영 계약",
        allowedCallers = {"ADM"})
public class MbrAdminOperationsController extends MbrBaseController {
    private final CpfOwnerAdminOperationsPort operations;

    public MbrAdminOperationsController(
            @Qualifier("mbrOwnerAdminOperationsPort") CpfOwnerAdminOperationsPort operations) {
        this.operations = operations;
    }

    @PostMapping("/query")
    @Operation(operationId = "mbrAdminOperationsQuery", summary = "MBR Owner 운영 정보 조회")
    public Map<String, Object> query(@RequestBody CpfOwnerAdminQuery query) {
        return operations.query(query);
    }

    @PostMapping("/command")
    @Operation(operationId = "mbrAdminOperationsCommand", summary = "MBR Owner 운영 명령 수행")
    public Map<String, Object> command(@RequestBody CpfOwnerAdminCommand command) {
        return operations.command(command);
    }
}
