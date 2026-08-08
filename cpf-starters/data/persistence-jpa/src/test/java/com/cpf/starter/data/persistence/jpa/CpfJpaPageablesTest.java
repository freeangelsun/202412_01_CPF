package com.cpf.starter.data.persistence.jpa;
import static org.assertj.core.api.Assertions.*;import com.cpf.core.api.page.*;import java.util.Set;import org.junit.jupiter.api.Test;
class CpfJpaPageablesTest {
 @Test void mapsMultiSortAndEnforcesAllowList(){var request=new CpfPageRequest(0,50);var pageable=CpfJpaPageables.toPageable(request,java.util.List.of(new CpfSort("name",CpfSortDirection.ASC),new CpfSort("createdAt",CpfSortDirection.DESC)),Set.of("name","createdAt"));assertThat(pageable.getPageSize()).isEqualTo(50);assertThat(pageable.getSort().stream().toList()).hasSize(2);}
 @Test void rejectsUnsafeSort(){assertThatThrownBy(()->CpfJpaPageables.toPageable(new CpfPageRequest(0,10),java.util.List.of(new CpfSort("drop table",CpfSortDirection.ASC)),Set.of("name"))).isInstanceOf(IllegalArgumentException.class);}
}
