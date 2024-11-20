package com.example.taskmanager;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.service.TaskService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@ComponentScan
@EnableAspectJAutoProxy
public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        TaskService taskService = context.getBean(TaskService.class);
        Task task1 = taskService.createTask(new Task(null, "Task 1", "Description 1", 1L));
        Task task2 = taskService.createTask(new Task(null, "Task 2", "Invalid task", 2L));

        System.out.println("All Tasks: " + taskService.getAllTasks());

        System.out.println("Task with ID 1: " + taskService.getTaskById(1L).orElse(null));

        taskService.updateTask(1L, new Task(null, "Updated Task 1", "Updated Description 1", 1L));
        System.out.println("Updated Task with ID 1: " + taskService.getTaskById(1L).orElse(null));

        taskService.deleteTask(2L);
        System.out.println("All Tasks after deletion: " + taskService.getAllTasks());

        try {
            taskService.createTask(new Task(null, "", "Invalid task", 2L));

        } catch (Exception e){
            System.out.println("Exception caught: " + e.getMessage());
        }

    }
}
