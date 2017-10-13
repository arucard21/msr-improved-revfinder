package com.github.arucard21.msr;

import java.util.function.Function;

import javax.json.JsonValue;

public class ChangePreprocessor implements Function<JsonValue, CodeReview> {

	@Override
	public CodeReview apply(JsonValue t) {
		return new CodeReview(t.asJsonObject());
	}

}
