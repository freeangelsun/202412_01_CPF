package member.online.member.dto;

/** Offset/Page와 Cursor/Slice가 공유하는 Search 입력 계약입니다. */
public record SampleSearchRequest(String keyword, String statusCode, Integer page, Integer size, Long cursor) {
    public int safePage() { return page == null || page < 0 ? 0 : page; }
    public int safeSize() { return size == null || size <= 0 ? 20 : Math.min(size, 200); }
    public long safeCursor() { return cursor == null || cursor < 0 ? 0L : cursor; }
}
