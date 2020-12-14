package com.meter.energy.consumption.controller.dto;

import java.time.Month;
import java.util.Map;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents the monthly readings of a meter
 */
@ApiModel(value = "MeterMonthlyReading", description = "Represents the monthly meter readings")
public class MeterMonthlyReadingDto {
	
	@NotNull
	@ApiModelProperty(value = "Profile", required = true)
    private String profile;
	
	@NotNull
	@ApiModelProperty(value = "Monthly Readings", required = true)
    private Map<Month, Long> monthlyReadings;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public Map<Month, Long> getMonthlyReadings() {
		return monthlyReadings;
	}

	public void setMonthlyReadings(Map<Month, Long> monthlyReadings) {
		this.monthlyReadings = monthlyReadings;
	}

	@Override
	public String toString() {
		return "MeterMonthlyReadingDto [profile=" + profile + ", monthlyReadings=" + monthlyReadings + "]";
	}
	
}
