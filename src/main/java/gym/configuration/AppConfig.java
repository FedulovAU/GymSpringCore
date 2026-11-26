package gym.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan(basePackages = "gym")
@PropertySource(value = "classpath:appConfig.properties")
public class AppConfig {
}
