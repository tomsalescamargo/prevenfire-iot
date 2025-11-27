package com.prevenfire.logging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SensorReadingRequestDTO(

    @NotBlank(message = "Device ID is mandatory")
    String deviceId,

    @NotNull(message = "Temperature is mandatory")
    Double temperature,

    @NotNull(message = "Limit is mandatory")
    Double temperatureLimit
) {}
