package com.meter.energy.consumption.controller.dto;

import java.time.Month;
import java.util.Map;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;

/**
 * Represents the monthly fractions for a profile
 */
@ApiModel(value = "MonthlyProfileFractionsDto", description = "Represents the monthly profile fractions")
public class MonthlyProfileFractionsDto {

	@NotNull
	private Map<Month, Double> monthlyFractions;

	public Map<Month, Double> getMonthlyFractions() {
		return monthlyFractions;
	}

	public void setMonthlyFractions(Map<Month, Double> monthlyFractions) {
		this.monthlyFractions = monthlyFractions;
	}

	@Override
	public String toString() {
		return "MonthlyProfileFractionsDto [monthlyFractions=" + monthlyFractions + "]";
	}

}
