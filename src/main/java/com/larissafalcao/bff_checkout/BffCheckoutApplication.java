package com.larissafalcao.bff_checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class BffCheckoutApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffCheckoutApplication.class, args);
	}

}
