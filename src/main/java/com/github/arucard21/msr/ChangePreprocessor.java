package com.github.arucard21.msr;

import java.util.function.Function;

import javax.json.JsonValue;

public class ChangePreprocessor implements Function<JsonValue, ReviewableChange> {

	@Override
	public ReviewableChange apply(JsonValue t) {
		return new ReviewableChange(t.asJsonObject(), false);
	}

}
