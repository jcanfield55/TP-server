package com.nimbler.tp.dataobject.wmata;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.annotations.SerializedName;

public class RailStopSequence implements Serializable{
	private static final long serialVersionUID = -5944275900656689749L;

	@SerializedName("Path")
	private List<RailStopSeqElement> path;

	private transient String[] stopCodeSequence;

	public boolean isEmpty() {
		return (path==null || path.isEmpty());
	}

	/**
	 * @return the path
	 */
	public List<RailStopSeqElement> getPath() {
		return path;
	}


	/**
	 * @param path the path to set
	 */
	public void setPath(List<RailStopSeqElement> p) {
		path = p;
	}
	public void addPath(RailStopSeqElement p) {
		if(path==null)
			path = new ArrayList<RailStopSeqElement>();
		path.add(p);
	}


	/**
	 * @return the stopCodeSequence
	 */
	public String[] getStopCodeSequence() {
		if(stopCodeSequence==null && path!=null){
			synchronized (this) {
				Collections.sort(path,new Comparator<RailStopSeqElement>() {
					@Override
					public int compare(RailStopSeqElement o1,RailStopSeqElement o2) {
						if(o1!=null && o2!=null){
							return NumberUtils.toInt(o1.getSeqNum()) - NumberUtils.toInt(o2.getSeqNum());
						}
						return 0;
					}
				});
				if(stopCodeSequence==null && path!=null){
					stopCodeSequence = new String[path.size()];
					for (int i = 0; i < path.size(); i++) {
						stopCodeSequence[i] = path.get(i).getStationCode();
					}
				}
			}
		}
		return stopCodeSequence;
	}

	/**
	 * Checks if is valid sequence.
	 *
	 * @param start the start
	 * @param interMediate the inter mediate
	 * @param end the end
	 * @return true, if is valid sequence
	 */
	public boolean isValidSequence(String start,String interMediate, String end){
		String[] stopSeq = getStopCodeSequence();
		int startSeq = ArrayUtils.indexOf(stopSeq, start);
		int midSeq = ArrayUtils.indexOf(stopSeq, interMediate);
		int endSeq = ArrayUtils.indexOf(stopSeq, end);
		return (startSeq!=-1 && midSeq!=-1 && endSeq!=-1 && ((startSeq<=midSeq && midSeq<=endSeq) || (startSeq>=midSeq && midSeq>=endSeq)));
	}
	public boolean isValidSequence(List<String> start,List<String> interMediate, String end){
		String[] stopSeq = getStopCodeSequence();
		int startSeq = getSequenceNo(stopSeq,start);
		int midSeq = getSequenceNo(stopSeq, interMediate);
		int endSeq = ArrayUtils.indexOf(stopSeq, end);
		return (startSeq!=-1 && midSeq!=-1 && endSeq!=-1 && ((startSeq<=midSeq && midSeq<=endSeq) || (startSeq>=midSeq && midSeq>=endSeq)));
	}

	private int getSequenceNo(String[] stopSeq,List<String> start) {
		int seq = -1;
		for (String strt : start) {
			seq = ArrayUtils.indexOf(stopSeq, strt);
			if(seq!=-1)
				break;
		}
		return seq;
	}

	public RailStopSeqElement getElementForStop(String stopId) {
		if(path!=null){
			for (RailStopSeqElement element : path) {
				if(equalsIgnoreCase(element.getStationCode(),stopId))
					return element;
			}
		}
		return null;
	}
}
