package com.bsharan.demo_project.component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ConditionalOnProperty(prefix = "order", value = "type", havingValue = "offline", matchIfMissing = false) // this bean will only be created when the application.properties file have order.type=offline
//@Profile("prod") -> this means that the bean will only be created if spring.profiles.active=prod is set in application.properties file
public class OfflineOrder implements Order {

    @Lazy
    @Autowired
    Product product;

    public OfflineOrder(Product product){
        this.product = product;
        log.info("OfflineOrder bean being created");
    }

    @PostConstruct
    public void init(){
        log.info("OfflineOrder bean post construct called");
    }

    @PreDestroy
    public void cleanup() {
        log.info("OfflineOrder bean pre destroy");
    }
}
