package fps.cmn.smp.service;

import fps.cmn.smp.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 스프링 애플리케이션 전체 컨텍스트를 로드하여 테스트를 수행합니다.
@SpringBootTest
// 테스트 전용 설정(application-test.yml)을 사용할 수 있지만, 주석 처리되었습니다.
//@TestPropertySource(locations = "classpath:application-test.yml")
// fps.cmn 패키지에서 스프링 빈을 찾아 등록합니다.
@ComponentScan(basePackages = "fps.cmn")
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    /**
     * getAllMembers_ShouldReturnActualDataFromDB
     * - MemberService의 getAllMembers() 메서드가 실제 데이터베이스에서 데이터를 정상적으로 조회하는지 검증합니다.
     */
    @Test
    void getAllMembers_ShouldReturnActualDataFromDB() {
       // When: MemberService를 사용해 모든 회원 데이터를 조회합니다.
        List<Member> members = memberService.getAllMembers();

        // Then: 조회된 데이터가 null이 아니며, 최소 한 건 이상의 데이터가 존재해야 합니다.
        assertThat(members).isNotNull();
        assertThat(members.size()).isGreaterThan(0);

        // Log members 결과로 반환된 회원 데이터를 출력합니다.
        members.forEach(member -> System.out.println("Member: " + member));
    }
}
