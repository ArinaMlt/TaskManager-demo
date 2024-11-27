package com.example.taskmanager.kafka;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.service.TaskService;
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
public class KafkaTaskConsumer {

    private final TaskService taskService;

    @KafkaListener(id = "t1_demo",
            topics = "t1_demo_task_registration",
            containerFactory = "kafkaListenerContainerFactory")
    public void listener(@Payload List<TaskDto> messageList,
                         Acknowledgment ack,
                         @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                         @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        log.debug("Task consumer: Обработка новых сообщений");
        try {
            List<Task> tasks = messageList.stream()
                    .map(dto -> {
                        dto.setTitle(key + "@" + dto.getTitle());
                        return TaskMapper.toEntity(dto);
                    })
                    .toList();
            taskService.registerTasks(tasks);
        } finally {
            ack.acknowledge();
        }
        log.debug("Task consumer: записи обработаны");
    }
}
