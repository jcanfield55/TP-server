package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.annotations.SerializedName;
import com.nimbler.tp.util.ComUtils;


public class RailStation extends Point implements Serializable,Cloneable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1372736388802194331L;

	/**
	 * 
	 */

	public RailStation() {
	}

	@SerializedName("Lat")
	private String lat;
	@SerializedName("Lon")
	private String lon;
	@SerializedName("Name")
	private String name;

	@SerializedName("StationTogether1")
	private String stationTogether1;

	@SerializedName("StationTogether2")
	private String stationTogether2;//unused

	@SerializedName("LineCode1")
	private String lineCode1;

	@SerializedName("LineCode2")
	private String lineCode2;

	@SerializedName("LineCode3")
	private String lineCode3;//unused

	@SerializedName("LineCode4")
	private String lineCode4;//unused

	@SerializedName("Code")
	private String code; //station code

	private double gtfsDistance;


	public String getLat() {
		return lat;
	}
	public double getGtfsDistance() {
		return gtfsDistance;
	}
	public List<String> getAllStationLineCodes() {
		List<String> res = new ArrayList<String>();
		if(!ComUtils.isEmptyString(lineCode1))
			res.add(lineCode1);
		if(!ComUtils.isEmptyString(lineCode2))
			res.add(lineCode2);
		if(!ComUtils.isEmptyString(lineCode3))
			res.add(lineCode3);
		if(!ComUtils.isEmptyString(lineCode4))
			res.add(lineCode4);
		return res;
	}

	public RailStation(RailStation r) {
		super(r.getX(), r.getY());
		setCode(r.getCode());
		setGtfsDistance(r.getGtfsDistance());
		setLat(r.getLat());
		setLineCode1(r.getLineCode1());
		setLineCode2(r.getLineCode2());
		setLineCode3(r.getLineCode3());
		setLineCode4(r.getLineCode4());
		setLon(r.getLon());
		setName(r.getName());
		setStationTogether1(r.getStationTogether1());
		setStationTogether2(r.getStationTogether2());
	}
	public List<String> getAllStationCodes() {
		List<String> res = new ArrayList<String>();
		if(!ComUtils.isEmptyString(code))
			res.add(code);
		if(!ComUtils.isEmptyString(stationTogether1))
			res.add(stationTogether1);
		if(!ComUtils.isEmptyString(stationTogether2))
			res.add(stationTogether2);
		return res;
	}
	public boolean haveStationCode(String c) {
		return StringUtils.equalsIgnoreCase(code,c) || StringUtils.equalsIgnoreCase(stationTogether1,c)|| StringUtils.equalsIgnoreCase(stationTogether2,c);
	}


	public void setGtfsDistance(double gtfsDistance) {
		this.gtfsDistance = gtfsDistance;
	}
	public boolean isMultipleStation() {
		return !ComUtils.isEmptyString(stationTogether1) || !ComUtils.isEmptyString(stationTogether1);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result
				+ ((lineCode1 == null) ? 0 : lineCode1.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RailStation other = (RailStation) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (lineCode1 == null) {
			if (other.lineCode1 != null)
				return false;
		} else if (!lineCode1.equals(other.lineCode1))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	public boolean isMultipleLine() {
		return !ComUtils.isEmptyString(lineCode2)||
				!ComUtils.isEmptyString(lineCode3) || !ComUtils.isEmptyString(lineCode4);
	}
	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public double getX() {
		if(Double.isNaN(x) && lat!=null)
			setX(NumberUtils.toDouble(lat));
		return super.getX();
	}

	@Override
	public double getY() {
		if(Double.isNaN(y) && lon!=null)
			setY(NumberUtils.toDouble(lon));
		return super.getY();
	}


	public String getLon() {
		return lon;
	}

	public String getStationTogether1() {
		return stationTogether1;
	}

	public void setStationTogether1(String stationTogether1) {
		this.stationTogether1 = stationTogether1;
	}

	public String getStationTogether2() {
		return stationTogether2;
	}

	public void setStationTogether2(String stationTogether2) {
		this.stationTogether2 = stationTogether2;
	}

	public String getLineCode1() {
		return lineCode1;
	}

	public void setLineCode1(String lineCode1) {
		this.lineCode1 = lineCode1;
	}

	public String getLineCode2() {
		return lineCode2;
	}

	public void setLineCode2(String lineCode2) {
		this.lineCode2 = lineCode2;
	}

	public String getLineCode3() {
		return lineCode3;
	}

	public void setLineCode3(String lineCode3) {
		this.lineCode3 = lineCode3;
	}

	public String getLineCode4() {
		return lineCode4;
	}

	public void setLineCode4(String lineCode4) {
		this.lineCode4 = lineCode4;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public String getName() {
		return name;
	}
	public boolean haveLine(String line,boolean ignoreCase) {
		if(ignoreCase){
			if(StringUtils.equalsIgnoreCase(line,lineCode1)	|| StringUtils.equalsIgnoreCase(line,lineCode2)  || StringUtils.equalsIgnoreCase(line,lineCode3) || StringUtils.equalsIgnoreCase(line,lineCode4)){
				return true;
			}
		}else{
			if(StringUtils.equals(line,lineCode1)|| StringUtils.equals(line,lineCode2)  || StringUtils.equals(line,lineCode3) || StringUtils.equals(line,lineCode4)){
				return true;
			}
		}
		return false;
	}

	public boolean haveLine(String route) {
		return haveLine(route, false);
	}


	public void setName(String name) {
		this.name = name;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RailStation ["
				+ (name != null ? "name=" + name + ", " : "")
				+ (code != null ? "code=" + code + ", " : "")
				+ (lat != null ? "lat=" + lat + ", " : "")
				+ (lon != null ? "lon=" + lon + ", " : "")
				+ (stationTogether1 != null ? "stationTogether1="
						+ stationTogether1 + ", " : "")
						+ (stationTogether2 != null ? "stationTogether2="
								+ stationTogether2 + ", " : "")
								+ (lineCode1 != null ? "lineCode1=" + lineCode1 + ", " : "")
								+ (lineCode2 != null ? "lineCode2=" + lineCode2 + ", " : "")
								+ (lineCode3 != null ? "lineCode3=" + lineCode3 + ", " : "")
								+ (lineCode4 != null ? "lineCode4=" + lineCode4 : "") + "]";
	}
}
