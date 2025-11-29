package com.prevenfire.control;

import com.prevenfire.control.model.DeviceConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeviceConfigTest {

    @Test
    @DisplayName("Should apply default values on consistency check")
    void shouldApplyDefaultValues() {
        DeviceConfig config = new DeviceConfig("ESP32");

        // Simulates JPA lifecycle event
        config.ensureConsistency();

        assertEquals(50.0, config.getTemperatureLimit());
        assertEquals(false, config.getHighToleranceEnabled());
        assertEquals(30000, config.getReadingIntervalMs());

        // Effective limit should equal base limit
        assertEquals(50.0, config.getEffectiveTemperatureLimit());
    }

}
