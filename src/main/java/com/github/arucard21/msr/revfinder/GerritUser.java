package com.github.arucard21.msr.revfinder;

import javax.json.Json;
import javax.json.JsonObject;

public class GerritUser {
	private final int id;
	private final String name;
	
	public GerritUser(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public GerritUser(JsonObject json) {
		if(json.containsKey("_account_id")) {			
			this.id = json.getInt("_account_id", -1);
		}
		else {
			//try alternate key
			this.id = json.getInt("id", -1);
		}
		this.name = json.getString("name", "");
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	
	public JsonObject asJsonObject(){
		return Json.createObjectBuilder()
				.add("_account_id", getId())
				.add("name", getName())
				.build();
	}
}
