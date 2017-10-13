package com.github.arucard21.msr.revfinder;

public class Reviewer {
	private final String id;
	private final String name;
	
	public Reviewer(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
