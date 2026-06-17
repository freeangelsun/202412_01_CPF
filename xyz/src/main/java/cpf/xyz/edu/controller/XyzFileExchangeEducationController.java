package cpf.xyz.edu.controller;

import cpf.cmn.fle.core.CmnFileProtocol;
import cpf.cmn.fle.core.CmnFileExchangeHistoryRecord;
import cpf.cmn.fle.core.CmnFileTransferDirection;
import cpf.cmn.fle.core.CmnFileTransferRequest;
import cpf.cmn.fle.core.CmnFileTransferResult;
import cpf.cmn.fle.core.CmnRemoteCommandRequest;
import cpf.cmn.fle.core.CmnRemoteCommandResult;
import cpf.cmn.fle.service.CmnFileExchangeService;
import cpf.pfw.common.logging.FpsTransaction;
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
 * CMN ?뚯씪/?먭꺽 ?곌퀎 怨듯넻 ⑤뱢 ?ъ슜踰뺤쓣 蹂댁뿬二쇰뒗 援먯쑁??而⑦듃濡ㅻ윭?낅땲??
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 08. ?뚯씪/?먭꺽 ?곌퀎", description = "濡쒖뺄 ?뚯씪, FTP/SFTP/SCP, SSH ?곌퀎 ?섑뵆")
public class XyzFileExchangeEducationController {
    private final CmnFileExchangeService fileExchangeService;

    public XyzFileExchangeEducationController(CmnFileExchangeService fileExchangeService) {
        this.fileExchangeService = fileExchangeService;
    }

    /**
     * CMN ?뚯씪 ?곌퀎 怨듯넻?쇰줈 濡쒖뺄 ?뚯씪???앹꽦?⑸땲??
     *
     * @param path     湲곗? ?붾젆?곕━ ?꾨옒 ?곷? 寃쎈줈
     * @param contents ?뚯씪 ?댁슜
     * @return ?앹꽦 寃곌낵
     */
    @PostMapping("/file-exchange/local/write")
    @FpsTransaction(id = "XYZ09EDU0014", name = "XYZ援먯쑁濡쒖뺄?뚯씪?곌린")
    @Operation(summary = "濡쒖뺄 ?뚯씪 ?곌린 ?섑뵆", description = "CMN ?뚯씪 ?곌퀎 怨듯넻 湲곗? ?붾젆?곕━ ?꾨옒???뚯씪???앹꽦?⑸땲??")
    public ResponseEntity<Map<String, Object>> writeLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path,
            @RequestParam(defaultValue = "XYZ 援먯쑁???뚯씪 ?곌퀎 ?섑뵆") String contents) {
        Path writtenPath = fileExchangeService.writeText(path, contents);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", writtenPath.toString());
        response.put("contents", contents);
        response.put("files", fileExchangeService.list("edu"));
        return ResponseEntity.ok(response);
    }

    /**
     * CMN ?뚯씪 ?곌퀎 怨듯넻?쇰줈 濡쒖뺄 ?뚯씪???쎌뒿?덈떎.
     *
     * @param path 湲곗? ?붾젆?곕━ ?꾨옒 ?곷? 寃쎈줈
     * @return ?뚯씪 ?댁슜
     */
    @GetMapping("/file-exchange/local/read")
    @FpsTransaction(id = "XYZ09EDU0015", name = "XYZ援먯쑁濡쒖뺄?뚯씪?쎄린")
    @Operation(summary = "濡쒖뺄 ?뚯씪 ?쎄린 ?섑뵆", description = "CMN ?뚯씪 ?곌퀎 怨듯넻 湲곗? ?붾젆?곕━ ?꾨옒 ?뚯씪???쎌뒿?덈떎.")
    public ResponseEntity<Map<String, Object>> readLocalFile(
            @RequestParam(defaultValue = "edu/sample.txt") String path) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("path", path);
        response.put("contents", fileExchangeService.readText(path));
        return ResponseEntity.ok(response);
    }

    /**
     * FTP/SFTP/SCP ?뚯씪 ?꾩넚 怨꾪쉷???앹꽦?⑸땲??
     *
     * @return ?뚯씪 ?꾩넚 怨꾪쉷 ?먮뒗 ?ㅽ뻾 寃곌낵
     */
    @PostMapping("/file-exchange/transfer-plan")
    @FpsTransaction(id = "XYZ09EDU0016", name = "XYZ援먯쑁?뚯씪?꾩넚怨꾪쉷")
    @Operation(summary = "FTP/SFTP/SCP ?뚯씪 ?꾩넚 怨꾪쉷 ?섑뵆", description = "CMN ?뚯씪 ?곌퀎 怨듯넻?쇰줈 ?먭꺽 ?뚯씪 ?꾩넚 낅졊 怨꾪쉷???앹꽦?⑸땲??")
    public ResponseEntity<CmnFileTransferResult> buildFileTransferPlan(
            @RequestParam(defaultValue = "SCP") CmnFileProtocol protocol,
            @RequestParam(defaultValue = "UPLOAD") CmnFileTransferDirection direction,
            @RequestParam(defaultValue = "localhost") String host,
            @RequestParam(defaultValue = "appuser") String username,
            @RequestParam(defaultValue = "edu/sample.txt") String localPath,
            @RequestParam(defaultValue = "/data/fps/sample.txt") String remotePath) {
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
     * SSH ?먭꺽 낅졊 怨꾪쉷???앹꽦?⑸땲??
     *
     * @return SSH 낅졊 怨꾪쉷 ?먮뒗 ?ㅽ뻾 寃곌낵
     */
    @PostMapping("/remote/ssh/command-plan")
    @FpsTransaction(id = "XYZ09EDU0017", name = "XYZ援먯쑁SSH낅졊怨꾪쉷")
    @Operation(summary = "SSH 낅졊 怨꾪쉷 ?섑뵆", description = "CMN ?먭꺽 ?곌퀎 怨듯넻?쇰줈 SSH 낅졊 怨꾪쉷???앹꽦?⑸땲??")
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
     * CMN ?뚯씪/?먭꺽 ?곌퀎 理쒓렐 ?대젰??議고쉶?⑸땲??
     *
     * @return 理쒓렐 ?뚯씪/?먭꺽 ?곌퀎 ?대젰
     */
    @GetMapping("/file-exchange/history")
    @FpsTransaction(id = "XYZ09EDU0020", name = "XYZ援먯쑁?뚯씪?곌퀎?대젰議고쉶")
    @Operation(summary = "?뚯씪/?먭꺽 ?곌퀎 ?대젰 議고쉶 ?섑뵆", description = "CMN ?뚯씪 ?곌퀎 怨듯넻??湲곕줉??理쒓렐 泥섎━ ?대젰??議고쉶?⑸땲??")
    public ResponseEntity<List<CmnFileExchangeHistoryRecord>> findFileExchangeHistory() {
        return ResponseEntity.ok(fileExchangeService.findRecentHistory());
    }
}

