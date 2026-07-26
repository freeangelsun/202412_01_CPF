package com.cpf.core.api.page;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class CpfPageTest {
 @Test void calculatesPageState(){ var p=new CpfPage<>(List.of("a","b"),5,0,2); assertEquals(3,p.totalPages()); assertTrue(p.hasNext()); }
 @Test void validatesRequest(){ assertThrows(IllegalArgumentException.class,()->new CpfPageRequest(-1,10)); }
}
