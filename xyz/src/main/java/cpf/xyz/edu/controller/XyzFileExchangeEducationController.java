package cpf.xyz.edu.controller;

import cpf.cmn.fle.core.CmnFileProtocol;
import cpf.cmn.fle.core.CmnFileExchangeHistoryRecord;
import cpf.cmn.fle.core.CmnFileTransferDirection;
import cpf.cmn.fle.core.CmnFileTransferRequest;
import cpf.cmn.fle.core.CmnFileTransferResult;
import cpf.cmn.fle.core.CmnRemoteCommandRequest;
import cpf.cmn.fle.core.CmnRemoteCommandResult;
import cpf.cmn.fle.service.CmnFileExchangeService;
import cpf.pfw.common.logging.CpfTransaction;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 08. 파일 연계", description = "로컬 파일, 원격 파일 전송, SSH 명령 계획 샘플")
public class XyzFileExchangeEducationController {
    private final CmnFileExchangeService fileExchangeService;

    public XyzFileExchangeEducationController(CmnFileExchangeService fileExchangeService) {
        this.fileExchangeService = fileExchangeService;
    }

    @PostMapping("/file-exchange/local/write")
    @CpfTransaction(id = "XYZ09EDU0014", name = "XYZLocalFileWrite")
    @Operation(summary = "로컬 파일 쓰기 샘플", description = "허용된 EDU 경로에 텍스트 파일을 쓰고 파일 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> writeLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path,
            @RequestParam(defaultValue = "CPF 파일 연계 교육 샘플입니다.") String contents) {
        Path writtenPath = fileExchangeService.writeText(path, contents);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", writtenPath.toString());
        response.put("contents", contents);
        response.put("files", fileExchangeService.list("edu"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file-exchange/local/read")
    @CpfTransaction(id = "XYZ09EDU0015", name = "XYZLocalFileRead")
    @Operation(summary = "로컬 파일 읽기 샘플", description = "파일 연계 서비스가 관리하는 로컬 텍스트 파일을 읽습니다.")
    public ResponseEntity<Map<String, Object>> readLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("contents", fileExchangeService.readText(path));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/file-exchange/transfer-plan")
    @CpfTransaction(id = "XYZ09EDU0016", name = "XYZFileTransferPlan")
    @Operation(summary = "파일 전송 계획 샘플", description = "SCP/SFTP 같은 원격 파일 전송 요청 구조와 감사 이력 기준을 확인합니다.")
    public ResponseEntity<CmnFileTransferResult> buildFileTransferPlan(
            @RequestParam(defaultValue = "SCP") CmnFileProtocol protocol,
            @RequestParam(defaultValue = "UPLOAD") CmnFileTransferDirection direction,
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "edu/sample.txt") String localPath,
            @RequestParam(defaultValue = "/data/cpf/sample.txt") String remotePath) {
        CmnFileTransferRequest request = new CmnFileTransferRequest(
                protocol,
                direction,
                host,
                22,
                username,
                null,
                localPath,
                remotePath,
                "XYZ_EDU");
        return ResponseEntity.ok(fileExchangeService.transfer(request));
    }

    @PostMapping("/remote/ssh/command-plan")
    @CpfTransaction(id = "XYZ09EDU0017", name = "XYZSshCommandPlan")
    @Operation(summary = "SSH 명령 계획 샘플", description = "원격 서버 명령 실행 요청과 이력 저장 구조를 확인합니다.")
    public ResponseEntity<CmnRemoteCommandResult> buildSshCommandPlan(
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "hostname") String command) {
        CmnRemoteCommandRequest request = new CmnRemoteCommandRequest(
                host,
                22,
                username,
                null,
                command,
                "XYZ_EDU");
        return ResponseEntity.ok(fileExchangeService.runSshCommand(request));
    }

    @GetMapping("/file-exchange/history")
    @CpfTransaction(id = "XYZ09EDU0020", name = "XYZFileExchangeHistory")
    @Operation(summary = "파일 연계 이력 조회", description = "최근 파일 연계 요청과 처리 결과 이력을 조회합니다.")
    public ResponseEntity<List<CmnFileExchangeHistoryRecord>> findFileExchangeHistory() {
        return ResponseEntity.ok(fileExchangeService.findRecentHistory());
    }
}

