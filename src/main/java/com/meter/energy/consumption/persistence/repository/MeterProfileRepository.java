package com.meter.energy.consumption.persistence.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.meter.energy.consumption.persistence.model.MeterProfile;


public interface MeterProfileRepository extends JpaRepository<MeterProfile, Long> {
	
	@Cacheable(value = "meterProfile", key = "#root.methodName + #profile")
	MeterProfile findByProfile(String profile);

	MeterProfile findByMeterId(String meterId);

}
