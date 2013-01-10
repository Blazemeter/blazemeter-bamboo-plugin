package com.blazemeter.bamboo.plugin.configuration;

public class BlazeMeterTests {
	private String id;
	private String name;
	
	public BlazeMeterTests(String id, String name){
		this.id = id;
		this.name = name;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return id.equals(obj);
	}

	@Override
	public String toString() {
		return name;
	}

	
}
