package external.online.external.dto;

import java.util.List;
import external.online.external.model.SampleItem;

/** Cursor/Slice 표준 응답입니다. */
public record SampleSlice(List<SampleItem> items, boolean hasNext, Long nextCursor) { }
