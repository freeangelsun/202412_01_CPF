package com.cpf.reference.filetransfer.controller;

import com.cpf.core.common.filetransfer.CpfFileExchangeGateway;
import com.cpf.core.common.filetransfer.CpfFileExchangeHistoryRecord;
import com.cpf.core.common.filetransfer.CpfFileTransferProtocol;
import com.cpf.core.common.filetransfer.CpfFileTransferResult;
import com.cpf.core.common.filetransfer.CpfRemoteCommandPlan;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * 파일 송수신과 원격 명령 실행 계획을 학습하는 EDU API입니다.
 */
@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 08. 파일 연계", description = "로컬 파일, 원격 파일 전송, SSH 명령 계획 샘플")
public class ReferenceFileExchangeEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final CpfFileExchangeGateway fileExchangeGateway;

    public ReferenceFileExchangeEducationController(CpfFileExchangeGateway fileExchangeGateway) {
        this.fileExchangeGateway = fileExchangeGateway;
    }

    @PostMapping("/file-exchange/local/write")
    @CpfOnlineTransaction(id = "OREFAA0080", name = "REFLocalFileWrite")
    @Operation(operationId = "refFileExchangeEducationWriteLocalFile", summary = "로컬 파일 쓰기 샘플", description = "허용된 EDU 경로에 텍스트 파일을 쓰고 파일 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> writeLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path,
            @RequestParam(defaultValue = "CPF 파일 연계 교육 샘플입니다.") String contents) {
        Path writtenPath = fileExchangeGateway.writeText(path, contents, "REF_EDU");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", writtenPath.toString());
        response.put("contents", contents);
        response.put("files", fileExchangeGateway.list("edu", "REF_EDU"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file-exchange/local/read")
    @CpfOnlineTransaction(id = "OREFAA0029", name = "REFLocalFileRead")
    @Operation(operationId = "refFileExchangeEducationReadLocalFile", summary = "로컬 파일 읽기 샘플", description = "파일 연계 서비스가 관리하는 로컬 텍스트 파일을 읽습니다.")
    public ResponseEntity<Map<String, Object>> readLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("contents", fileExchangeGateway.readText(path, "REF_EDU"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/file-exchange/transfer-plan")
    @CpfOnlineTransaction(id = "OREFAA0031", name = "REFFileTransferPlan")
    @Operation(operationId = "refFileExchangeEducationBuildFileTransferPlan", summary = "파일 전송 계획 샘플", description = "SCP/SFTP 같은 원격 파일 전송 요청 구조와 감사 이력 기준을 확인합니다.")
    public ResponseEntity<CpfFileTransferResult> buildFileTransferPlan(
            @RequestParam(defaultValue = "SCP") CpfFileTransferProtocol protocol,
            @RequestParam(defaultValue = "UPLOAD") String direction,
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "edu/sample.txt") String localPath,
            @RequestParam(defaultValue = "/data/cpf/sample.txt") String remotePath) {
        return ResponseEntity.ok(fileExchangeGateway.transfer(
                protocol.name(),
                direction,
                host,
                22,
                "CPF_REF_EDU_FILE_CREDENTIAL",
                localPath,
                remotePath,
                "REF_EDU"));
    }

    @PostMapping("/remote/ssh/command-plan")
    @CpfOnlineTransaction(id = "OREFAA0032", name = "REFSshCommandPlan")
    @Operation(operationId = "refFileExchangeEducationBuildSshCommandPlan", summary = "SSH 명령 계획 샘플", description = "원격 서버 명령 실행 요청과 이력 저장 구조를 확인합니다.")
    public ResponseEntity<CpfRemoteCommandPlan> buildSshCommandPlan(
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "hostname") String command) {
        return ResponseEntity.ok(fileExchangeGateway.planRemoteCommand(
                host,
                22,
                username,
                command,
                "REF_EDU"));
    }

    @GetMapping("/file-exchange/history")
    @CpfOnlineTransaction(id = "OREFAA0035", name = "REFFileExchangeHistory")
    @Operation(operationId = "refFileExchangeEducationFindFileExchangeHistory", summary = "파일 연계 이력 조회", description = "최근 파일 연계 요청과 처리 결과 이력을 조회합니다.")
    public ResponseEntity<List<CpfFileExchangeHistoryRecord>> findFileExchangeHistory() {
        return ResponseEntity.ok(fileExchangeGateway.findRecentHistory());
    }
}

