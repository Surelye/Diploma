package sgu.borodin.nas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class NetworkAttachedStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetworkAttachedStorageApplication.class, args);
	}

}
