package member.domain.base;

import com.cpf.data.persistence.api.CpfBaseRepository;

/** member MyBatis Repository의 공통 namespace/paging 정책을 제공하는 Domain Base입니다. */
public abstract class MemberBaseRepository extends CpfBaseRepository {
    protected static final String TABLE_PREFIX = "MBR";
    protected final int pageSize(int requested) { return boundedSize(requested, 20, 200); }
    protected final int pageOffset(int page, int size) {
        requireRule(page >= 0, "page는 0 이상이어야 합니다.");
        return Math.multiplyExact(page, pageSize(size));
    }
}
