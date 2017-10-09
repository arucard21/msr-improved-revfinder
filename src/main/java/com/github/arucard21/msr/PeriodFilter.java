package com.github.arucard21.msr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.function.Predicate;

import javax.json.JsonValue;

public class PeriodFilter implements Predicate<JsonValue> {

	/**
	 * Match only changes created in 2016
	 */
	@Override
	public boolean test(JsonValue t) {
		String createdDateTime = t.asJsonObject().getString("created");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnnnnn");
		LocalDateTime created = LocalDateTime.parse(createdDateTime, formatter);
		return created.getYear() == 2016;
	}

}
