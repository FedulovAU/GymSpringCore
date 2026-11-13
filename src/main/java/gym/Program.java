package gym;

import gym.configuration.AppConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Program {
    static void main(String[] args){
        try(var context = new AnnotationConfigApplicationContext(AppConfig.class)){
        }
    }
}
