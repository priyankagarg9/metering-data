package com.meter.energy.consumption.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * In real system, this will be separate service that exposes an API which will fetch information based on id. 
 * Keeping it very simple to cover basic scenarios.
 */
@Entity
@Table(name = "meter_data")
public class MeterData {

    @Id
    @Column(columnDefinition = "INT(11) UNSIGNED", name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne()
    @JoinColumn(name = "meter_profile_id", nullable = false)
    private MeterProfile meterProfile;
    
    @Column(name = "month" , nullable = false, length = 9)
    private String month;
    
    @Column(name = "fraction" , nullable = false)
    private Double fraction;
    
    @Column(name = "consumption" , nullable = true, length = 11)
    private Long consumption;
    
    public MeterData() {
    	
    }

	public MeterData(Long id, MeterProfile meterProfile, String month, Double fraction, Long consumption) {
		super();
		this.id = id;
		this.meterProfile = meterProfile;
		this.month = month;
		this.fraction = fraction;
		this.consumption = consumption;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public MeterProfile getMeterProfile() {
		return meterProfile;
	}

	public void setMeterProfile(MeterProfile meterProfile) {
		this.meterProfile = meterProfile;
	}
	
	public String getMonth() {
		return month;
	}

	public void setMonth(String month) {
		this.month = month;
	}

	public Double getFraction() {
		return fraction;
	}

	public void setFraction(Double fraction) {
		this.fraction = fraction;
	}

	public Long getConsumption() {
		return consumption;
	}

	public void setConsumption(Long consumption) {
		this.consumption = consumption;
	}

	@Override
	public String toString() {
		return "MeterData [id=" + id + ", meterProfile=" + meterProfile + ", month=" + month + ", fraction=" + fraction
				+ ", consumption=" + consumption + "]";
	}

}
