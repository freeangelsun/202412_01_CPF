package com.cpf.core.api.page;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Paging/Cursor 공개 계약의 정상·경계·공격 입력을 고정합니다. */
class CpfPagingContractTest {
    @Test
    void pageRequest기본값최대값0크기overflow를검증한다() {
        assertEquals(new CpfPageRequest(0, 20), CpfPageRequest.of(null, null));
        assertEquals(200, new CpfPageRequest(0, 200).size());
        assertThrows(IllegalArgumentException.class, () -> new CpfPageRequest(-1, 20));
        assertThrows(IllegalArgumentException.class, () -> new CpfPageRequest(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new CpfPageRequest(0, 201));
        assertThrows(ArithmeticException.class, () -> new CpfPageRequest(Integer.MAX_VALUE, 200).offset());
    }

    @Test
    void page는null목록과전체0건및마지막페이지를일관되게표현한다() {
        CpfPage<String> empty = new CpfPage<>(null, 0, 0, 20);
        assertTrue(empty.items().isEmpty());
        assertEquals(0, empty.totalPages());
        assertFalse(empty.hasNext());
        assertFalse(empty.hasPrevious());

        CpfPage<String> last = new CpfPage<>(List.of("x"), 5, 2, 2);
        assertEquals(3, last.totalPages());
        assertFalse(last.hasNext());
        assertTrue(last.hasPrevious());
        assertThrows(IllegalArgumentException.class, () -> CpfPage.of(List.of(), null, 0));
    }

    @Test
    void slice는lookAhead한건만소비하고잘못된metadata를거부한다() {
        CpfSlice<Integer> slice = CpfSlice.fromLookAhead(List.of(1, 2, 3), 0, 2);
        assertEquals(List.of(1, 2), slice.items());
        assertTrue(slice.hasNext());
        assertThrows(IllegalArgumentException.class, () -> CpfSlice.fromLookAhead(List.of(), -1, 2));
        assertThrows(IllegalArgumentException.class, () -> CpfSlice.fromLookAhead(List.of(), 0, 0));
    }

    @Test
    void sort는default방향과공격문자열을RepositoryAllowList책임으로분리한다() {
        assertEquals(CpfSortDirection.ASC, CpfSortDirection.from(null));
        assertEquals(CpfSortDirection.DESC, CpfSortDirection.from(" desc "));
        assertThrows(IllegalArgumentException.class, () -> CpfSortDirection.from("DROP TABLE"));
        assertEquals("createdAt", new CpfSort(" createdAt ", null).field());
        assertThrows(IllegalArgumentException.class, () -> new CpfSort("   ", CpfSortDirection.ASC));
    }

    @Test
    void signedCursor는tamper약한Secret형식오류를failClosed한다() {
        byte[] secret = "0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        CpfHmacCursorCodec codec = new CpfHmacCursorCodec(secret);
        String token = codec.encode("id=100&sort=id");
        assertEquals("id=100&sort=id", codec.decode(token));
        assertThrows(IllegalArgumentException.class, () -> codec.decode(token + "x"));
        assertThrows(IllegalArgumentException.class, () -> codec.decode("v2.bad.bad"));
        assertThrows(IllegalArgumentException.class, () -> new CpfHmacCursorCodec(new byte[8]));
    }
}
