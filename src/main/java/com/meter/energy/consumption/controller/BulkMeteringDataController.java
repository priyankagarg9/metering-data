package com.meter.energy.consumption.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.meter.energy.consumption.dto.MeterReadingsListDto;
import com.meter.energy.consumption.dto.ProfileFractionsListDto;
import com.meter.energy.consumption.service.BulkMeteringDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = { "v1/bulk-energy-consumption" })
@Api(tags = { "bulk-energy-consumption" })
public class BulkMeteringDataController {

	@Autowired
	private BulkMeteringDataService bulkMeteringDataService;

	/**
	 * This method would process the list of monthly fractions for a profile. If the
	 * total of all the fractions for a profile is not 1, error will be thrown.
	 * 
	 * @param the list of monthly fractions of a profile
	 */
	@RequestMapping(path = "/profile-fractions", method = RequestMethod.POST)
	@ResponseStatus(code = HttpStatus.OK)
	@ApiOperation(value = "Processes the profile fraction data", consumes = "application/json")
	public void processProfileFractions(
			@ApiParam(value = "Profile Fraction Data") @Valid @RequestBody ProfileFractionsListDto profileFractionsList) {
		bulkMeteringDataService.processProfileFractions(profileFractionsList.getProfileFractions());
	}

	/**
	 * This method would process the list of monthly meter readings for a profile.
	 * The meter readings of a given profile will be rejected in case of validation
	 * errors.
	 * 
	 * @param the list of monthly meter readings of a profile
	 */
	@RequestMapping(path = "/meter-readings", method = RequestMethod.POST)
	@ResponseStatus(code = HttpStatus.OK)
	@ApiOperation(value = "Processes the meter readings of a profile", consumes = "application/json")
	public Map<String, String> processMeterReadings(
			@ApiParam(value = "Profile Meter Readings") @Valid @RequestBody MeterReadingsListDto meterReadingsList) {
		return bulkMeteringDataService.processMeterReadings(meterReadingsList.getMeterReadings());
	}

}
