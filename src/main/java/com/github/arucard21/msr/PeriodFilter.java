package com.github.arucard21.msr;

import java.time.LocalDateTime;
import java.util.function.Predicate;

import javax.json.JsonValue;

public class PeriodFilter implements Predicate<JsonValue> {

	/**
	 * Match only changes created in 2016
	 */
	@Override
	public boolean test(JsonValue t) {
		String createdDateTime = t.asJsonObject().getString("created");
		return createdDateTime.contains("2016");
//		LocalDateTime created = LocalDateTime.parse(createdDateTime);
//		return created.getYear() == 2016;
	}

}
