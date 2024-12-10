package com.example.taskmanager.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaTaskStatusProducer {
    private final KafkaTemplate template;

    public KafkaTaskStatusProducer(@Qualifier("taskStatus") KafkaTemplate template) {
        this.template = template;
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
