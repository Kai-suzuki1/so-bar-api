package app.diy.note_taking_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import app.diy.note_taking_app.configuration.NoteTakingAppConfigProperties;

@SpringBootApplication
@EnableConfigurationProperties(NoteTakingAppConfigProperties.class)
public class NoteTakingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(NoteTakingAppApplication.class, args);
	}

}
