package com.meter.energy.consumption.service;

/**
 * Interface representing service methods for metering data in bulk
 */
public interface ProcessCsvDataService {

	/**
	 * Process the list of monthly fractions of a profile
	 * 
	 * @param profileFractions the list of monthly fractions of a profile
	 */
	public void processCsvFiles();

}
