package cpf.acc.bse.mapper;

import cpf.acc.bse.entity.AccAccount;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccAccountMapper {
    List<AccAccount> selectAllAccounts();
}
