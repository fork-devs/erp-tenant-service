package com.erp.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement(order = 0)
public class ErpTenantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpTenantServiceApplication.class, args);
    }

}
