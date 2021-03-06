package com.github.arucard21.msr.revfinder;

import javax.json.Json;
import javax.json.JsonObject;

public class RevisionFile {
	private final String fileName;
	private final int linesInserted;
	private final int linesDeleted;
	private final int sizesDelta;
	private final int size;
	
	public RevisionFile(String fileName, int linesInserted, int linesDeleted, int sizesDelta, int size) {
		this.fileName = fileName;
		this.linesInserted = linesInserted;
		this.linesDeleted = linesDeleted;
		this.sizesDelta = sizesDelta;
		this.size = size;
	}
	
	public JsonObject asJsonObject(){
		return Json.createObjectBuilder()
				.add("fileName", getFileName())
				.build();
	}

	
	public String getFileName() {
		return fileName;
	}
	public int getLinesInserted() {
		return linesInserted;
	}
	public int getLinesDeleted() {
		return linesDeleted;
	}
	public int getSizesDelta() {
		return sizesDelta;
	}
	public int getSize() {
		return size;
	}
}
