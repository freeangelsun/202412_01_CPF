package member.sample.dto;

import java.util.List;
import member.sample.model.SampleItem;
import com.cpf.core.api.base.CpfResponse;

/** Search/Page 표준 응답입니다. */
public record SamplePage(List<SampleItem> items, long total, int page, int size) implements CpfResponse { }
