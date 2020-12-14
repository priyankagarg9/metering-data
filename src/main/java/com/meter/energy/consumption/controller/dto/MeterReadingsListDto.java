package com.meter.energy.consumption.controller.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;

/**
 * Represents the meter readings for multiple meters
 */
@ApiModel(value = "MeterReadingsListDto", description = "Represents meter readings for multiple meters")
public class MeterReadingsListDto {

	@NotNull
	private List<MeterReadingDto> meterReadings;

	public List<MeterReadingDto> getMeterReadings() {
		return meterReadings;
	}

	public void setMeterReadings(List<MeterReadingDto> meterReadings) {
		this.meterReadings = meterReadings;
	}

	@Override
	public String toString() {
		return "MeterReadingsListDto [meterReadings=" + meterReadings + "]";
	}

}
