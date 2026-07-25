package com.cpf.member.operation;

import com.cpf.core.api.admin.CpfOwnerAdminCommand;
import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.execution.CpfSharedApi;
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
@CpfSharedApi(
        id = "SMBRAD0001",
        name = "MbrAdminOperations",
        ownerDomain = "MBR",
        description = "ADM Control Plane용 MBR Owner 운영 계약",
        allowedCallers = {"ADM"})
public class MbrAdminOperationsController {
    private final CpfOwnerAdminOperationsPort operations;

    public MbrAdminOperationsController(
            @Qualifier("mbrOwnerAdminOperationsPort") CpfOwnerAdminOperationsPort operations) {
        this.operations = operations;
    }

    @PostMapping("/query")
    public Map<String, Object> query(@RequestBody CpfOwnerAdminQuery query) {
        return operations.query(query);
    }

    @PostMapping("/command")
    public Map<String, Object> command(@RequestBody CpfOwnerAdminCommand command) {
        return operations.command(command);
    }
}
