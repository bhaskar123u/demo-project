package com.bsharan.demo_project.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class User {

    private static final Logger log = LoggerFactory.getLogger(User.class);

    private final Order order;

    // constructor injection - all dependency resolved at object creation only, immutability, fail-fast approach(during compile time)
    public User(Order order){
        this.order = order;
        log.info("User constructor called");
    }

    @PostConstruct
    public void init(){
        log.info("User bean post construct called");
        log.info("Order object injected, hashcode {}", order.hashCode());
        log.info("Order object injected, class {}", order.getClass().getName());
        //log.info("Product ordered {},{}",order.getProduct().getProductId(), order.getProduct().getProductName());
    }

    @PreDestroy
    public void cleanup() {
        log.info("User bean pre destroy");
    }
}
