package com.example.taskmanager.service;

import com.example.taskmanager.aspect.LogExecution;
import com.example.taskmanager.aspect.LogTracking;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 1. POST /tasks — создание новой задачи.
 * <p>
 * 2. GET /tasks/{id} — получение задачи по ID.
 * <p>
 * 3. PUT /tasks/{id} — обновление задачи.
 * <p>
 * 4. DELETE /tasks/{id} — удаление задачи.
 * <p>
 * 5. GET /tasks — получение списка всех задач.
 */
@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @LogExecution
    public Task createTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be null or empty");
        }
        return taskRepository.save(task);
    }
    @LogExecution
    public Optional<Task> getTaskById(Long id) {
        return taskRepository.findById(id);
    }

    @LogTracking
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @LogExecution
    public Optional<Task> updateTask(Long id, Task task) {
        return taskRepository.update(id, task);
    }

    @LogTracking
    public boolean deleteTask(Long id) {
        return taskRepository.deleteById(id);
    }
}
