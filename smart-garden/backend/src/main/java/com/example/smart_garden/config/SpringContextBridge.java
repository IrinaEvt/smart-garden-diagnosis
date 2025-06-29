package com.example.smart_garden.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextBridge implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextBridge.context = applicationContext;
        System.out.println("✅ SpringContextBridge: контекстът е зададен!");
    }

    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
    public static ApplicationContext getApplicationContext() {
        return context;
    }
}
