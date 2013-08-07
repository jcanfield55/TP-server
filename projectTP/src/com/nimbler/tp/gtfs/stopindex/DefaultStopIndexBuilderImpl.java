/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs.stopindex;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.Transformer;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Trip;
import org.springframework.beans.factory.annotation.Autowired;

import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.GtfsContext;
import com.nimbler.tp.gtfs.GtfsDataService;
import com.nimbler.tp.gtfs.TripStopIndex;
import com.nimbler.tp.util.GtfsUtils;

/**
 * The Class DefaultStopIndexBuilderImpl.
 *
 * @author nirmal
 */

public class DefaultStopIndexBuilderImpl extends TripStopIndexBuilder{

	@Autowired
	GtfsContext gtfsContext;
	@Autowired
	GtfsDataService gtfsDataService;

	public DefaultStopIndexBuilderImpl() {
	}

	public void buildIndex(TripStopIndex stopIndex, List<GtfsBundle> bundles)throws RuntimeException {		
		try {
			for (GtfsBundle bundle : bundles) {
				if(!bundle.getAgencyType().equals(agency))
					continue;
				GtfsRelationalDaoImpl context;
				context = GtfsUtils.getGtfsDao(bundle.getValidFile(), this.lstClasses);
				stopIndex.save(bundle.getAgencyType(), context,new Transformer() {
					@Override
					public Object transform(Object obj) {
						Trip trip = (Trip) obj;
						return trip.getId().getId();
					}
				});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
