package cpf.pfw.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Enables PFW AOP support such as transaction logging.
 */
@Configuration
@EnableAspectJAutoProxy
public class PfwAopConfig {
}
