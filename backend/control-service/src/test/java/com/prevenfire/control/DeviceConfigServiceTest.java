package com.prevenfire.control;

import com.prevenfire.control.dto.DeviceConfigRequestDTO;
import com.prevenfire.control.model.DeviceConfig;
import com.prevenfire.control.repository.DeviceConfigRepository;
import com.prevenfire.control.service.DeviceConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceConfigServiceTest {

	@Mock
    private DeviceConfigRepository repository;

    @InjectMocks
    private DeviceConfigService service;

    @Test
    @DisplayName("Should save new configuration when device does not exist")
    void shouldSaveNewConfiguration() {
        String deviceId = "ESP32-TEST-01";
        // Create DTO with High Tolerance Enabled to verify calculation logic
        DeviceConfigRequestDTO dto = new DeviceConfigRequestDTO(
                deviceId, 55.0, true, null,  30
        );

        when(repository.findById(deviceId)).thenReturn(Optional.empty());

        // Mock save to return the same object passed to it
        when(repository.save(any(DeviceConfig.class))).thenAnswer(i -> i.getArguments()[0]);

        DeviceConfig result = service.saveConfig(dto);

        assertNotNull(result);
        assertEquals(55.0, result.getTemperatureLimit());
        assertTrue(result.getEffectiveTemperatureLimit() > result.getTemperatureLimit());
        assertEquals(deviceId, result.getDeviceId());

        verify(repository, times(1)).save(any(DeviceConfig.class));
    }

    @Test
    @DisplayName("Should reset configurable fields to null")
    void shouldResetConfiguration() {
        String deviceId = "ESP32-OLD";
        DeviceConfig existingConfig = new DeviceConfig(deviceId);
        existingConfig.setTemperatureLimit(80.0);
        existingConfig.setHighToleranceEnabled(true);

        when(repository.findById(deviceId)).thenReturn(Optional.of(existingConfig));
        when(repository.save(any(DeviceConfig.class))).thenAnswer(i -> i.getArguments()[0]);

        DeviceConfig result = service.resetConfig(deviceId);

        // ASSERT: Service must have set the fields to null to trigger the Entity’s @PreUpdate default logic later.
        assertNull(result.getTemperatureLimit());
        assertNull(result.getHighToleranceEnabled());
        verify(repository).save(existingConfig);
    }

    @Test
    @DisplayName("Should keep previous configuration values when DTO has null fields (Partial Update)")
    void shouldKeepOldValuesWhenDtoHasNulls() {
        String deviceId = "ESP32-TEST-PARTIAL";
        DeviceConfig configInDB = new DeviceConfig(deviceId);
        configInDB.setHighToleranceEnabled(true);
        configInDB.setReadingIntervalMs(60000);

        // DTO with only temperature change, others are null
        DeviceConfigRequestDTO dto = new DeviceConfigRequestDTO(
                deviceId, 90.0, null, null, null
        );

        when(repository.findById(deviceId)).thenReturn(Optional.of(configInDB));
        when(repository.save(any(DeviceConfig.class))).thenAnswer(i -> i.getArguments()[0]);

        DeviceConfig result = service.saveConfig(dto);

        assertEquals(90.0, result.getTemperatureLimit()); // Updated
        assertTrue(result.getHighToleranceEnabled()); // Preserved from DB
        assertEquals(60000, result.getReadingIntervalMs()); // Preserved from DB
    }

    @Test
    @DisplayName("Should return transient default object if device not found in DB")
    void shouldReturnDefaultTransientIfNotFound() {
        String deviceId = "ESP32-TOM-SALES";
        when(repository.findById(deviceId)).thenReturn(Optional.empty());

        DeviceConfig result = service.getConfigByDeviceOrDefault(deviceId);

        assertNotNull(result);
        assertEquals("ESP32-TOM-SALES", result.getDeviceId());
        assertNotNull(result.getEffectiveTemperatureLimit());
    }
}
