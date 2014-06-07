package com.mercury.service;

import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.annotation.Resource;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.zeromq.ZMQ;
import org.zeromq.ZMQException;

import com.mercury.util.MQClient;

@Service
public class MarketSubscriberService {

  private static final Logger LOG = LoggerFactory.getLogger(MarketSubscriberService.class);

  @Value("${subscriber.relay}")
  private String relay;

  @Resource
  private MQClient messageQueue;

  public void drink() {
    ZMQ.Context context = ZMQ.context(1);
    ZMQ.Socket subscriber = context.socket(ZMQ.SUB);

    // Connect to the first publicly available relay.
    subscriber.connect(relay);

    // Disable filtering.
    subscriber.subscribe(new byte[0]);

    while (true) {
      try {
        // Receive compressed raw market data.
        byte[] receivedData = subscriber.recv(0);

        // We build a large enough buffer to contain the decompressed data.
        byte[] decompressed = new byte[receivedData.length * 16];

        // Decompress the raw market data.
        Inflater inflater = new Inflater();
        inflater.setInput(receivedData);
        int decompressedLength = inflater.inflate(decompressed);
        inflater.end();

        byte[] output = new byte[decompressedLength];
        System.arraycopy(decompressed, 0, output, 0, decompressedLength);

        // Transform data into JSON strings.
        String marketJson = new String(output, "UTF-8");

        // Un-serialize the JSON data.
        JSONParser parser = new JSONParser();
        JSONObject marketData = (JSONObject) parser.parse(marketJson);

        LOG.trace(marketJson);
        messageQueue.put("emdr", marketData);
      } catch (ZMQException | DataFormatException | UnsupportedEncodingException | ParseException ex) {
        LOG.warn("ZMQ Exception occurred : {}", ex.getMessage());
      }
    }
  }
}
