package com.bsharan.demo_project;

import com.bsharan.demo_project.annotations.PrintBeanCreationLogs;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@PrintBeanCreationLogs
@SpringBootApplication
public class DemoProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(DemoProjectApplication.class, args);
	}
}
