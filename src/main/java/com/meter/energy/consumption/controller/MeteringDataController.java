package com.meter.energy.consumption.controller;

import java.time.Month;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.meter.energy.consumption.controller.dto.MeterMonthlyReadingDto;
import com.meter.energy.consumption.controller.dto.MonthlyProfileFractionsDto;
import com.meter.energy.consumption.service.MeteringDataService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = { "/v1/energy-consumption" })
@Api(tags = { "energy-consumption" })
public class MeteringDataController {

	@Autowired
	private MeteringDataService meteringDataService;

	/**
	 * This method would process the list of monthly fractions for a profile. If the
	 * total of all the fractions for a profile is not 1, error will be thrown.
	 * 
	 * @param the monthly fractions dto
	 * @param the name of the profile
	 */
	@PostMapping("/{profile}/fractions")
	@ResponseStatus(code = HttpStatus.OK)
	@ApiOperation(value = "Processes the profile fraction data", consumes = "application/json")
	public void processProfileFractions(
			@ApiParam(value = "Profile Fraction Data", required = true) @Valid @RequestBody MonthlyProfileFractionsDto monthlyFractions,
			@ApiParam(value = "Profile", required = true) @PathVariable(value = "profile") String profile) {
		meteringDataService.processFractionData(profile, monthlyFractions.getMonthlyFractions());
	}

	/**
	 * This method would process the list of monthly readings for a meter. The meter
	 * readings of a given profile will be rejected in case of validation errors.
	 * 
	 * @param the meter monthly reading object
	 * @param the meter id
	 */
	@PostMapping("/{meterId}/meter-readings")
	@ResponseStatus(code = HttpStatus.OK)
	@ApiOperation(value = "Processes the meter readings data", consumes = "application/json")
	public void processMeterReadings(
			@ApiParam(value = "Meter Reading Data", required = true) @Valid @RequestBody MeterMonthlyReadingDto meterMonthlyReadingDto,
			@ApiParam(value = "Meter Id", required = true) @PathVariable(value = "meterId") String meterId) {
		meteringDataService.processMeterReadings(meterId, meterMonthlyReadingDto);
	}

	/**
	 * This method would give the consumption for the given meter id and month.
	 * The method would return error code in case he data for the provided inputs is not found.
	 * @param meterId
	 * @param month
	 * @return the consumption for given meter id and month
	 */
	@GetMapping("/{meterId}/{month}/consumption")
	@ResponseStatus(code = HttpStatus.OK)
	@ApiOperation(value = "Gets the consumption by meter id and month", response = Long.class, produces = "application/json")
	public Long getConsumption(
			@ApiParam(value = "Meter Id", required = true) @PathVariable(value = "meterId") String meterId,
			@ApiParam(value = "Month", required = true) @PathVariable(value = "month") Month month) {
		return meteringDataService.getConsumption(meterId, month);
	}
}
