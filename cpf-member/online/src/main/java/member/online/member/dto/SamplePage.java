package member.online.member.dto;

import java.util.List;
import member.online.member.model.SampleItem;

/** Search/Page 표준 응답입니다. */
public record SamplePage(List<SampleItem> items, long total, int page, int size) { }
