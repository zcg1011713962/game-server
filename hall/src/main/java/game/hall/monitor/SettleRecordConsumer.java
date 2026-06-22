package game.hall.monitor;

import game.common.entity.SettleRecordQueueDTO;
import game.common.service.RedisSettleService;
import game.hall.service.SettleService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class SettleRecordConsumer {

    @Autowired
    private RedisSettleService redisSettleService;

    @Autowired
    private SettleService settleService;

    private volatile boolean running = true;

    private Thread thread;

    @PostConstruct
    public void start() {
        thread = new Thread(() -> {
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    SettleRecordQueueDTO dto = redisSettleService.popSettleRecord();

                    if (dto == null) {
                        continue;
                    }
                    log.info("结算记录 roomId:{} roundId:{}", dto.getRoomId(), dto.getRoundId());
                    settleService.saveSettleRecord(dto);
                } catch (IllegalStateException e) {
                    if (!running) {
                        break;
                    }
                    log.error("Redis连接已关闭，结算记录消费停止", e);
                    break;

                } catch (Exception e) {
                    if (running) {
                        log.error("结算记录消费异常", e);
                    }
                }
            }

            log.info("结算记录消费者已停止");
        });

        thread.setName("settle-record-consumer");
        thread.setDaemon(false);
        thread.start();
    }

    @PreDestroy
    public void destroy() {
        running = false;

        if (thread != null) {
            thread.interrupt();
        }

        log.info("停止结算记录消费者");
    }
}
