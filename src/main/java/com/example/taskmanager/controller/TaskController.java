package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.kafka.KafkaTaskProducer;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/tasks")
@Tag(name = "Task controller", description = "API для управления задачами")
public class TaskController {
    private final TaskService taskService;
    private final KafkaTaskProducer kafkaTaskProducer;
    @Value("t1_demo_task_registration")
    private String topic;

    @Operation(summary = "Создать новую задачу")
    @PostMapping
    public ResponseEntity<TaskDto> postTask(TaskDto taskDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(taskDto));
    }

    @Operation(summary = "Получить все задачи")
    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу, если она существует")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<TaskDto> getTaskById(@Parameter(description = "ID задачи",
            example = "1") @PathVariable Long id) {
        Optional<TaskDto> taskDto = taskService.getTaskById(id);
        if (taskDto.isPresent()) {
            return ResponseEntity.ok(taskDto.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @Operation(summary = "Изменить задачу по ID")
    @PutMapping(value = "/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
        Optional<TaskDto> updatedTask = taskService.updateTask(id, taskDto);

        if (updatedTask.isPresent()) {
            return ResponseEntity.ok(updatedTask.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Удалить задачу по ID")
    @DeleteMapping(value = "/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @GetMapping(value = "/parse")
    public void parseSource() {
        List<TaskDto> taskDtos = taskService.parseJson();
        taskDtos.forEach(dto -> kafkaTaskProducer.sendTo(topic, dto));
    }

}
