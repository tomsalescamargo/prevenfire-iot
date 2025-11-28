package com.prevenfire.control.service;

import com.prevenfire.control.dto.DeviceConfigRequestDTO;
import com.prevenfire.control.model.DeviceConfig;
import com.prevenfire.control.repository.DeviceConfigRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeviceConfigService {

    private final DeviceConfigRepository repository;

    public DeviceConfigService(DeviceConfigRepository repository){
        this.repository = repository;
    }

    /**
     * Creates or Updates the device configuration.
     * CREATION: If fields are null, uses project defaults.
     * UPDATE: If fields are null, use previous configurations.
     */
    @Transactional
    public DeviceConfig saveConfig(DeviceConfigRequestDTO configRequest) {
        // Load existing config if present, or instance a Model with default attributes
        DeviceConfig config = repository.findById(configRequest.deviceId())
                .orElse(new DeviceConfig(configRequest.deviceId()));

        // Temperature Limit is mandatory
        config.setTemperatureLimit(configRequest.temperatureLimit());

        // Set fields directly if provided.
        if (configRequest.highToleranceEnabled() != null) {
            config.setHighToleranceEnabled(configRequest.highToleranceEnabled());
            config.setHighToleranceReason(configRequest.highToleranceReason());
        }

        if (configRequest.readingIntervalSeconds() != null) {
            config.setReadingIntervalMs(configRequest.readingIntervalSeconds() * 1000);
        }

        // Calculate Effective Temperature Limit
        if (Boolean.TRUE.equals(config.getHighToleranceEnabled())) {
            config.setEffectiveTemperatureLimit(config.getTemperatureLimit() + 30);
        } else {
            config.setEffectiveTemperatureLimit(config.getTemperatureLimit());
        }

        return repository.save(config);
    }

    /**
     * Resets configuration to project defaults at saving
     * @param requestDTO
     * @return Configuration reset
     */
    @Transactional
    public DeviceConfig resetConfig(String deviceId) {
        DeviceConfig config = repository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Device not found for ID: " + deviceId
                ));

        // Set null to trigger @PreUpdate defaults
        // (Effective limit will be reset by @PreUpdate in Model too)
        config.setTemperatureLimit(null);
        config.setHighToleranceEnabled(null);
        config.setHighToleranceReason(null);
        config.setReadingIntervalMs(null);

        repository.save(config);
        return config;
    }

    public DeviceConfig getConfigByDevice(String deviceId) {
        return repository.findById(deviceId).orElse(null);
    }

    /**
     * Retrieves config. If not found, returns transient default object (not saved in DB).
     * Useful for embedded devices with no configs saved yet, avoiding errors.
     * @return device found or default DeviceConfig
     */
    public DeviceConfig getConfigByDeviceOrDefault(String deviceId) {
        return repository.findById(deviceId)
                .orElseGet(() -> new DeviceConfig(deviceId));
    }

    /**
     * Deletes a configuration by Device ID
     */
    @Transactional
    public DeviceConfig deleteConfigByDevice(String deviceId) {
        DeviceConfig config = repository.findById(deviceId).orElse(null);

        if (config != null) {
            repository.delete(config);
        }

        return config;
    }
}
