package cpf.cmn.smp.service;

import cpf.cmn.smp.entity.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// ?ㅽ봽留??좏뵆由ъ??댁뀡 ?꾩껜 而⑦뀓?ㅽ듃瑜?濡쒕뱶?섏뿬 ?뚯뒪?몃? ?섑뻾?⑸땲??
@SpringBootTest
// ?뚯뒪???꾩슜 ?ㅼ젙(application-test.yml)???ъ슜?????덉?留? 二쇱꽍 泥섎━?섏뿀?듬땲??
//@TestPropertySource(locations = "classpath:application-test.yml")
// cpf.cmn ?⑦궎吏?먯꽌 ?ㅽ봽留?鍮덉쓣 李얠븘 ?깅줉?⑸땲??
@ComponentScan(basePackages = "cpf.cmn")
public class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    /**
     * getAllMembers_ShouldReturnActualDataFromDB
     * - MemberService??getAllMembers() 硫붿꽌?쒓? ?ㅼ젣 ?곗씠?곕쿋?댁뒪?먯꽌 ?곗씠?곕? ?뺤긽?곸쑝濡?議고쉶?섎뒗吏 寃利앺빀?덈떎.
     */
    @Test
    void getAllMembers_ShouldReturnActualDataFromDB() {
       // When: MemberService瑜??ъ슜??紐⑤뱺 ?뚯썝 ?곗씠?곕? 議고쉶?⑸땲??
        List<Member> members = memberService.getAllMembers();

        // Then: 議고쉶???곗씠?곌? null???꾨땲硫? 理쒖냼 ??嫄??댁긽???곗씠?곌? 議댁옱?댁빞 ?⑸땲??
        assertThat(members).isNotNull();
        assertThat(members.size()).isGreaterThan(0);

        // Log members 寃곌낵濡?諛섑솚???뚯썝 ?곗씠?곕? 異쒕젰?⑸땲??
        members.forEach(member -> System.out.println("Member: " + member));
    }
}

