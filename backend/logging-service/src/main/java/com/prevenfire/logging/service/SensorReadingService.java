package com.prevenfire.logging.service;

import com.prevenfire.logging.dto.SensorReadingRequestDTO;
import com.prevenfire.logging.model.SensorReading;
import com.prevenfire.logging.repository.SensorReadingRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorReadingService {

    private final SensorReadingRepository repository;

    public SensorReadingService(SensorReadingRepository repository) {
        this.repository = repository;
    }

    /**
     * Persists a new sensor reading.
     * Converts DTO to Entity and relies on Entity's @PrePersist for logic (isOverLimit).
     */
    @Transactional
    public SensorReading registerReading(SensorReadingRequestDTO readingRequest) {
        SensorReading sensorReadingModel = new SensorReading();
        sensorReadingModel.setDeviceId(readingRequest.deviceId());
        sensorReadingModel.setTemperature(readingRequest.temperature());

        // IMPORTANT: The ESP32 sends the EFFECTIVE limit used at that moment.
        // We trust the device's reporting context.
        sensorReadingModel.setTemperatureLimit(readingRequest.temperatureLimit());

        return repository.save(sensorReadingModel);
    }

    public List<SensorReading> getReadingsByDevice(String deviceId) {
        return repository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<SensorReading> getCriticalReadingsByDevice(String deviceId) {
        return repository.findByDeviceIdAndIsOverLimitIsTrueOrderByTimestampDesc(deviceId);
    }
}
