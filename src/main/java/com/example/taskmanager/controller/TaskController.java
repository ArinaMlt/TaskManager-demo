package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/tasks")
@Tag(name = "Task controller", description = "API для управления задачами")
public class TaskController {
    TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(summary = "Создать новую задачу")
    @PostMapping
    public ResponseEntity<Task> postTask(Task task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    @Operation(summary = "Получить все задачи")
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @Operation(summary = "Получить задачу по ID", description = "Возвращает задачу, если она существует")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Задача успешно найдена"),
            @ApiResponse(responseCode = "404", description = "Задача не найдена")
    })
    @GetMapping(value = "/{id}")
    public ResponseEntity<Task> getTaskById(@Parameter(description = "ID задачи",
            example = "1") @PathVariable Long id) {
        Task task = taskService.getTaskById(id).orElse(null);
        return task != null ?
                ResponseEntity.ok(task) : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @Operation(summary = "Изменить задачу по ID")
    @PutMapping(value = "/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task task) {
        Optional<Task> updatedTask = taskService.updateTask(id, task);

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

}
