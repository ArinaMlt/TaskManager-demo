package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationService {

    public void sendNotification(TaskStatusDto statusDto) {
        String message = "Статус задачи " + statusDto.getId() + " изменен на " + statusDto.getStatus();
        log.info("Send notification: {}", message);
    }
}
