package com.meter.energy.consumption.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.meter.energy.consumption.dto.MeterMonthlyReadingDto;
import com.meter.energy.consumption.exception.ApplicationException;
import com.meter.energy.consumption.persistence.model.MeterData;
import com.meter.energy.consumption.persistence.model.MeterProfile;
import com.meter.energy.consumption.persistence.repository.MeterDataRepository;
import com.meter.energy.consumption.persistence.repository.MeterProfileRepository;
import com.meter.energy.consumption.service.impl.MeteringDataServiceImpl;

@ExtendWith(SpringExtension.class)
public class MeteringDataServiceTest {

    @Mock
	private MeterProfileRepository meterProfileRepo;

    @Mock
	private MeterDataRepository meterDataRepo;

    private MeteringDataService meteringDataService;
    
    private Map<Month, Double> profileFractions;
    
    private MeterMonthlyReadingDto meterMonthlyReadingDto;

    @BeforeEach
    public void setUp() {
        meteringDataService = new MeteringDataServiceImpl();
        
        profileFractions = new HashMap<>();
        meterMonthlyReadingDto = new MeterMonthlyReadingDto();
        
        ReflectionTestUtils.setField(meteringDataService, "meterProfileRepo", meterProfileRepo);
        ReflectionTestUtils.setField(meteringDataService, "meterDataRepo", meterDataRepo);
    }
    
