
package com.nimbler.tp.dataobject;

import java.io.Serializable;

public class AgencyAndId
implements Serializable, Comparable
{

	public AgencyAndId()
	{
	}

	public AgencyAndId(String agencyId, String id)
	{
		this.agencyId = agencyId;
		this.id = id;
	}

	public String getAgencyId()
	{
		return agencyId;
	}

	public void setAgencyId(String agencyId)
	{
		this.agencyId = agencyId;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public boolean hasValues()
	{
		return agencyId != null && id != null;
	}

	public int compareTo(AgencyAndId o)
	{
		int c = agencyId.compareTo(o.agencyId);
		if(c == 0)
			c = id.compareTo(o.id);
		return c;
	}

	public static AgencyAndId convertFromString(String value, char separator)
	{
		int index = value.indexOf(separator);
		if(index == -1)
			throw new IllegalStateException((new StringBuilder()).append("invalid agency-and-id: ").append(value).toString());
		else
			return new AgencyAndId(value.substring(0, index), value.substring(index + 1));
	}

	public int hashCode()
	{
		return agencyId.hashCode() ^ id.hashCode();
	}

	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(!(obj instanceof AgencyAndId))
			return false;
		AgencyAndId other = (AgencyAndId)obj;
		if(!agencyId.equals(other.agencyId))
			return false;
		return id.equals(other.id);
	}

	public String toString()
	{
		return convertToString(this);
	}

	public static AgencyAndId convertFromString(String value)
			throws IllegalArgumentException
			{
		if(value == null || value.isEmpty())
			return null;
		int index = value.indexOf('_');
		if(index == -1)
			throw new IllegalArgumentException((new StringBuilder()).append("invalid agency-and-id: ").append(value).toString());
		else
			return new AgencyAndId(value.substring(0, index), value.substring(index + 1));
			}

	public static String convertToString(AgencyAndId aid)
	{
		if(aid == null)
			return null;
		else
			return (new StringBuilder()).append(aid.getAgencyId()).append('_').append(aid.getId()).toString();
	}

	public static final char ID_SEPARATOR = 95;
	private static final long serialVersionUID = 1L;
	private String agencyId;
	private String id;
	@Override
	public int compareTo(Object o) {
		return 0;
	}
}


