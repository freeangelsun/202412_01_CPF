package cpf.cmn.smp.mapper;

import cpf.cmn.smp.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MemberMapper {
    List<Member> selectAllMembers();
}

