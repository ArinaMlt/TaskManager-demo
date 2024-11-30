package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDto;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/tasks")
@RequiredArgsConstructor
@Tag(name = "Task controller", description = "API для управления задачами")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Создать новую задачу")
    @PostMapping
    public void postTask(TaskDto taskDto) {
        taskService.createTask(taskDto);
    }

    @Operation(summary = "Получить все задачи")
    @GetMapping
    public List<TaskDto> getAllTasks() {
        return taskService.getAllTasks();
    }

    @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу, если она существует")
    @GetMapping(value = "/{id}")
    public TaskDto getTaskById(@Parameter(description = "ID задачи",
            example = "1") @PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @Operation(summary = "Изменить задачу по ID")
    @PutMapping(value = "/{id}")
    public TaskDto updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
        return taskService.updateTask(id, taskDto);
    }

    @Operation(summary = "Удалить задачу по ID")
    @DeleteMapping(value = "/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }
}
