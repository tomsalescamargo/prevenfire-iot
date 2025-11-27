package com.prevenfire.logging.controller;

import com.prevenfire.logging.dto.SensorReadingRequestDTO;
import com.prevenfire.logging.model.SensorReading;
import com.prevenfire.logging.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorReadingService service;

    public SensorReadingController(SensorReadingService service) {
        this.service = service;
    }

    /**
     * Endpoint: POST /api/readings
     * Register a sensor reading.
     */
    @PostMapping
    public ResponseEntity<String> registerSensorReading(
            @Valid @RequestBody SensorReadingRequestDTO readingDTO
    ) {
        // Convert DTO -> Model
        SensorReading sensorReadingModel = new SensorReading();
        sensorReadingModel.setDeviceId(readingDTO.deviceId());
        sensorReadingModel.setTemperature(readingDTO.temperature());
        sensorReadingModel.setTemperatureLimit(readingDTO.temperatureLimit());

        // Delegates business logic to service layer
        service.registerReading(sensorReadingModel);

        // Return HTTP 201 (CREATED) - Spring already handles and return errors
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Sensor reading registered successfully.");
    }

    /**
     * Endpoint: GET /api/readings/{deviceId}
     * Returns the full history for a device.
     */
    @GetMapping("/{deviceId}")
    public ResponseEntity<List<SensorReading>> getSensorReadingsByDeviceId(@PathVariable String deviceId) {
        List<SensorReading> readings = service.getReadingsByDevice(deviceId);

        return ResponseEntity.ok(readings);
    }

    /**
     * Endpoint: GET /api/readings/{deviceId}/criticals
     * Returns only the readings where the limit was exceeded.
     */
    @GetMapping("/{deviceId}/criticals")
    public ResponseEntity<List<SensorReading>> getCriticalReadingsByDeviceId(@PathVariable String deviceId) {
        List<SensorReading> readings = service.getCriticalReadingsByDevice(deviceId);

        return ResponseEntity.ok(readings);
    }
}
