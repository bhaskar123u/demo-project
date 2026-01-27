package com.bsharan.demo_project.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConditionalOnProperty(prefix = "order", value = "type", havingValue = "online", matchIfMissing = false)
public class OnlineOrder implements Order {

    private static Logger log = LoggerFactory.getLogger(OnlineOrder.class);
    private final Product product;

    public OnlineOrder(Product product){
        this.product = product;
        log.info("OnlineOrder bean being created");
    }

    @PostConstruct
    public void init(){
        log.info("OnlineOrder bean post construct called");
    }

    @PreDestroy
    public void cleanup() {
        log.info("OnlineOrder bean pre destroy");
    }
}
