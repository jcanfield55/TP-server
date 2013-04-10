/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.List;

import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

/**
 * The Class GtfsContext.
 *
 * @author nirmal
 */
public class GtfsContext {
	private List<GtfsBundle> gtfsBundles;

	public List<GtfsBundle> getGtfsBundles() {
		return gtfsBundles;
	}

	public void setGtfsBundles(List<GtfsBundle> gtfsBundles) {
		this.gtfsBundles = gtfsBundles;
	}
	public GtfsBundle getBundle(AGENCY_TYPE type) {
		for (GtfsBundle bundle: gtfsBundles) {
			if(bundle.getAgencyType().equals(type)){
				return bundle;
			}
		}
		return null;
	}
}
