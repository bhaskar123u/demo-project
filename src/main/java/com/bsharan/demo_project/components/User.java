package com.bsharan.demo_project.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class User {

    private final Order order;

    // constructor injection - all dependency resolved at object creation only, immutability, fail-fast approach(during compile time)
    public User(Order order){
        this.order = order;
        log.info("User constructor called");
    }

    @PostConstruct
    public void init(){
        log.info("User bean post construct called");
        log.info("Order object injected in User? hashcode, class {} {}", order.hashCode(), order.getClass().getName());
    }

    @PreDestroy
    public void cleanup() {
        log.info("User bean pre destroy");
    }
}
