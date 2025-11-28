package com.prevenfire.control.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@Table(name = "tb_device_config")
public class DeviceConfig {

    @Id
    private String deviceId;

    @Column(nullable = false)
    private Double temperatureLimit;

    @Column(nullable = false)
    private Boolean highToleranceEnabled;

    private String highToleranceReason;

    // Final temperature limit to be applied by the device,
    // adjusted according to High Tolerance settings.
    @Column(nullable = false)
    private Double effectiveTemperatureLimit;

    @Column(nullable = false)
    private Integer readingIntervalMs;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Creates a transient configuration instance with project default values.
    // Used when the device has no persisted configuration.
    public DeviceConfig(String deviceId) {
        this.deviceId = deviceId;
        this.temperatureLimit = 50.0;
        this.highToleranceEnabled = false;
        this.effectiveTemperatureLimit = 50.0;
        this.readingIntervalMs = 30000;
    }

    @PrePersist
    @PreUpdate
    public void prePersist() {
        if (this.temperatureLimit == null) {
            this.temperatureLimit = 50.0;
            this.effectiveTemperatureLimit = 50.0;
        }

        if (this.highToleranceEnabled == null) {
            this.highToleranceEnabled = false;
        }

        if (this.readingIntervalMs == null) {
            this.readingIntervalMs = 30000;
        }
    }
}
