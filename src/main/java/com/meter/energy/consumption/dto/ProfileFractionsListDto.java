package com.meter.energy.consumption.dto;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;

/**
 * Represents the fractions for multiple profiles
 */
@ApiModel(value = "ProfileFractionsListDto", description = "Represents fractions for multiple profiles")
public class ProfileFractionsListDto {

	@NotNull
	private List<ProfileFractionDto> profileFractions;

	public List<ProfileFractionDto> getProfileFractions() {
		return profileFractions;
	}

	public void setProfileFractions(List<ProfileFractionDto> profileFractions) {
		this.profileFractions = profileFractions;
	}

	@Override
	public String toString() {
		return "ProfileFractionsListDto [profileFractions=" + profileFractions + "]";
	}

}
