package com.example.taskmanager.service;

import com.example.taskmanager.aspect.LogExecution;
import com.example.taskmanager.aspect.LogTracking;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskStatusDto;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.kafka.KafkaTaskStatusProducer;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final KafkaTaskStatusProducer kafkaTaskStatusProducer;
    @Value("task_status")
    private String topic;


    @LogExecution
    public void createTask(TaskDto taskDto) {
        if (taskDto.getTitle() == null || taskDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        taskRepository.save(TaskMapper.toEntity(taskDto));
    }

    @LogExecution
    public TaskDto getTaskById(Long id) {
        return taskRepository.findById(id)
                .map(TaskMapper::toDTO)
                .orElseThrow(() -> new TaskNotFoundException("Task with id " + id + " not found"));
    }

    @LogTracking
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(task -> TaskMapper.toDTO(task))
                .collect(Collectors.toList());
    }

    @LogExecution
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача с id " + id + " не найдена"));

        existingTask.setTitle(taskDto.getTitle());
        if (existingTask.getStatus() != taskDto.getStatus()) {
            existingTask.setStatus(taskDto.getStatus());
            TaskStatusDto taskStatusDto = new TaskStatusDto(id, taskDto.getStatus());
            kafkaTaskStatusProducer.sendTo(topic, taskStatusDto);
        }
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setUserId(taskDto.getUserId());

        Task updatedTask = taskRepository.save(existingTask);
        return TaskMapper.toDTO(updatedTask);
    }

    @LogTracking
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
