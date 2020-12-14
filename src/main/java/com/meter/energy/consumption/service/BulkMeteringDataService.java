package com.meter.energy.consumption.service;

import java.util.List;
import java.util.Map;

import com.meter.energy.consumption.controller.dto.MeterReadingDto;
import com.meter.energy.consumption.controller.dto.ProfileFractionDto;

/**
 * Interface representing service methods for metering data in bulk
 */
public interface BulkMeteringDataService {

	/**
	 * Process the list of monthly fractions of a profile
	 * 
	 * @param profileFractions the list of monthly fractions of a profile
	 */
	public void processProfileFractions(List<ProfileFractionDto> profileFractions);

	/**
	 * Process the list of monthly meter readings of a profile
	 * 
	 * @param meterReadings the list of monthly meter readings of a profile
	 * @return map containing all the profiles which were rejected (never null, may
	 *         be empty)
	 */
	public Map<String, String> processMeterReadings(List<MeterReadingDto> meterReadings);

}
