package com.github.arucard21.msr;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

import javax.json.JsonObject;
import javax.json.JsonValue;

import com.github.arucard21.msr.Project;

public class PeriodFilter implements Predicate<JsonValue> {

	private Project project;

	public PeriodFilter(Project project) {
		this.project = project;
	}

	/**
	 * Match only changes created in 2016
	 */
	@Override
	public boolean test(JsonValue t) {
		LocalDateTime created = getDate(t.asJsonObject(), "created");
		return created.isAfter(getStartDate()) && created.isBefore(getEndDate());
	}

	private LocalDateTime getEndDate() {
		switch (project) {
		case ANDROID: 
			return LocalDateTime.of(2012, Month.JANUARY, 1, 0, 0);
		case CHROMIUM: 
			return LocalDateTime.of(2012, Month.MAY, 1, 0, 0);
		case OPENSTACK: 
			return LocalDateTime.of(2012, Month.MAY, 1, 0, 0);
		case QT: 
			return LocalDateTime.of(2012, Month.MAY, 1, 0, 0);
		default:
			return LocalDateTime.now();
		}
	}

	private LocalDateTime getStartDate() {
		switch (project) {
		case ANDROID: 
			return LocalDateTime.of(2008, Month.OCTOBER, 1, 0, 0);
		case CHROMIUM: 
			return LocalDateTime.of(2011, Month.MAY, 1, 0, 0);
		case OPENSTACK: 
			return LocalDateTime.of(2011, Month.JULY, 1, 0, 0);
		case QT: 
			return LocalDateTime.of(2011, Month.MAY, 1, 0, 0);
		default:
			return LocalDateTime.now();
		}
	}

	private LocalDateTime getDate(JsonObject change, String dateField) {
		String dateString = change.getString(dateField);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");
		LocalDateTime  date = LocalDateTime.parse(dateString, formatter);
		return date;
	}

}