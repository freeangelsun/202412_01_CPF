package member.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** member Generated Domain의 선택형 Batch Runtime 진입점입니다. */
@SpringBootApplication(scanBasePackages="member")
public class MemberBatchApplication { public static void main(String[] args) { SpringApplication.run(MemberBatchApplication.class,args); } }
