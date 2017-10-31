package com.github.arucard21.msr;

public enum Project {OPENSTACK("openstack"), QT("qt"), MEDIAWIKI("mediawiki"), ECLIPSE("eclipse");

	public final String name;
	Project(String name){
		this.name = name;
	}
}
