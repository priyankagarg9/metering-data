package com.meter.energy.consumption.dto;

import java.time.Month;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents a monthly fraction of a profile
 */
@ApiModel(value = "ProfileFraction", description = "Represents a monthly fraction of a profile")
public class ProfileFractionDto {
	
	@NotNull
	@ApiModelProperty(value = "Month", required = true)
    private Month month;
	
	@NotNull
	@ApiModelProperty(value = "Profile", required = true)
    private String profile;
	
	@DecimalMin("0.00")
    @DecimalMax("0.99")
	@ApiModelProperty(value = "Fraction", required = true)
    private Double fraction;
	
	public ProfileFractionDto() {
		
	}

	public ProfileFractionDto(@NotNull Month month, @NotNull String profile,
			@DecimalMin("0.00") @DecimalMax("0.99") Double fraction) {
		super();
		this.month = month;
		this.profile = profile;
		this.fraction = fraction;
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

    public Double getFraction() {
		return fraction;
	}

	public void setFraction(Double fraction) {
		this.fraction = fraction;
	}

	public void trim() {
        profile = StringUtils.trimWhitespace(profile);
    }

    @Override
    public String toString() {
        return "ProfileFraction [profile="
               + profile
               + ", month="
               + month
               + ", fraction="
               + fraction
               + "]";
    }

}
