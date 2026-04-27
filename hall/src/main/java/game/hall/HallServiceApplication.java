package game.hall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "game.hall")
public class HallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HallServiceApplication.class, args);
    }
}