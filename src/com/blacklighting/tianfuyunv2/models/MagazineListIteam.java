package com.blacklighting.tianfuyunv2.models;

import android.graphics.Bitmap;

public class MagazineListIteam {
	private String period, name, image;
	Bitmap realImag;

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Bitmap getRealImag() {
		return realImag;
	}

	public void setRealImag(Bitmap realImag) {
		this.realImag = realImag;
	}

}
