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

	public boolean equals(Object o) {
		return (o instanceof Note) && ((Note) o).text.equals(text);
	}

	public int hashCode() {
		return text.hashCode();
	}
}
