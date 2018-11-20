package br.edu.utfpr.pb.aula6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
public class MainRest extends SpringBootServletInitializer{

    public static void main(String[] args) {
        SpringApplication.run(MainRest.class, args);
    }  
    
}
