package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {
    // Преобразование Task в TaskDTO
    public static TaskDto toDTO(Task task) {
        if (task == null) {
            return null;
        }
        return new TaskDto(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                task.getUserId()
        );
    }

    // Преобразование TaskDTO в Task
    public static Task toEntity(TaskDto taskDTO) {
        if (taskDTO == null) {
            return null;
        }
        return new Task(
                taskDTO.getId(),
                taskDTO.getTitle(),
                taskDTO.getStatus(),
                taskDTO.getDescription(),
                taskDTO.getUserId()
        );
    }
}
