package com.nimbler.tp.dataobject.bart;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="estimate")
public class Estimate {
	private String minutes;
	private String platform;
	private String direction;
	private String length;
	private String color;
	private String hexcolor;
	private String bikeflag;
	private long createTime = System.currentTimeMillis();
	
	public String getMinutes() {
		return minutes;
	}
	public void setMinutes(String minutes) {
		this.minutes = minutes;
	}
	public String getPlatform() {
		return platform;
	}

	public long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
	public void setPlatform(String platform) {
		this.platform = platform;
	}
	public String getDirection() {
		return direction;
	}
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getLength() {
		return length;
	}
	public void setLength(String length) {
		this.length = length;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getHexcolor() {
		return hexcolor;
	}
	public void setHexcolor(String hexcolor) {
		this.hexcolor = hexcolor;
	}
	public String getBikeflag() {
		return bikeflag;
	}
	public void setBikeflag(String bikeflag) {
		this.bikeflag = bikeflag;
	}
	@Override
	public String toString() {
		return "Estimate [bikeflag=" + bikeflag + ", color=" + color
				+ ", direction=" + direction + ", hexcolor=" + hexcolor
				+ ", length=" + length + ", minutes=" + minutes + ", platform="
				+ platform + "]";
	}

}
