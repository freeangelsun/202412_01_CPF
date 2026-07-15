package cpf.pfw.common.remotelog;

import java.util.List;
import java.util.Map;

/** 원격 로그 묶음의 비동기 생성·만료·재다운로드를 관리하는 PFW port입니다. */
public interface CpfRemoteLogBundleJobPort {

    CpfRemoteLogBundleJob submit(String ownerId, List<String> artifactIds);

    CpfRemoteLogBundleJob find(String jobId, String ownerId);

    CpfRemoteLogDownloadGrant issueDownloadGrant(String jobId, String ownerId);

    CpfRemoteLogBundle resolveDownload(String jobId, String ownerId, String token);

    Map<String, Object> diagnostics();
}
