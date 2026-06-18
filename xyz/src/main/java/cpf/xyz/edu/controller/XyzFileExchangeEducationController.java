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
 * CPF 기능 설명입니다.
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
public class XyzFileExchangeEducationController {
    private final CmnFileExchangeService fileExchangeService;

    public XyzFileExchangeEducationController(CmnFileExchangeService fileExchangeService) {
        this.fileExchangeService = fileExchangeService;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PostMapping("/file-exchange/local/write")
    @CpfTransaction(id = "XYZ09EDU0014", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> writeLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path,
            @RequestParam(defaultValue = "CPF 처리 기준입니다.") String contents) {
        Path writtenPath = fileExchangeService.writeText(path, contents);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", writtenPath.toString());
        response.put("contents", contents);
        response.put("files", fileExchangeService.list("edu"));
        return ResponseEntity.ok(response);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/file-exchange/local/read")
    @CpfTransaction(id = "XYZ09EDU0015", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> readLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("contents", fileExchangeService.readText(path));
        return ResponseEntity.ok(response);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @PostMapping("/file-exchange/transfer-plan")
    @CpfTransaction(id = "XYZ09EDU0016", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
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

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @PostMapping("/remote/ssh/command-plan")
    @CpfTransaction(id = "XYZ09EDU0017", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
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

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @GetMapping("/file-exchange/history")
    @CpfTransaction(id = "XYZ09EDU0020", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<List<CmnFileExchangeHistoryRecord>> findFileExchangeHistory() {
        return ResponseEntity.ok(fileExchangeService.findRecentHistory());
    }
}

