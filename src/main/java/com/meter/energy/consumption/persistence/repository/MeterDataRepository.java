package com.meter.energy.consumption.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.meter.energy.consumption.persistence.model.MeterData;
import com.meter.energy.consumption.persistence.model.MeterProfile;


public interface MeterDataRepository extends JpaRepository<MeterData, Long> {

	void deleteAllByMeterProfile(MeterProfile meterProfile);

	MeterData findByMeterProfileAndMonth(MeterProfile meterProfile, String month);

}
