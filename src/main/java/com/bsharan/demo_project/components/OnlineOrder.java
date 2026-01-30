package com.bsharan.demo_project.components;

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
@ConditionalOnProperty(prefix = "order", value = "type", havingValue = "online", matchIfMissing = false)
public class OnlineOrder implements Order {

    @Lazy
    @Autowired
    Product product;

    public OnlineOrder(){
        log.info("OnlineOrder bean being created");
//        if(this.product != null)
//            log.info(this.product.getClass().getName());
//        else{
//            log.info("product is null");
//        }
    }

    @PostConstruct
    public void init(){
        log.info("OnlineOrder bean post construct called");
        log.info("Product object injected in OnlineOrder? hashcode, class {} {}", product.hashCode(), product.getClass().getName());
    }

    @PreDestroy
    public void cleanup() {
        log.info("OnlineOrder bean pre destroy");
    }
}
