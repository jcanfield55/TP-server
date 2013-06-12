package com.nimbler.tp.dataobject.wmata;

import java.util.List;

import com.google.gson.annotations.SerializedName;


public class WmataRailLines {

	@SerializedName("Lines")
	private List<RailLine> lines;

	public List<RailLine> getLines() {
		return lines;
	}

	public void setLines(List<RailLine> lines) {
		this.lines = lines;
	}

	@Override
	public String toString() {
		return "WmataRailLines [lines=" + lines + "]";
	}
}
