package member.online.member.dto;

import java.util.List;
import member.online.member.model.SampleItem;

/** Cursor/Slice 표준 응답입니다. */
public record SampleSlice(List<SampleItem> items, boolean hasNext, Long nextCursor) { }
