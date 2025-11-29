package com.prevenfire.logging;

import com.prevenfire.logging.dto.SensorReadingRequestDTO;
import com.prevenfire.logging.model.SensorReading;
import com.prevenfire.logging.repository.SensorReadingRepository;
import com.prevenfire.logging.service.SensorReadingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SensorReadingServiceTest {

    @Mock
    private SensorReadingRepository repository;

    @InjectMocks
    private SensorReadingService service;

    @Test
    @DisplayName("Should convert DTO to Entity and call repository save")
    void shouldConvertDtoAndCallSave() {
        SensorReadingRequestDTO dto = new SensorReadingRequestDTO("ESP32-TOM", 25.5, 50.0);

        when(repository.save(any(SensorReading.class))).thenAnswer(i -> i.getArgument(0));

        SensorReading result = service.registerReading(dto);

        assertNotNull(result);
        assertEquals("ESP32-TOM", result.getDeviceId());
        assertEquals(25.5, result.getTemperature());
        assertEquals(50.0, result.getTemperatureLimit());

        verify(repository).save(any(SensorReading.class));
    }
}
