package com.github.arucard21.msr.revfinder;

import javax.json.Json;
import javax.json.JsonObject;

public class Reviewer {
	private final int id;
	private final String name;
	
	public Reviewer(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Reviewer(JsonObject json) {
		this.id = json.getInt("ID");
		this.name = json.getString("Name");
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public JsonObject asJsonObject(){
		return Json.createObjectBuilder()
				.add("ID", getId())
				.add("Name", getName())
				.build();
	}
}
