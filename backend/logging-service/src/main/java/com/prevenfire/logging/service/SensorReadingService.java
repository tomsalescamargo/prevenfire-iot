package com.prevenfire.logging.service;

import com.prevenfire.logging.model.SensorReading;
import com.prevenfire.logging.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensorReadingService {

    private final SensorReadingRepository repository;

    // Constructor Dependency Injection
    public SensorReadingService(SensorReadingRepository repository) {
        this.repository = repository;
    }

    public SensorReading registerReading(SensorReading reading) {
        return repository.save(reading);
    }

    public List<SensorReading> getReadingsByDevice(String deviceId) {
        return repository.findByDeviceIdOrderByTimestampDesc(deviceId);
    }

    public List<SensorReading> getCriticalReadingsByDevice(String deviceId) {
        return repository.findByDeviceIdAndIsOverLimitIsTrueOrderByTimestampDesc(deviceId);
    }
}
