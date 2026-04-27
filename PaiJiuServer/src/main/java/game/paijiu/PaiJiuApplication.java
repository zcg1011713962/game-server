package game.paijiu;

import game.paijiu.netty.PaiJiuServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "game.paijiu")
public class PaiJiuApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaiJiuApplication.class, args);
    }

    @Bean
    public CommandLineRunner startNetty(PaiJiuServer paiJiuServer) {
        return args -> paiJiuServer.start();
    }
}