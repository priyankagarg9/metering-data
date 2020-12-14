package com.meter.energy.consumption.service.impl;

import java.time.Month;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meter.energy.consumption.controller.dto.MeterMonthlyReadingDto;
import com.meter.energy.consumption.exception.ApplicationException;
import com.meter.energy.consumption.persistence.model.MeterData;
import com.meter.energy.consumption.persistence.model.MeterProfile;
import com.meter.energy.consumption.persistence.repository.MeterDataRepository;
import com.meter.energy.consumption.persistence.repository.MeterProfileRepository;
import com.meter.energy.consumption.service.MeteringDataService;

/**
 * Implementation of {@link MeteringDataService}.
 */
@Service
public class MeteringDataServiceImpl implements MeteringDataService {

	@Autowired
	private MeterProfileRepository meterProfileRepo;

	@Autowired
	private MeterDataRepository meterDataRepo;

	private static final double THRESHOLD = 0.01;
	private static final String FRACTION_TOTAL_INCORRECT_PROFILE = "Fraction total for the given profile is incorrect: ";
	private static final String PROFILE_DOES_NOT_EXIST = "The profile does not exist : ";
	private static final String PROFILE_HAS_INCONSISTENT_DATA = "The profile has inconsistent data: ";
	private static final String METER_ID_DOES_NOT_EXIST = "The meter id does not exist : ";
	private static final String CONSUMPTION_DOES_NOT_EXIST = "Consumption for the given meter id and month does not exist: ";

	@Override
	@Transactional(rollbackFor = ApplicationException.class)
	public void processFractionData(String profile, Map<Month, Double> monthlyFractions) {

		/* calculate the sum and throw an error if sum is not equal to 1.0 */
		double sum = monthlyFractions.values().stream().reduce(0.0, Double::sum);
		if (Math.abs(sum - 1.0) > THRESHOLD) {
			throw new ApplicationException(FRACTION_TOTAL_INCORRECT_PROFILE + profile);
		}

		MeterProfile meterProfile = meterProfileRepo.findByProfile(profile);

		/* if profile exists, delete all fractions data from database */
		if (null != meterProfile) {
			meterDataRepo.deleteAllByMeterProfile(meterProfile);
		} else {
			/* the profile does not exist in database, adding it */
			meterProfile = populateAndSaveMeterProfile(profile);
		}

		final MeterProfile meterProfileData = meterProfile;

		// adding the fractions data to database
		monthlyFractions.entrySet()
				.forEach(entry -> populateAndSaveMeterData(meterProfileData, entry.getKey(), entry.getValue()));
	}

	@Override
	public Long getConsumption(String meterId, Month month) {
		MeterProfile meterProfile = meterProfileRepo.findByMeterId(meterId);
		if (null == meterProfile) {
			throw new ApplicationException(METER_ID_DOES_NOT_EXIST + meterId);
		}

		MeterData meterData = meterDataRepo.findByMeterProfileAndMonth(meterProfile, month.name());
		if (null == meterData) {
			throw new ApplicationException(CONSUMPTION_DOES_NOT_EXIST + meterId + " AND " + month.name());
		}
		return meterData.getConsumption();
	}

	@Override
	@Transactional(rollbackFor = ApplicationException.class)
	public void processMeterReadings(String meterId, MeterMonthlyReadingDto meterMonthlyReadingDto) {

		/* first check the database for the profile */
		MeterProfile meterProfile = meterProfileRepo.findByProfile(meterMonthlyReadingDto.getProfile());
		if (null == meterProfile) {
			throw new ApplicationException(PROFILE_DOES_NOT_EXIST + meterMonthlyReadingDto.getProfile());
		}

		/* create a sorted map from existing hashmap */
		Map<Month, Long> treeMap = new TreeMap<>();
		treeMap.putAll(meterMonthlyReadingDto.getMonthlyReadings());

		/*
		 * profile exists, now compare the monthly readings and throw exception in case
		 * of validation error
		 */
		MeterData meterData = null;
		int loopStart = treeMap.size() - 1;
		long totalConsumption = treeMap.get(Month.DECEMBER);

		for (int i = loopStart; i > 0; i--) {
			processEachMeterReading(treeMap, i, meterMonthlyReadingDto.getProfile(), meterData, totalConsumption,
					meterProfile);
		}

		/* adding the data for january */
		meterData = meterDataRepo.findByMeterProfileAndMonth(meterProfile, Month.JANUARY.name());
		meterData.setConsumption(treeMap.get(Month.JANUARY));
		meterDataRepo.saveAndFlush(meterData);

		/* populate the meter id in the database */
		meterProfile.setMeterId(meterId);
		meterProfileRepo.saveAndFlush(meterProfile);

	}

	private void processEachMeterReading(Map<Month, Long> treeMap, int i, String profile, MeterData meterData,
			long totalConsumption, MeterProfile meterProfile) {
		long nextMonthReading = treeMap.get(Month.of(i + 1));
		long previousMonthReading = treeMap.get(Month.of(i));
		if (nextMonthReading < previousMonthReading) {
			throw new ApplicationException(PROFILE_HAS_INCONSISTENT_DATA + profile);
		}

		/* check the readings are consistent with fraction with a tolerance of 25% */
		long monthlyConsumption = nextMonthReading - previousMonthReading;
		meterData = meterDataRepo.findByMeterProfileAndMonth(meterProfile, Month.of(i + 1).name());
		double expectedConsumption = meterData.getFraction() * totalConsumption;
		if ((monthlyConsumption < expectedConsumption * 0.75) || (monthlyConsumption > expectedConsumption * 1.25)) {
			throw new ApplicationException(PROFILE_HAS_INCONSISTENT_DATA + profile);
		}

		/*
		 * populate the consumption in the meterData object and add it to the
		 * meterDataList
		 */
		meterData.setConsumption(monthlyConsumption);
		meterData.setMonth(Month.of(i + 1).name());
		meterDataRepo.saveAndFlush(meterData);
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
