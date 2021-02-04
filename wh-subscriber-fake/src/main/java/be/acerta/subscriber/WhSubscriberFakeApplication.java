package be.acerta.subscriber;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class WhSubscriberFakeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhSubscriberFakeApplication.class, args);
	}

}
