package com.meter.energy.consumption.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * In real system, this will be separate service that exposes an API which will
 * fetch information based on id. Keeping it very simple to cover basic
 * scenarios.
 */
@Entity
@Table(name = "meter_profile")
public class MeterProfile {

	@Id
	@Column(columnDefinition = "INT(11) UNSIGNED", name = "id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "meter_id", unique = true, nullable = true, length = 11)
	private String meterId;

	@Column(name = "profile", unique = true, nullable = false, length = 11)
	private String profile;

	public MeterProfile() {

	}

	public MeterProfile(Long id, String meterId, String profile) {
		super();
		this.id = id;
		this.meterId = meterId;
		this.profile = profile;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	@Override
	public String toString() {
		return "MeterProfile [id=" + id + ", meterId=" + meterId + ", profile=" + profile + "]";
	}

}
