package com.nimbler.tp.dataobject.wmata;

import java.io.Serializable;
import java.util.List;
/**
 * 
 * @author nirmal
 *
 */
public class WmataRoutes implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7945629786667862774L;
	List<WmataRoute> Routes;

	public List<WmataRoute> getRoutes() {
		return Routes;
	}


	public void setRoutes(List<WmataRoute> routes) {
		Routes = routes;
	}
	public static class WmataRoute implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1565912442601661845L;
		private String Name;
		private String RouteID;
		public String getName() {
			return Name;
		}
		public void setName(String name) {
			Name = name;
		}
		public String getRouteID() {
			return RouteID;
		}
		public void setRouteID(String routeID) {
			RouteID = routeID;
		}
	}
}
