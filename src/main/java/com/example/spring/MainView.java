/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.spring;

import com.vaadin.router.Route;
import com.vaadin.shared.communication.PushMode;
import com.vaadin.shared.ui.Transport;
import com.vaadin.ui.Composite;
import com.vaadin.ui.event.AttachEvent;
import com.vaadin.ui.html.Div;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

/**
 * The main view contains a simple label element and a template element.
 */
@Route("")
public class MainView extends Composite<Div> {
    private static int index = 0;

    public MainView() {
        Properties consumerprops = new Properties();
        consumerprops.put("bootstrap.servers", "localhost:9092");
        consumerprops.put("group.id", Integer.toString(index++));
        consumerprops.put("enable.auto.commit", "true");
        consumerprops.put("auto.commit.interval.ms", "1000");
        consumerprops.put("key.deserializer", StringDeserializer.class.getName());
        consumerprops.put("value.deserializer", StringDeserializer.class.getName());
        ChatTag tag = new ChatTag();

        new Thread(() -> {
            KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerprops);
            consumer.subscribe(Collections.singletonList("chat-input"));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                if (!records.isEmpty()) {
                    getUI().ifPresent(ui -> ui.access(() -> {
                        for (ConsumerRecord<String, String> record : records) {
                            tag.chatOutput(record.value());
                        }
                    }));

                }
            }
        }).start();

        getContent().add(tag);
    }

    public static void sendLine(String line) {
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
        producer.send(new ProducerRecord<>("chat-input", "line", line));
        producer.close();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        //TODO: this does not work or is very hacky. How does one enable push in Vaadin Flow?
        attachEvent.getUI().getPushConfiguration().setPushMode(PushMode.AUTOMATIC);
        attachEvent.getUI().getPushConfiguration().setTransport(Transport.LONG_POLLING);
    }
}
