package com.example.taskmanager.service;

import com.example.taskmanager.aspect.LogExecution;
import com.example.taskmanager.aspect.LogTracking;
import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.dto.TaskStatusDto;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.kafka.KafkaTaskProducer;
import com.example.taskmanager.kafka.KafkaTaskStatusProducer;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final KafkaTaskProducer kafkaTaskProducer;
    private final KafkaTaskStatusProducer kafkaTaskStatusProducer;
    @Value("task_status")
    private String topic;


    @LogExecution
    public TaskDto createTask(TaskDto taskDto) {
        if (taskDto.getTitle() == null || taskDto.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        Task task = taskRepository.save(TaskMapper.toEntity(taskDto));
        return TaskMapper.toDTO(task);
    }

    @LogExecution
    public Optional<TaskDto> getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        if (task.isPresent()) {
            return Optional.of(TaskMapper.toDTO(task.get()));
        } else {
            return Optional.empty();
        }
    }

    @LogTracking
    public List<TaskDto> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(task -> TaskMapper.toDTO(task))
                .collect(Collectors.toList());
    }

    @LogExecution
    public Optional<TaskDto> updateTask(Long id, TaskDto taskDto) {
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task updatedTask = existingTask.get();
            updatedTask.setTitle(taskDto.getTitle());
            if (updatedTask.getStatus() != taskDto.getStatus()) {
                updatedTask.setStatus(taskDto.getStatus());
                TaskStatusDto taskStatusDto = new TaskStatusDto(id, taskDto.getStatus());
                kafkaTaskStatusProducer.sendTo(topic, taskStatusDto);
            }
            updatedTask.setDescription(taskDto.getDescription());
            updatedTask.setUserId(taskDto.getUserId());

            taskRepository.save(updatedTask);
            return Optional.of(TaskMapper.toDTO(updatedTask));

        }
        return Optional.empty();
    }

    @LogTracking
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void registerTasks(List<Task> tasks) {
        log.info("Registering tasks... {}", tasks);
        taskRepository.saveAll(tasks)
                .stream()
                .map(Task::getId)
                .forEach(kafkaTaskProducer::send);
    }

    public List<TaskDto> parseJson() {
        ObjectMapper mapper = new ObjectMapper();

        TaskDto[] tasks;
        try {
            tasks = mapper.readValue(new File("src/main/resources/MOCK_DATA.json"), TaskDto[].class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.asList(tasks);
    }
}
