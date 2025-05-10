package com.creditsimulator.infra.persistence.entity;

import com.creditsimulator.shared.enums.SimulationResultStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "simulation_result")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationResultEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    private SimulationJobEntity job;

    @Column(columnDefinition = "jsonb")
    private String input;

    @Column(columnDefinition = "jsonb")
    private String output;

    @Enumerated(EnumType.STRING)
    private SimulationResultStatus status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
