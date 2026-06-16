package cpf.acc;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AccApplication.class)
class AccApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(AccApplicationTests.class); // SLF4J 濡쒓굅 ?좎뼵

	@Test
	void contextLoads() {
		logger.info("Application Context Loaded Successfully!");
	}
}

