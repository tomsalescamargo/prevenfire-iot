package com.prevenfire.logging.repository;

import com.prevenfire.logging.model.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing SensorReading persistence.
 */
@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {

    /**
     * Retrieves all readings for a specific device, ordered by most recent first.
     *
     * @param deviceId The unique identifier of the source device.
     * @return List of sensor readings sorted by timestamp descending.
     */
    List<SensorReading> findByDeviceIdOrderByTimestampDesc(String deviceId);

    /**
     * Retrieves critical readings where the temperature exceeded the configured limit.
     * Useful for alert history and auditing.
     *
     * @param deviceId The unique identifier of the source device.
     * @return List of readings flagged as over limit.
     */
    List<SensorReading> findByDeviceIdAndIsOverLimitIsTrueOrderByTimestampDesc(String deviceId);
}