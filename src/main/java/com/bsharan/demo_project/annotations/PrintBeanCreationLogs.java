package com.bsharan.demo_project.annotations;

import com.bsharan.demo_project.model.PrintBeanCreationLogsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PrintBeanCreationLogsRegistrar.class)
public @interface PrintBeanCreationLogs {}
// this is a custom annotation, it prints logs for all beans created for application context
