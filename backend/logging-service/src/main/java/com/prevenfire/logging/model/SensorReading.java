package com.prevenfire.logging.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Device ID is mandatory")
    @Column(nullable = false)
    private String deviceId;

    @NotNull(message = "Temperature reading cannot be null")
    @Column(nullable = false)
    private Double temperature;

    @NotNull(message = "Temperature limit is mandatory")
    @Column(nullable = false)
    private Double temperatureLimit;

    @Column(nullable = false)
    private Boolean isOverLimit;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }

        if (this.isOverLimit == null) {
            if (this.temperature != null && this.temperatureLimit != null) {
                this.isOverLimit = this.temperature > this.temperatureLimit;
            } else {
                throw new IllegalStateException(
                        "Cannot calculate isOverLimit: temperature and temperatureLimit are required"
                );
            }
        }
    }
}
