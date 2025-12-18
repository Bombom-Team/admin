package news.bombomadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class BomBomServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(BomBomServerApplication.class, args);
    }

}
