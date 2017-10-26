package com.github.arucard21.msr;

public enum Project {ANDROID("android"), CHROMIUM("chromium"), OPENSTACK("openstack"), QT("qt"), WIKIMEDIA("wikimedia"),
	ECLIPSE("eclipse");

	public final String name;
	Project(String name){
		this.name = name;
	}
}
