package com.prevenfire.control.repository;

import com.prevenfire.control.model.DeviceConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** JPA repository for DeviceConfig entities identified by deviceId. */
@Repository
public interface DeviceConfigRepository extends JpaRepository<DeviceConfig, String> {
}
