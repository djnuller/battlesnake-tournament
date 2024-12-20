package dk.jdsj.battlenskae;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@OpenAPIDefinition(
        servers = @Server(url = "https://bs-api.desperate.dk")
)
@SpringBootApplication
@EnableJpaRepositories
public class BattlenskaeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BattlenskaeApplication.class, args);
    }

}
