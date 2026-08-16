package member.domain.model;

import java.util.List;

/** Search/Page 표준 응답입니다. */
public record SamplePage(List<SampleItem> items, long total, int page, int size) { }
