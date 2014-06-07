package com.mercury;

import com.mercury.service.MarketSubscriberService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class App {
  public static void main(String[] args) {
    ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
    MarketSubscriberService firehose = context.getBean(MarketSubscriberService.class);
    firehose.drink();
  }
}
