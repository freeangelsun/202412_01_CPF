package external.domain.model;

import java.util.List;

/** Cursor/Slice 표준 응답입니다. */
public record SampleSlice(List<SampleItem> items, boolean hasNext, Long nextCursor) { }
