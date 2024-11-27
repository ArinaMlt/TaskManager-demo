package com.example.taskmanager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class KafkaTaskProducer {

    private final KafkaTemplate template;

    public KafkaTaskProducer(@Qualifier("task") KafkaTemplate template) {
        this.template = template;
    }

    public void send(Long id) {
        try {
            template.sendDefault(UUID.randomUUID().toString(), id).get();
            template.flush();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void sendTo(String topic, Object o) {
        try {
            template.send(topic, o).get();
            template.flush();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
