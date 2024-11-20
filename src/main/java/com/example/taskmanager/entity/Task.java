package com.example.taskmanager.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Task {

    Long id;
    String title;
    String description;
    Long userId;
}
