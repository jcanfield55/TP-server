package com.nimbler.tp.dataobject.flurry;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class FlurrySessionEventData implements Serializable{

	private static final long serialVersionUID = -4145516874045186403L;
	private String id;

	private String metaData;

	@SerializedName("u")
	private String u;

	@SerializedName("v")
	private String v;

	@SerializedName("dv")
	private String dv;

	@SerializedName("t")
	private String t;

	@SerializedName("uid")
	private String uid; 

	@SerializedName("l")
	private List<FlurryLogEntry> l; 


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMetaData() {
		return metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	@Override
	public String toString() {
		return "FlurrySessionEventData [u=" + u + ", v=" + v + ", dv=" + dv
				+ ", t=" + t + ", uid=" + uid + ", l=" + l + "]";
	}

	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public String getU() {
		return u;
	}

	public void setU(String u) {
		this.u = u;
	}

	public String getDv() {
		return dv;
	}

	public void setDv(String dv) {
		this.dv = dv;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	public List<FlurryLogEntry> getL() {
		return l;
	}

	public void setL(List<FlurryLogEntry> l) {
		this.l = l;
	}

	public static class FlurryLogEntry{

		@SerializedName("e")
		private String e;

		@SerializedName("o")
		private long o;

		@SerializedName("d")
		private long d;

		@SerializedName("p")		
		private Map<String,String> p;

		public String getE() {
			return e;
		}

		public void setE(String e) {
			this.e = e;
		}

		public long getO() {
			return o;
		}

		public void setO(long o) {
			this.o = o;
		}

		public long getD() {
			return d;
		}

		public void setD(long d) {
			this.d = d;
		}

		public Map<String, String> getP() {
			return p;
		}

		public void setP(Map<String, String> p) {
			this.p = p;
		}

		@Override
		public String toString() {
			return "FlurryLogEntry [e=" + e + ", o=" + o + ", d=" + d + ", p="
					+ p + "]";
		}
	}
}

