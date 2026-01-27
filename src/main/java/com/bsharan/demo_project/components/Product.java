package com.bsharan.demo_project.components;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// @Component -> spring calls default constructor to create bean, if default constructor is not present we can use @Bean
@Component
@Getter
@Setter
public class Product {

    private static final Logger log = LoggerFactory.getLogger(Product.class);
    private final String productName;
    private final long productId;

    Product(){
        this.productId = 12345L;
        this.productName = "defaultName";
        log.info("Product constructor called");
    }

    Product(String productName, long productId){
        this.productId = productId;
        this.productName = productName;
    }

    @PostConstruct
    public void init(){
        log.info("Product post construct is called");
    }

    @PreDestroy
    public void destroy(){
        log.info("Product pre destroy is called");
    }
}
