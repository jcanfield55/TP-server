/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs;

import java.util.List;

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
}
