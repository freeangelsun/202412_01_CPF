package cpf.xyz.edu.controller;

import cpf.pfw.common.filetransfer.CpfFileExchangeGateway;
import cpf.pfw.common.filetransfer.CpfFileExchangeHistoryRecord;
import cpf.pfw.common.filetransfer.CpfFileTransferProtocol;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.filetransfer.CpfRemoteCommandPlan;
import cpf.pfw.common.execution.CpfOnlineTransaction;
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
    private final CpfFileExchangeGateway fileExchangeGateway;

    public XyzFileExchangeEducationController(CpfFileExchangeGateway fileExchangeGateway) {
        this.fileExchangeGateway = fileExchangeGateway;
    }

    @PostMapping("/file-exchange/local/write")
    @CpfOnlineTransaction(id = "OXYZAA0080", name = "XYZLocalFileWrite")
    @Operation(operationId = "xyzFileExchangeEducationWriteLocalFile", summary = "로컬 파일 쓰기 샘플", description = "허용된 EDU 경로에 텍스트 파일을 쓰고 파일 목록을 조회합니다.")
    public ResponseEntity<Map<String, Object>> writeLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path,
            @RequestParam(defaultValue = "CPF 파일 연계 교육 샘플입니다.") String contents) {
        Path writtenPath = fileExchangeGateway.writeText(path, contents, "XYZ_EDU");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", writtenPath.toString());
        response.put("contents", contents);
        response.put("files", fileExchangeGateway.list("edu", "XYZ_EDU"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/file-exchange/local/read")
    @CpfOnlineTransaction(id = "OXYZAA0029", name = "XYZLocalFileRead")
    @Operation(operationId = "xyzFileExchangeEducationReadLocalFile", summary = "로컬 파일 읽기 샘플", description = "파일 연계 서비스가 관리하는 로컬 텍스트 파일을 읽습니다.")
    public ResponseEntity<Map<String, Object>> readLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("contents", fileExchangeGateway.readText(path, "XYZ_EDU"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/file-exchange/transfer-plan")
    @CpfOnlineTransaction(id = "OXYZAA0031", name = "XYZFileTransferPlan")
    @Operation(operationId = "xyzFileExchangeEducationBuildFileTransferPlan", summary = "파일 전송 계획 샘플", description = "SCP/SFTP 같은 원격 파일 전송 요청 구조와 감사 이력 기준을 확인합니다.")
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
                "CPF_XYZ_EDU_FILE_CREDENTIAL",
                localPath,
                remotePath,
                "XYZ_EDU"));
    }

    @PostMapping("/remote/ssh/command-plan")
    @CpfOnlineTransaction(id = "OXYZAA0032", name = "XYZSshCommandPlan")
    @Operation(operationId = "xyzFileExchangeEducationBuildSshCommandPlan", summary = "SSH 명령 계획 샘플", description = "원격 서버 명령 실행 요청과 이력 저장 구조를 확인합니다.")
    public ResponseEntity<CpfRemoteCommandPlan> buildSshCommandPlan(
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "hostname") String command) {
        return ResponseEntity.ok(fileExchangeGateway.planRemoteCommand(
                host,
                22,
                username,
                command,
                "XYZ_EDU"));
    }

    @GetMapping("/file-exchange/history")
    @CpfOnlineTransaction(id = "OXYZAA0035", name = "XYZFileExchangeHistory")
    @Operation(operationId = "xyzFileExchangeEducationFindFileExchangeHistory", summary = "파일 연계 이력 조회", description = "최근 파일 연계 요청과 처리 결과 이력을 조회합니다.")
    public ResponseEntity<List<CpfFileExchangeHistoryRecord>> findFileExchangeHistory() {
        return ResponseEntity.ok(fileExchangeGateway.findRecentHistory());
    }
}

