package external.sample.dto;

import java.util.List;
import external.sample.model.SampleItem;
import com.cpf.core.api.base.CpfResponse;

/** Cursor/Slice 표준 응답입니다. */
public record SampleSlice(List<SampleItem> items, boolean hasNext, Long nextCursor) implements CpfResponse { }
