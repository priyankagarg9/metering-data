package com.meter.energy.consumption.service.impl;

import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meter.energy.consumption.controller.dto.MeterReadingDto;
import com.meter.energy.consumption.controller.dto.ProfileFractionDto;
import com.meter.energy.consumption.exception.ApplicationException;
import com.meter.energy.consumption.persistence.model.MeterData;
import com.meter.energy.consumption.persistence.model.MeterProfile;
import com.meter.energy.consumption.persistence.repository.MeterDataRepository;
import com.meter.energy.consumption.persistence.repository.MeterProfileRepository;
import com.meter.energy.consumption.service.BulkMeteringDataService;

/**
 * Implementation of {@link BulkMeteringDataService}.
 */
@Service
public class BulkMeteringDataServiceImpl implements BulkMeteringDataService {

	@Autowired
	private MeterProfileRepository meterProfileRepo;

	@Autowired
	private MeterDataRepository meterDataRepo;

	private static final double THRESHOLD = 0.01;
	private static final String FRACTION_TOTAL_INCORRECT = "Fraction total for atleast one profile is incorrect";
	private static final String PROFILE_DOES_NOT_EXIST = "The profile does not exist : ";
	private static final String PROFILE_HAS_INCONSISTENT_DATA = "The profile has inconsistent data";

	@Override
	@Transactional(rollbackFor = ApplicationException.class)
	public void processProfileFractions(List<ProfileFractionDto> profileFractions) {
		Map<String, Double> totalFractionPerProfile = new HashMap<>();
		for (ProfileFractionDto profileFractionDto : profileFractions) {
			MeterProfile meterProfile = meterProfileRepo.findByProfile(profileFractionDto.getProfile());

			if (null == meterProfile) {
				/* the profile does not exist in database, adding it */
				meterProfile = populateAndSaveMeterProfile(profileFractionDto.getProfile());
			}

			/* adding the new monthly fraction to the existing profile */
			totalFractionPerProfile.computeIfPresent(profileFractionDto.getProfile(),
					(key, value) -> value + profileFractionDto.getFraction());

			/* add the new profile to the map if absent */
			totalFractionPerProfile.putIfAbsent(profileFractionDto.getProfile(), profileFractionDto.getFraction());

			/* create model object and persist it */
			populateAndSaveMeterData(meterProfile, profileFractionDto.getMonth(), profileFractionDto.getFraction());
		}

		/* check the map for the fraction total, throw an exception if total is not 1 */
		if (totalFractionPerProfile.values().stream().anyMatch(value -> Math.abs(value - 1.0) > THRESHOLD)) {
			throw new ApplicationException(FRACTION_TOTAL_INCORRECT);
		}
	}

	@Override
	public Map<String, String> processMeterReadings(@Valid List<MeterReadingDto> meterReadings) {

		/* list of profiles having validation errors */
		Map<String, String> rejectedProfiles = new HashMap<>();

		/* Compare by meter id and month */
		Comparator<MeterReadingDto> compareByMeterIdAndMonth = Comparator.comparing(MeterReadingDto::getMeterId)
				.thenComparing(MeterReadingDto::getMonth);

		Map<String, List<MeterReadingDto>> sortedAndGroupedReadings = meterReadings.stream()
				.sorted(compareByMeterIdAndMonth).collect(Collectors.groupingBy(MeterReadingDto::getProfile));

		/*
		 * check for validation errors and filter out the profiles and corresponding
		 * data containing errors
		 */
		sortedAndGroupedReadings.entrySet().stream()
				.forEach(entry -> validateEntryAndPopulateDb(entry.getKey(), entry.getValue(), rejectedProfiles));

		return rejectedProfiles;
	}

	@Transactional(rollbackFor = Exception.class)
	private void validateEntryAndPopulateDb(String profile, List<MeterReadingDto> meterReadings,
			Map<String, String> rejectedProfiles) {

		/* first check the database for the profile */
		MeterProfile meterProfile = meterProfileRepo.findByProfile(profile);
		if (null == meterProfile) {
			rejectedProfiles.putIfAbsent(profile, PROFILE_DOES_NOT_EXIST);
			return;
		}

		/*
		 * profile exists, now compare the monthly readings and return false if the next
		 * month's reading is less than previous month
		 */
		MeterData meterData = null;
		int loopStart = meterReadings.size() - 1;
		long totalConsumption = meterReadings.get(loopStart).getReading();
		List<MeterData> meterDataList = new ArrayList<>();
		for (int i = loopStart; i > 0; i--) {
			processEachMeterReading(meterReadings, i, profile, meterData, totalConsumption, meterProfile,
					rejectedProfiles, meterDataList);
		}

		/* the profile data is valid, populate the meter id in the database */
		meterProfile.setMeterId(meterReadings.get(0).getMeterId());
		meterProfileRepo.saveAndFlush(meterProfile);

		/* add data for january */
		meterData = meterDataRepo.findByMeterProfileAndMonth(meterProfile, Month.JANUARY.name());
		meterData.setConsumption(meterReadings.get(0).getReading().longValue());
		meterDataList.add(meterData);

		/* add the meterDataList with consumption values to the database */
		meterDataRepo.saveAll(meterDataList);
		meterDataRepo.flush();
	}

	private void processEachMeterReading(List<MeterReadingDto> meterReadings, int i, String profile,
			MeterData meterData, long totalConsumption, MeterProfile meterProfile, Map<String, String> rejectedProfiles,
			List<MeterData> meterDataList) {
		int nextMonthReading = meterReadings.get(i).getReading();
		int previousMonthReading = meterReadings.get(i - 1).getReading();
		if (nextMonthReading < previousMonthReading) {
			rejectedProfiles.putIfAbsent(profile, PROFILE_HAS_INCONSISTENT_DATA);
			return;
		}

		/* check the readings are consistent with fraction with a tolerance of 25% */
		long monthlyConsumption = nextMonthReading - previousMonthReading;
		meterData = meterDataRepo.findByMeterProfileAndMonth(meterProfile, meterReadings.get(i).getMonth().name());
		double expectedConsumption = meterData.getFraction() * totalConsumption;
		if ((monthlyConsumption < expectedConsumption * 0.75) || (monthlyConsumption > expectedConsumption * 1.25)) {
			rejectedProfiles.putIfAbsent(profile, PROFILE_HAS_INCONSISTENT_DATA);
			return;
		}

		/*
		 * populate the consumption in the meterData object and add it to the
		 * meterDataList
		 */
		meterData.setConsumption(monthlyConsumption);
		meterData.setMonth(meterReadings.get(i).getMonth().name());
		meterDataList.add(meterData);

	}

	/*
	 * this method should ideally be in another class, but keeping it here for
	 * simplicity
	 */
	private MeterProfile populateAndSaveMeterProfile(String profile) {
		MeterProfile meterProfile = new MeterProfile();
		meterProfile.setProfile(profile);
		meterProfile = meterProfileRepo.saveAndFlush(meterProfile);
		return meterProfile;
	}

	/*
	 * this method should ideally be in another class, but keeping it here for
	 * simplicity
	 */
	private void populateAndSaveMeterData(MeterProfile meterProfile, Month month, Double fraction) {
		MeterData meterData = new MeterData();
		meterData.setMeterProfile(meterProfile);
		meterData.setMonth(month.name());
		meterData.setFraction(fraction);
		meterDataRepo.saveAndFlush(meterData);
	}
}
