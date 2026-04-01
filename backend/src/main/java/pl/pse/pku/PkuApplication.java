package pl.pse.pku;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PkuApplication {

	public static void main(String[] args) {
		SpringApplication.run(PkuApplication.class, args);
	}

}
