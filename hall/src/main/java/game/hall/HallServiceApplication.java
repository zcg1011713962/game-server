package game.hall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@MapperScan({"game.hall.mybatis.mapper"})
@SpringBootApplication(scanBasePackages = {"game.hall", "game.common"})
@EnableScheduling
public class HallServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HallServiceApplication.class, args);
    }
}
