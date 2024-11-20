package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepository {
    private final List<Task> tasks = new ArrayList<>();
    private Long currentId = 1L;

    public Task save(Task task) {
        if (task.getId() == null) {
            task.setId(currentId++);
        }
        tasks.add(task);
        return task;
    }

    public Optional<Task> findById(Long id) {
        return tasks.stream().filter(task -> task.getId().equals(id)).findFirst();
    }

    public List<Task> findAll() {
        return new ArrayList<>(tasks);
    }

    public boolean deleteById(Long id) {
        return tasks.removeIf(task -> task.getId().equals(id));
    }

    public Optional<Task> update(Long id, Task updatedTask) {
        return findById(id).map(existingTask -> {
            existingTask.setTitle(updatedTask.getTitle());
            existingTask.setDescription(updatedTask.getDescription());
            existingTask.setUserId(updatedTask.getUserId());
            return existingTask;
        });
    }
}
