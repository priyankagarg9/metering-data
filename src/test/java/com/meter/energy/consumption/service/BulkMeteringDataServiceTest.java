package com.meter.energy.consumption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.meter.energy.consumption.controller.dto.MeterReadingDto;
import com.meter.energy.consumption.controller.dto.ProfileFractionDto;
import com.meter.energy.consumption.exception.ApplicationException;
import com.meter.energy.consumption.persistence.model.MeterData;
import com.meter.energy.consumption.persistence.model.MeterProfile;
import com.meter.energy.consumption.persistence.repository.MeterDataRepository;
import com.meter.energy.consumption.persistence.repository.MeterProfileRepository;
import com.meter.energy.consumption.service.impl.BulkMeteringDataServiceImpl;

@ExtendWith(SpringExtension.class)
public class BulkMeteringDataServiceTest {

    @Mock
	private MeterProfileRepository meterProfileRepo;

    @Mock
	private MeterDataRepository meterDataRepo;

    private BulkMeteringDataService bulkMeteringDataService;
    
    private List<ProfileFractionDto> profileFractions;
    
    private List<MeterReadingDto> meterReadings;

    @BeforeEach
    public void setUp() {
    	bulkMeteringDataService = new BulkMeteringDataServiceImpl();
        
        profileFractions = new ArrayList<>();
        meterReadings = new ArrayList<>();
        
        ReflectionTestUtils.setField(bulkMeteringDataService, "meterProfileRepo", meterProfileRepo);
        ReflectionTestUtils.setField(bulkMeteringDataService, "meterDataRepo", meterDataRepo);
    }
    
    @Test
    public void test_processProfileFractions_incorrectFractionsSum() {
    	populateValidFractionsData();
    	profileFractions.add(new ProfileFractionDto(Month.JANUARY, "A", 0.20));
    	Assertions.assertThrows(ApplicationException.class, () -> {
            bulkMeteringDataService.processProfileFractions(profileFractions);
        });
    	
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).saveAndFlush(Mockito.any());
    }
    
    @Test
    public void test_processProfileFractions_ok_profileExists() {
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(new MeterData());
    	populateValidFractionsData();
    	
    	bulkMeteringDataService.processProfileFractions(profileFractions);
    	
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterProfileRepo, Mockito.never()).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).saveAndFlush(Mockito.any());
    }
    
    @Test
    public void test_processProfileFractions_ok_profileDoesNotExists() {
    	MeterProfile meterProfile = new MeterProfile(1l, "1", "A");
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(null);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(new MeterData());
    	populateValidFractionsData();
    	
    	bulkMeteringDataService.processProfileFractions(profileFractions);
    	
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).saveAndFlush(Mockito.any());
    }
    
    
    @Test
    public void test_processMeterReadings_profilesDoNotExist() {
    	populateValidMeterReadingsData();
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(null);
    	
    	Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
    	assertEquals(2, rejectedProfiles.size());
    	assertTrue(rejectedProfiles.containsKey("A"));
    	assertTrue(rejectedProfiles.containsKey("B"));
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_nextMonthReadingLessThanPreviousMonth() {
    	populateValidMeterReadingsData();
    	meterReadings.add(new MeterReadingDto("1001", "A",Month.DECEMBER, 85));
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);

    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(meterData);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	
		Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
		assertEquals(1, rejectedProfiles.size());
    	assertTrue(rejectedProfiles.containsKey("A"));
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    }
    
     @Test
    public void test_processMeterReadings_toleranceMoreThan25_higherLevel() {
    	populateValidMeterReadingsData();
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.DECEMBER, 140));
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);

    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(meterData);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	
    	Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
		assertEquals(1, rejectedProfiles.size());
    	assertTrue(rejectedProfiles.containsKey("A"));
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    }
    
     @Test
    public void test_processMeterReadings_toleranceMoreThan25_lowerLevel() {
    	populateValidMeterReadingsData();
     	meterReadings.add(new MeterReadingDto("1001", "A", Month.DECEMBER, 113));
     	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
     	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);

     	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
     	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
     	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(meterData);
     	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
     	
     	Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
 		assertEquals(1, rejectedProfiles.size());
     	assertTrue(rejectedProfiles.containsKey("A"));
     	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
     	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_ok() {
    	populateValidMeterReadingsData();
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);

    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(meterData);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	
    	Map<String, String> rejectedProfiles = bulkMeteringDataService.processMeterReadings(meterReadings);
		assertEquals(0, rejectedProfiles.size());
    	Mockito.verify(meterProfileRepo, Mockito.atLeastOnce()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.atLeastOnce()).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    }

    private void populateValidMeterReadingsData() {
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.SEPTEMBER, 90));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.FEBRUARY, 20));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.JULY, 70));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.OCTOBER, 100));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.APRIL, 40));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.FEBRUARY, 20));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.JULY, 70));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.AUGUST, 80));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.NOVEMBER, 110));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.NOVEMBER, 110));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.SEPTEMBER, 90));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.OCTOBER, 100));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.MAY, 50));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.JANUARY, 10));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.JUNE, 60));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.APRIL, 40));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.AUGUST, 80));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.MAY, 50));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.DECEMBER, 120));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.JANUARY, 10));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.JUNE, 60));
    	meterReadings.add(new MeterReadingDto("1001", "A", Month.MARCH, 30));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.DECEMBER, 120));
    	meterReadings.add(new MeterReadingDto("1002", "B", Month.MARCH, 30));
	}

	private void populateValidFractionsData() {
    	profileFractions.add(new ProfileFractionDto(Month.JANUARY, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.FEBRUARY, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.MARCH, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.MAY, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.JUNE, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.APRIL, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.FEBRUARY, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.MARCH, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.NOVEMBER, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.SEPTEMBER, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.SEPTEMBER, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.DECEMBER, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.JANUARY, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.APRIL, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.MAY, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.JUNE, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.JULY, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.AUGUST, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.OCTOBER, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.OCTOBER, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.NOVEMBER, "B", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.JULY, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.AUGUST, "A", 0.083));
    	profileFractions.add(new ProfileFractionDto(Month.DECEMBER, "B", 0.083));
	}

}
