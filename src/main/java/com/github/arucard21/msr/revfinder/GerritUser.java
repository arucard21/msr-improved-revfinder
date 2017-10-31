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
	private double LCPScore;
	private double LCSuffScore;
	private double LCSubseqScore;
	private double LCSubstrScore;
	private double AVBinaryScore;
	
	public GerritUser(int id, String name) {
		this.id = id;
		this.name = name;
		setLCPScore(0.0);
		setLCSuffScore(0.0);
		setLCSubseqScore(0.0);
		setLCSubstrScore(0.0);

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
		try {
		return Json.createObjectBuilder()
				.add("_account_id", getId())
				.add("name", getName())
				.add("LCPScore", getLCPScore())
				.add("LCSuffScore", getLCSuffScore())
				.add("LCSubstrScore", getLCSubstrScore())
				.add("LCSubseqScore", getLCSubseqScore())
				.add("AVBinaryScore", getAVBinaryScore())
				.build();
	
		}catch(java.lang.NumberFormatException e) {
			return Json.createObjectBuilder()
					.add("_account_id", getId())
					.add("name", getName())
					.build();
		}
	}

	public double getLCPScore() {
		return LCPScore;
	}

	public void setLCPScore(Double lCPScore) {
		LCPScore = lCPScore;
	}

	public double getLCSuffScore() {
		return LCSuffScore;
	}

	public void setLCSuffScore(Double lCSuffScore) {
		LCSuffScore = lCSuffScore;
	}

	public double getLCSubseqScore() {
		return LCSubseqScore;
	}

	public void setLCSubseqScore(Double lCSubseqScore) {
		LCSubseqScore = lCSubseqScore;
	}

	public double getLCSubstrScore() {
		return LCSubstrScore;
	}

	public void setLCSubstrScore(Double lCSubstrScore) {
		LCSubstrScore = lCSubstrScore;
	}

	public double getAVBinaryScore() {
		return AVBinaryScore;
	}

	public void setAVBinaryScore(Double avBinaryScore) {
		AVBinaryScore = avBinaryScore;
	}


	// TODO COMbINE
	public double getCombinedFilepathScore() { return 1.0; }
}
