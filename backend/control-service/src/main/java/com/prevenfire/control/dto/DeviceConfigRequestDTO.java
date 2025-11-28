package com.prevenfire.control.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DeviceConfigRequestDTO(

    @NotBlank(message = "Device ID is mandatory")
    String deviceId,

    @NotNull(message = "Temperature limit is mandatory")
    Double temperatureLimit,

    Boolean highToleranceEnabled,

    String highToleranceReason,

    Integer readingIntervalSeconds
){}
