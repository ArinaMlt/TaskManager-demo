package com.example.taskmanager.kafka;

import com.example.taskmanager.dto.TaskStatusDto;
import com.example.taskmanager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaTaskStatusConsumer {
    private final NotificationService notificationService;

    @KafkaListener(id = "taskStatusDtoListener",
            topics = "task_status",
            containerFactory = "taskStatusDtoKafkaListenerContainerFactory")
    public void listener(@Payload List<TaskStatusDto> messageList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Task status consumer: Получено сообщение из топика '{}', с ключом '{}'", topic, key);
        try {
            log.debug("Task status consumer: Начинаем обработку {} сообщений", messageList.size());
            messageList.forEach(message -> notificationService.sendNotification(message));
        } catch (Exception e) {
            log.error("Task status consumer: Ошибка при обработке сообщений", e);
        } finally {
            ack.acknowledge();
        }
        log.debug("Task consumer: записи обработаны");
    }
}
