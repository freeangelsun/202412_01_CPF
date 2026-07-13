package cpf.xyz.edu.operation;

import cpf.pfw.common.batch.CpfBatchJobLogPath;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM에서 BAT JobInstance 로그를 조회하는 교육 샘플입니다.
 *
 * <p>목록 API는 조건 검색에 사용하고 상세 API는 선택한 JobInstance의 JSON Lines 레코드를
 * 조회할 때 사용합니다. jobName 검증은 PFW 로그 경로 정책을 재사용해 경로 조작 문자열이
 * 운영 조회 URL로 전달되지 않게 합니다.</p>
 */
public class XyzAdmBatchLogQueryEducationSample {

    public Map<String, String> queryUrls(LocalDate businessDate, String jobName, long jobInstanceId) {
        // 경로를 계산하는 호출 자체가 jobName과 식별자 규격을 검증합니다.
        CpfBatchJobLogPath.relativePath(jobName, jobInstanceId, businessDate);
        String date = businessDate.format(DateTimeFormatter.BASIC_ISO_DATE);

        String listUrl = UriComponentsBuilder.fromPath("/adm/api/reliability/batch-job-logs")
                .queryParam("businessDate", date)
                .queryParam("jobName", jobName)
                .queryParam("jobInstanceId", jobInstanceId)
                .queryParam("limit", 100)
                .build()
                .encode()
                .toUriString();
        String detailUrl = UriComponentsBuilder
                .fromPath("/adm/api/reliability/batch-job-logs/{businessDate}/{jobName}/{jobInstanceId}")
                .queryParam("maxRecords", 500)
                .buildAndExpand(date, jobName, jobInstanceId)
                .encode()
                .toUriString();

        Map<String, String> urls = new LinkedHashMap<>();
        urls.put("listUrl", listUrl);
        urls.put("detailUrl", detailUrl);
        urls.put("requiredPermission", "RELIABILITY_READ");
        return urls;
    }
}
