package cpf.acc.bse.mapper;

import cpf.acc.bse.entity.AccMember;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccMemberMapper {
    List<AccMember> selectAllMembers();
}

