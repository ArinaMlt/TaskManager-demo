package com.example.taskmanager.service;

import com.example.taskmanager.aspect.LogExecution;
import com.example.taskmanager.aspect.LogTracking;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
        Optional<Task> existingTask = taskRepository.findById(id);
        if (existingTask.isPresent()) {
            Task updatedTask = existingTask.get();
            updatedTask.setTitle(task.getTitle());
            updatedTask.setDescription(task.getDescription());
            updatedTask.setUserId(task.getUserId());

            taskRepository.save(updatedTask);
            return Optional.of(updatedTask);

        }
        return Optional.empty();
    }

    @LogTracking
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}
