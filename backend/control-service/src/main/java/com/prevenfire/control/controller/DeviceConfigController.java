package com.prevenfire.control.controller;

import com.prevenfire.control.dto.DeviceConfigRequestDTO;
import com.prevenfire.control.model.DeviceConfig;
import com.prevenfire.control.service.DeviceConfigService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
public class DeviceConfigController {

    private final DeviceConfigService service;

    public DeviceConfigController(DeviceConfigService service) {
        this.service = service;
    }

    /**
     * Creates a new configuration. If the device already has one,
     * the existing configuration is updated.
     * Missing fields fall back to default values.
     */
    @PostMapping
    public ResponseEntity<DeviceConfig> createConfiguration(
            @Valid @RequestBody DeviceConfigRequestDTO configDTO
    ) {
        DeviceConfig savedConfig = service.saveConfig(configDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedConfig);
    }

    /**
     * Updates a device configuration.
     */
    @PutMapping
    public ResponseEntity<DeviceConfig> updateConfiguration(
            @Valid @RequestBody DeviceConfigRequestDTO configDto
    ) {
        DeviceConfig updatedConfig = service.saveConfig(configDto);

        return ResponseEntity.ok(updatedConfig);
    }

    /**
     * Endpoint: PUT /api/config/{deviceId}/reset
     * Resets configuration with defaults
     */
    @PutMapping("/{deviceId}/reset")
    public ResponseEntity<?> resetConfiguration(@PathVariable String deviceId) {
        try {
            DeviceConfig defaultConfig = service.resetConfig(deviceId);
            return ResponseEntity.ok(defaultConfig);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Retrieves configuration by deviceId.
     * When defaultIfAbsent=true, returns a transient default config instead of 404.
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<?> getConfigByDeviceId(
            @PathVariable String deviceId,
            @RequestParam(value = "defaultIfAbsent", required = false) Boolean defaultIfAbsent
    ) {
        DeviceConfig config;
        if (Boolean.TRUE.equals(defaultIfAbsent)) {
            config = service.getConfigByDeviceOrDefault(deviceId);
        } else {
            config = service.getConfigByDevice(deviceId);
            if (config == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found for ID: " + deviceId);
            }
        }

        return ResponseEntity.ok(config);
    }

    /**
     * Deletes a device configuration. Returns the deleted configuration,
     * or 404 if none exists.
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> deleteConfigByDeviceId(@PathVariable String deviceId) {
        DeviceConfig deletedConfig = service.deleteConfigByDevice(deviceId);
        if (deletedConfig == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found for ID: " + deviceId);
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
