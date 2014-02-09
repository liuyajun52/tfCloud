/**
 * 
 */
package com.blacklighting.tianfuyunv2.models;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class NewsListIteam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6129425489695414212L;
	private String id, title, image, createTime;
	private Bitmap realImag;
	private boolean hasChecked;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public Bitmap getRealImag() {
		return realImag;
	}

	public void setRealImag(Bitmap realImag) {
		this.realImag = realImag;
	}

	public boolean isHasChecked() {
		return hasChecked;
	}

	public void setHasChecked(boolean hasChecked) {
		this.hasChecked = hasChecked;
	}

	
}
