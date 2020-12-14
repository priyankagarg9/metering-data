package com.meter.energy.consumption.controller.dto;

import java.time.Month;

import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a meter reading
 */
@ApiModel(value = "MeterReading", description = "Represents a meter reading")
public class MeterReadingDto {
	
	@NotNull
	@ApiModelProperty(value = "Meter ID", required = true)
    private String meterId;
	
	@NotNull
	@ApiModelProperty(value = "Profile", required = true)
    private String profile;
	
	@NotNull
	@ApiModelProperty(value = "Month", required = true)
    private Month month;
	
	@ApiModelProperty(value = "Reading", required = true)
    private Integer reading;

	public String getMeterId() {
		return meterId;
	}

	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public Month getMonth() {
		return month;
	}

	public void setMonth(Month month) {
		this.month = month;
	}

	public Integer getReading() {
		return reading;
	}

	public void setReading(Integer reading) {
		this.reading = reading;
	}
	
	public MeterReadingDto() {
		
	}
	
    public MeterReadingDto(@NotNull String meterId, @NotNull String profile, @NotNull Month month, Integer reading) {
		super();
		this.meterId = meterId;
		this.profile = profile;
		this.month = month;
		this.reading = reading;
	}

	public void trim() {
        profile = StringUtils.trimWhitespace(profile);
    }

    @Override
    public String toString() {
        return "MeterReading [meterId=" + meterId
               + ", profile="
               + profile
               + ", month="
               + month
               + ", reading="
               + reading
               + "]";
    }

}
