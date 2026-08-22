package member.sample.repository;

import member.base.MemberBaseRepository;
import member.sample.repository.SampleTransactionMapper;
import member.sample.model.SampleIdempotencyRecord;
import member.sample.model.SampleItem;
import com.cpf.data.persistence.api.CpfRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Sample Transaction의 실제 MyBatis Repository Consumer입니다. */
@CpfRepository
public class SampleTransactionRepository extends MemberBaseRepository {
    private static final Set<String> SORT_COLUMNS = Set.of(
            "sample_item_id", "sample_key", "item_name", "status_code", "created_at");
    private final SampleTransactionMapper mapper;

    /** Mapper를 주입해 Vendor 중립 Persistence 경로를 구성합니다. */
    public SampleTransactionRepository(SampleTransactionMapper mapper) { this.mapper = mapper; }

    public int insert(SampleItem item) { return mapper.insert(item); }
    public Optional<SampleItem> findById(long id) {
        requireRule(id > 0, "sampleItemId는 1 이상이어야 합니다.");
        return Optional.ofNullable(mapper.findById(id));
    }
    /** findBySampleKey 작업을 CPF 표준 계약에 따라 수행한다. */
    public Optional<SampleItem> findBySampleKey(String key) {
        return Optional.ofNullable(mapper.findBySampleKey(requireText(key,"sampleKey")));
    }
    public Optional<SampleIdempotencyRecord> findIdempotency(String key) {
        return Optional.ofNullable(mapper.findIdempotency(requireText(key,"idempotencyKey")));
    }
    public Optional<SampleItem> findForUpdate(long id) {
        requireRule(id > 0, "sampleItemId는 1 이상이어야 합니다.");
        return Optional.ofNullable(mapper.findForUpdate(id));
    }
    /** search 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<SampleItem> search(String keyword, String statusCode, int page, int size, String sortBy, String direction) {
        int normalizedSize = pageSize(size);
        String column = SORT_COLUMNS.contains(sortBy) ? sortBy : "sample_item_id";
        String order = "DESC".equalsIgnoreCase(direction) ? "DESC" : "ASC";
        return mapper.search(normalizeKeyword(keyword), normalizeStatus(statusCode),
                pageOffset(page, normalizedSize), normalizedSize, column, order);
    }
    /** count 작업을 CPF 표준 계약에 따라 수행한다. */
    public long count(String keyword, String statusCode) {
        return mapper.count(normalizeKeyword(keyword), normalizeStatus(statusCode));
    }
    public List<SampleItem> cursorSlice(String keyword, String statusCode, long cursor, int size) {
        requireRule(cursor >= 0, "cursor는 0 이상이어야 합니다.");
        return mapper.cursorSlice(normalizeKeyword(keyword), normalizeStatus(statusCode), cursor,
                boundedSize(size, 21, 201));
    }
    public int insertIdempotency(SampleIdempotencyRecord record) { return mapper.insertIdempotency(record); }
    /** updateWithVersion 작업을 CPF 표준 계약에 따라 수행한다. */
    public int updateWithVersion(SampleItem item) { return mapper.updateWithVersion(item); }
    public int logicalDeleteWithVersion(SampleItem item) { return mapper.logicalDeleteWithVersion(item); }
    private static String normalizeKeyword(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized=value.trim();
        if (normalized.length() > 200) throw new IllegalArgumentException("keyword는 200자 이하여야 합니다.");
        return normalized;
    }
    private static String normalizeStatus(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized=value.trim().toUpperCase(java.util.Locale.ROOT);
        if (!normalized.equals("ACTIVE") && !normalized.equals("INACTIVE"))
            throw new IllegalArgumentException("statusCode는 ACTIVE 또는 INACTIVE여야 합니다.");
        return normalized;
    }
}
