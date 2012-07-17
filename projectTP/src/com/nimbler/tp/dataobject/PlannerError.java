package com.nimbler.tp.dataobject;

import java.util.List;

/**
 * This represents an error in trip planning.
 *
 */
public class PlannerError {

	private int    id;
	private String msg;

	private List<String> missing = null;
	private boolean noPath = false;

	/** An error where no path has been found, but no points are missing */
	public PlannerError() {
		noPath = true;
	}

	public PlannerError(boolean np) {
		noPath = np;
	}

	//	public PlannerError(Message msg) {
	//		setMsg(msg);
	//	}

	public PlannerError(List<String> missing) {
		this.setMissing(missing);
	}

	public PlannerError(int id, String msg) {
		this.id  = id;
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	//	public void setMsg(Message msg) {
	//		this.msg = msg.get();
	//		this.id  = msg.getId();
	//	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param missing the list of point names which cannot be found (from, to, intermediate.n)
	 */
	public void setMissing(List<String> missing) {
		this.missing = missing;
	}

	/**
	 * @return the list of point names which cannot be found (from, to, intermediate.n)
	 */
	public List<String> getMissing() {
		return missing;
	}

	/**
	 * @param noPath whether no path has been found
	 */
	public void setNoPath(boolean noPath) {
		this.noPath = noPath;
	}

	/**
	 * @return whether no path has been found
	 */
	public boolean getNoPath() {
		return noPath;
	}

	@Override
	public String toString() {
		return "PlannerError [id=" + id + ", msg=" + msg + ", missing="
				+ missing + ", noPath=" + noPath + "]";
	}
}
