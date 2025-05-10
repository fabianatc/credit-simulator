package com.creditsimulator.infra.persistence.entity;

import com.creditsimulator.shared.enums.SimulationJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulation_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationJobEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private SimulationJobStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime finishedAt;

    private int totalSimulations;
    private int successCount;
    private int errorCount;
    
    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterEmail;
}

