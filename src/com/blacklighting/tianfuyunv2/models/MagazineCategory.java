package com.blacklighting.tianfuyunv2.models;

import java.util.List;

public class MagazineCategory {
	String name;
	List<Passage> passages;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Passage> getPassages() {
		return passages;
	}

	public void setPassages(List<Passage> passages) {
		this.passages = passages;
	}

}
