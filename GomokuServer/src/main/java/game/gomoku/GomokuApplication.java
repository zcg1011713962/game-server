package game.gomoku;

import game.gomoku.netty.GomokuServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"game.gomoku", "game.common"})
@EnableScheduling
public class GomokuApplication {
    public static void main(String[] args) {
        SpringApplication.run(GomokuApplication.class, args);
    }

    @Bean
    CommandLineRunner startNetty(GomokuServer server) {
        return args -> server.start();
    }
}
