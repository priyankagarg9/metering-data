package com.meter.energy.consumption.service;

import java.time.Month;
import java.util.Map;

import com.meter.energy.consumption.dto.MeterMonthlyReadingDto;

/**
 * Interface representing service methods for metering data
 */
public interface MeteringDataService {

	/**
	 * Process the monthly fraction data for a given profile
	 * 
	 * @param profile
	 * @param monthlyFractions
	 */
	public void processFractionData(String profile, Map<Month, Double> monthlyFractions);

	/**
	 * Process the monthly meter readings for a given meter
	 * 
	 * @param meterId
	 * @param meterMonthlyReadingDto
	 */
	public void processMeterReadings(String meterId, MeterMonthlyReadingDto meterMonthlyReadingDto);

	/**
	 * Gets the consumption of a given meter for the given month
	 * @param meterId
	 * @param month
	 * @return consumption
	 */
	public Long getConsumption(String meterId, Month month);

}
