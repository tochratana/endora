package com.example.platform.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id
    private UUID id;

    private String name;

    @Column(name = "auth_enabled")
    private boolean authEnabled;

    @Column(name = "created_at")
    private Instant createdAt;
}
