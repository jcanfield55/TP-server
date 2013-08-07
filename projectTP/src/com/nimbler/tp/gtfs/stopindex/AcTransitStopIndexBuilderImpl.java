/*
 * @author nirmal
 */
package com.nimbler.tp.gtfs.stopindex;

import java.io.IOException;
import java.util.List;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.Transformer;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Trip;

import com.nimbler.tp.gtfs.GtfsBundle;
import com.nimbler.tp.gtfs.TripStopIndex;
import com.nimbler.tp.util.BeanUtil;
import com.nimbler.tp.util.GtfsUtils;
import com.nimbler.tp.util.TpConstants.AGENCY_TYPE;

/**
 * The Class AcTransitStopIndexBuilderImpl.
 *
 * @author nirmal
 * Not Used, in progress
 */
public class AcTransitStopIndexBuilderImpl extends TripStopIndexBuilder{

	public AcTransitStopIndexBuilderImpl() {
		agency = AGENCY_TYPE.AC_TRANSIT;
	}

	@Override
	public void buildIndex(TripStopIndex tripStopIndex, List<GtfsBundle> bundles) throws RuntimeException{
		try {
			for (GtfsBundle bundle : bundles) {
				if(bundle.getAgencyType().equals(agency)){
					GtfsRelationalDaoImpl context = GtfsUtils.getGtfsDao(bundle.getValidFile(), lstClasses);
					tripStopIndex.save(bundle.getAgencyType(), context,new Transformer() {
						@Override
						public Object transform(Object obj) {
							Trip trip = (Trip) obj;
							return GtfsUtils.getACtransitGtfsTripIdFromApiId(trip.getId().getId());
						}
					});
					BidiMap acTransitTripIdMapping = BeanUtil.getGtfsDataServiceBean().getAcTransitTripIdMapping();
					for (Trip trip : context.getAllTrips()) {
						String tripId = trip.getId().getId();
						String mappedTripId = GtfsUtils.getACtransitGtfsTripIdFromApiId(tripId);
						if(mappedTripId!=null)
							acTransitTripIdMapping.put(tripId, mappedTripId);						
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
