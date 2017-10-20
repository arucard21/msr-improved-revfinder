package com.github.arucard21.msr.revfinder;

import javax.json.Json;
import javax.json.JsonObject;

public class GerritUser {
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GerritUser other = (GerritUser) obj;
		if (id != other.id)
			return false;
		return true;
	}

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
