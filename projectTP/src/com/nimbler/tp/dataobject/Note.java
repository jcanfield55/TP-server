/*
 * 
 */
package com.nimbler.tp.dataobject;

import javax.xml.bind.annotation.XmlElement;

public class Note {
	@XmlElement
	public String text;

	public Note() {
		/* Required by JAXB but unused */
	}

	public Note(String note) {
		text = note;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Note other = (Note) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}


}
