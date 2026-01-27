package com.bsharan.demo_project.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;


public class BeanCreationLogger implements BeanPostProcessor {

    final String RED = "\u001B[31m";
    final String GREEN = "\u001B[32m";
    final String RESET = "\u001B[0m";
    private static final Logger log = LoggerFactory.getLogger(BeanCreationLogger.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        log.info("{}[BEAN INIT]{} {}{}{} -> {}",
                RED, RESET,
                GREEN, beanName, RESET,
                bean.getClass().getName()
        );
        return bean;
    }
}
