package cpf.cmn.biz.log;

import cpf.cmn.utils.TextUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * CMN 공통 업무 로그 서비스입니다.
 *
 * <p>업무 모듈 공통으로 남겨야 하는 상태 변경, 외부 연계 요청, 중요 이벤트를
 * 같은 테이블에 기록하도록 제공하는 샘플 서비스입니다. PFW 거래 로그는 기술 추적 중심이고,
 * CMN 업무 로그는 업무 의미 중심으로 사용합니다.</p>
 */
@Service
public class CmnBusinessLogService extends cpf.cmn.common.base.CmnBaseService {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    @Autowired
    public CmnBusinessLogService(@Qualifier("cmnBusinessJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public boolean isEnabled() {
        return jdbcTemplateProvider.getIfAvailable() != null;
    }

    public CmnBusinessLogResult register(CmnBusinessLogRequest request) {
        JdbcTemplate jdbcTemplate = requireJdbcTemplate();
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "CMN");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO cmn_business_log (
                        business_area, business_key, log_type, log_message, log_payload,
                        transaction_id, trace_id, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, TextUtils.requireText(request.businessArea(), "businessArea"));
            statement.setString(2, TextUtils.requireText(request.businessKey(), "businessKey"));
            statement.setString(3, TextUtils.requireText(request.logType(), "logType"));
            statement.setString(4, TextUtils.requireText(request.logMessage(), "logMessage"));
            statement.setString(5, request.logPayload());
            statement.setString(6, request.transactionId());
            statement.setString(7, request.traceId());
            statement.setString(8, requestUser);
            statement.setString(9, requestUser);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("CMN 업무 로그 등록 ID를 확인할 수 없습니다.");
        }
        return new CmnBusinessLogResult(key.longValue());
    }

    private JdbcTemplate requireJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("CMN 업무 공통 DB가 비활성화되어 있습니다.");
        }
        return jdbcTemplate;
    }
}
