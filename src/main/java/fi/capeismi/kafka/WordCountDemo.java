/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.capeismi.kafka;

import com.example.spring.ExampleServletInitializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.SpringApplication;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Demonstrates, using the high-level KStream DSL, how to implement the WordCount program
 * that computes a simple word occurrence histogram from an input text.
 * <p>
 * In this example, the input stream reads from a topic named "streams-plaintext-input", where the values of messages
 * represent lines of text; and the histogram output is written to topic "streams-wordcount-output" where each record
 * is an updated count of a single word.
 * <p>
 * Before running this example you must create the input topic and the output topic (e.g. via
 * bin/kafka-topics.sh --create ...), and write some data to the input topic (e.g. via
 * bin/kafka-console-producer.sh). Otherwise you won't see any data arriving in the output topic.
 */
public class WordCountDemo implements Runnable {

    @Override
    public void run() {
        Properties pr = new Properties();
        pr.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        pr.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        pr.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        pr.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        pr.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // setting offset reset to earliest so that we can re-run the demo code with the same pre-loaded data
        // Note: To re-run the demo, you need to use the offset reset tool:
        // https://cwiki.apache.org/confluence/display/KAFKA/Kafka+Streams+Application+Reset+Tool
        pr.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KStreamBuilder builder = new KStreamBuilder();

        KStream<String, String> source = builder.stream("streams-plaintext-input");

        KTable<String, Long> counts = source
                .flatMapValues(value -> {
//                    System.out.println("taking in "+value);
                    return Arrays.asList(value.toLowerCase(Locale.getDefault()).split(" "));
                })
                .groupBy((key, value) -> value)
                .count("Count_s");

        // need to override value serde to Long type
        counts.to(Serdes.String(), Serdes.Long(), "streams-wordcount-output");

        final KafkaStreams streams = new KafkaStreams(builder, pr);
        final CountDownLatch latch = new CountDownLatch(1);

        // attach shutdown handler to catch control-c
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            new Thread(() -> {
                Properties p = new Properties();
                p.put("bootstrap.servers", "localhost:9092");
                p.put("acks", "all");
                p.put("retries", 0);
                p.put("batch.size", 16384);
                p.put("linger.ms", 1);
                p.put("buffer.memory", 33554432);
                p.put("key.serializer", org.apache.kafka.common.serialization.StringSerializer.class.getName());
                p.put("value.serializer", org.apache.kafka.common.serialization.StringSerializer.class.getName());

                KafkaProducer<String, String> producer = new KafkaProducer<>(p);
                while(true) {

                    producer.send(new ProducerRecord<String, String>("streams-plaintext-input", "kek", "vek"));
//                    System.out.println("sending kek");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        producer.close();
                        e.printStackTrace();
                    }
                }
            }).start();


            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }
}