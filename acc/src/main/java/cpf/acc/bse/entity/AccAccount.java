package cpf.acc.bse.entity;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AccAccount {
    private Integer accountId;
    private String accountNo;
    private String accountName;
    private String accountStatus;
    private BigDecimal balance;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
