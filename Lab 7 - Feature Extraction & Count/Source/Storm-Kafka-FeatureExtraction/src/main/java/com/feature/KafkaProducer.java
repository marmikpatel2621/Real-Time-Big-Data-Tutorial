<<<<<<< HEAD
package com.feature;

import com.migcomponents.migbase64.Base64;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by user on 26/10/2016.
 */
public class KafkaProducer {
    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();

    public KafkaProducer() {
        properties.put("metadata.broker.list", EnvironmentProperties.kafka_broker);
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        properties.put("message.max.bytes", "10000000");
        producer = new Producer<Integer, String>(new ProducerConfig(properties));
    }


    public static String EncodeVideo(String file){
        String encodedString = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        encodedString = Base64.encodeToString(bytes, true);
        return encodedString;
    }

    public static void sendMessage(String arg) {
        new KafkaProducer(); //Setting properties for kafka producer
        String topic = EnvironmentProperties.KAFKA_TOPIC1;  //Topic Name
        String msg = arg;
        KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, msg);
        System.out.println(msg);
        producer.send(data);

        System.out.println("Video Sent");
        producer.close();
    }
}
=======
package com.feature;

import com.migcomponents.migbase64.Base64;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by user on 26/10/2016.
 */
public class KafkaProducer {
    private static Producer<Integer, String> producer;
    private final Properties properties = new Properties();

    public KafkaProducer() {
        properties.put("metadata.broker.list", EnvironmentProperties.kafka_broker);
        properties.put("serializer.class", "kafka.serializer.StringEncoder");
        properties.put("request.required.acks", "1");
        properties.put("message.max.bytes", "10000000");
        producer = new Producer<Integer, String>(new ProducerConfig(properties));
    }


    public static String EncodeVideo(String file){
        String encodedString = null;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bytes = output.toByteArray();
        encodedString = Base64.encodeToString(bytes, true);
        return encodedString;
    }

    public static void sendMessage(String arg) {
        new KafkaProducer(); //Setting properties for kafka producer
        String topic = EnvironmentProperties.KAFKA_TOPIC1;  //Topic Name
        String msg = arg;
        KeyedMessage<Integer, String> data = new KeyedMessage<Integer, String>(topic, msg);
        System.out.println(msg);
        producer.send(data);

        System.out.println("Video Sent");
        producer.close();
    }
}
>>>>>>> a0495d98d387807115d61aa80b1a900d7b9d45b7