    @Test
    public void test_processFractionData_incorrectFractionsSum() {
    	populateValidFractionsData("A");
    	profileFractions.put(Month.JANUARY, 0.20);
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.processFractionData("A", profileFractions);
        });
    	
    	Mockito.verify(meterProfileRepo, Mockito.never()).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.never()).deleteAllByMeterProfile(Mockito.any());
    	Mockito.verify(meterProfileRepo, Mockito.never()).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.never()).saveAndFlush(Mockito.any());
    }
    
    @Test
    public void test_processFractionData_ok_profileExists() {
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.doNothing().when(meterDataRepo).deleteAllByMeterProfile(Mockito.any());
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(new MeterData());
    	populateValidFractionsData("A");
    	
    	meteringDataService.processFractionData("A", profileFractions);
    	
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo).deleteAllByMeterProfile(Mockito.any());
    	Mockito.verify(meterProfileRepo, Mockito.never()).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.atLeast(12)).saveAndFlush(Mockito.any());
    }
    
    @Test
    public void test_processFractionData_ok_profileDoesNotExists() {
    	MeterProfile meterProfile = new MeterProfile(1l, "1", "A");
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(null);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(new MeterData());
    	populateValidFractionsData("A");
    	
    	meteringDataService.processFractionData("A", profileFractions);
    	
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.never()).deleteAllByMeterProfile(Mockito.any());
    	Mockito.verify(meterProfileRepo).saveAndFlush(Mockito.any());
    	Mockito.verify(meterDataRepo, Mockito.atLeast(12)).saveAndFlush(Mockito.any());
    }
    
    @Test
    public void test_getConsumption_meterIdDoesNotExist() {
    	Mockito.when(meterProfileRepo.findByMeterId(Mockito.anyString())).thenReturn(null);
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.getConsumption("1", Month.JANUARY);
        });
    	
    	Mockito.verify(meterProfileRepo).findByMeterId(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.never()).findByMeterProfileAndMonth(Mockito.any(), Mockito.any());
    }
    
    @Test
    public void test_getConsumption_consumptionDoesNotExist() {
    	MeterProfile meterProfile = new MeterProfile(1l, "1", "A");
    	Mockito.when(meterProfileRepo.findByMeterId(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(), Mockito.any())).thenReturn(null);
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.getConsumption("1", Month.JANUARY);
        });
    	
    	Mockito.verify(meterProfileRepo).findByMeterId(Mockito.anyString());
    	Mockito.verify(meterDataRepo).findByMeterProfileAndMonth(Mockito.any(), Mockito.any());
    }
    
    @Test
    public void test_getConsumption_ok() {
    	MeterProfile meterProfile = new MeterProfile(1l, "1", "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, 100l);
    	Mockito.when(meterProfileRepo.findByMeterId(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(), Mockito.any())).thenReturn(meterData);
        
    	Long consumption = meteringDataService.getConsumption("1", Month.JANUARY);
    	assertEquals(100l, consumption);
    	
    	Mockito.verify(meterProfileRepo).findByMeterId(Mockito.anyString());
    	Mockito.verify(meterDataRepo).findByMeterProfileAndMonth(Mockito.any(), Mockito.any());
    }
    
    @Test
    public void test_processMeterReadings_profileDoesNotExist() {
    	populateValidMeterReadingsData("A");
    	
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(null);
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.processMeterReadings("1", meterMonthlyReadingDto);
        });
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_nextMonthReadingLessThanPreviousMonth() {
    	populateValidMeterReadingsData("A");
    	meterMonthlyReadingDto.getMonthlyReadings().put(Month.DECEMBER, 85l);
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.processMeterReadings("1", meterMonthlyReadingDto);
        });
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_toleranceMoreThan25_higherLevel() {
    	populateValidMeterReadingsData("A");
    	meterMonthlyReadingDto.getMonthlyReadings().put(Month.DECEMBER, 130l);
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.processMeterReadings("1", meterMonthlyReadingDto);
        });
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_toleranceMoreThan25_lowerLevel() {
    	populateValidMeterReadingsData("A");
    	meterMonthlyReadingDto.getMonthlyReadings().put(Month.DECEMBER, 113l);
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	
    	Assertions.assertThrows(ApplicationException.class, () -> {
            meteringDataService.processMeterReadings("1", meterMonthlyReadingDto);
        });
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    }
    
    @Test
    public void test_processMeterReadings_ok() {
    	populateValidMeterReadingsData("A");
    	MeterProfile meterProfile = new MeterProfile(1l, null, "A");
    	MeterData meterData = new MeterData(1l, meterProfile, Month.JANUARY.name(), 0.10, null);
    	Mockito.when(meterProfileRepo.findByProfile(Mockito.anyString())).thenReturn(meterProfile);
    	Mockito.when(meterDataRepo.findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString())).thenReturn(meterData);
    	Mockito.when(meterDataRepo.saveAndFlush(Mockito.any())).thenReturn(meterData);
    	Mockito.when(meterProfileRepo.saveAndFlush(Mockito.any())).thenReturn(meterProfile);
    	
    	meteringDataService.processMeterReadings("1", meterMonthlyReadingDto);
    	
    	Mockito.verify(meterProfileRepo).findByProfile(Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.atLeast(12)).findByMeterProfileAndMonth(Mockito.any(),Mockito.anyString());
    	Mockito.verify(meterDataRepo, Mockito.atLeast(12)).saveAndFlush(Mockito.any());
    	Mockito.verify(meterProfileRepo).saveAndFlush(Mockito.any());
    }

    private void populateValidMeterReadingsData(String profile) {
    	meterMonthlyReadingDto.setProfile(profile);
    	Map<Month, Long> monthlyReadings = new HashMap<>();
    	monthlyReadings.put(Month.SEPTEMBER, 90l);
    	monthlyReadings.put(Month.OCTOBER, 100l);
    	monthlyReadings.put(Month.APRIL, 40l);
    	monthlyReadings.put(Month.FEBRUARY, 20l);
    	monthlyReadings.put(Month.JULY, 70l);
    	monthlyReadings.put(Month.AUGUST, 80l);
    	monthlyReadings.put(Month.NOVEMBER, 110l);
    	monthlyReadings.put(Month.MAY, 50l);
    	monthlyReadings.put(Month.JUNE, 60l);
    	monthlyReadings.put(Month.DECEMBER, 120l);
    	monthlyReadings.put(Month.JANUARY, 10l);
    	monthlyReadings.put(Month.MARCH, 30l);
    	meterMonthlyReadingDto.setMonthlyReadings(monthlyReadings);
	}

	private void populateValidFractionsData(String profile) {
    	profileFractions.put(Month.JANUARY, 0.083);
    	profileFractions.put(Month.FEBRUARY, 0.083);
    	profileFractions.put(Month.MARCH, 0.083);
    	profileFractions.put(Month.APRIL, 0.083);
    	profileFractions.put(Month.MAY, 0.083);
    	profileFractions.put(Month.JUNE, 0.083);
    	profileFractions.put(Month.JULY, 0.083);
    	profileFractions.put(Month.AUGUST, 0.083);
    	profileFractions.put(Month.SEPTEMBER, 0.083);
    	profileFractions.put(Month.OCTOBER, 0.083);
    	profileFractions.put(Month.NOVEMBER, 0.083);
    	profileFractions.put(Month.DECEMBER, 0.083);
	}

}
