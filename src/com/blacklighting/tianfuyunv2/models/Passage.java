package com.blacklighting.tianfuyunv2.models;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * 杂志文章的模型类
 * 
 * @author Liu Yajun@blacklighting UESTC
 * 
 */
public class Passage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 804297521779714828L;
	private String title, id, source, content, image, createTime;
	private Bitmap realImag;
	private boolean hasChecked;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
