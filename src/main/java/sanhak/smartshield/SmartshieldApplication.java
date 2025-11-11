package sanhak.smartshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import sanhak.smartshield.config.DotenvLoader;

@SpringBootApplication
public class SmartshieldApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(SmartshieldApplication.class)
				.initializers(new DotenvLoader()) // 가장 먼저 실행됨
				.run(args);
	}

}
