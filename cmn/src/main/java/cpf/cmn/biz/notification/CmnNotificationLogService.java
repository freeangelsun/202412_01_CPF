package cpf.cmn.biz.notification;

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
 * CMN 공통 알림 로그 서비스입니다.
 *
 * <p>메일, SMS, 푸시 발송 어댑터를 붙이기 전에 업무 모듈이 같은 포맷으로
 * 알림 요청과 결과를 기록할 수 있도록 최소 공통 로그를 제공합니다.</p>
 */
@Service
public class CmnNotificationLogService {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;

    @Autowired
    public CmnNotificationLogService(@Qualifier("cmnBusinessJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public boolean isEnabled() {
        return jdbcTemplateProvider.getIfAvailable() != null;
    }

    public CmnNotificationLogResult register(CmnNotificationLogRequest request) {
        JdbcTemplate jdbcTemplate = requireJdbcTemplate();
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "CMN");
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO cmn_notification_log (
                        notification_type, receiver, title, message, send_status,
                        transaction_id, trace_id, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, 'READY', ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, TextUtils.requireText(request.notificationType(), "notificationType"));
            statement.setString(2, TextUtils.requireText(request.receiver(), "receiver"));
            statement.setString(3, TextUtils.requireText(request.title(), "title"));
            statement.setString(4, TextUtils.requireText(request.message(), "message"));
            statement.setString(5, request.transactionId());
            statement.setString(6, request.traceId());
            statement.setString(7, requestUser);
            statement.setString(8, requestUser);
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("CMN 알림 로그 등록 ID를 확인할 수 없습니다.");
        }
        return new CmnNotificationLogResult(key.longValue(), "READY");
    }

    private JdbcTemplate requireJdbcTemplate() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new IllegalStateException("CMN 업무 공통 DB가 비활성화되어 있습니다.");
        }
        return jdbcTemplate;
    }
}
