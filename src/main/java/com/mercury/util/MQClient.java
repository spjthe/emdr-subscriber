package com.mercury.util;

public interface MQClient {

  void put(String queue, Object object);
}
