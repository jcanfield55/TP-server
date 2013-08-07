package com.nimbler.tp.dataobject.my511;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name="foo1" )
@XmlAccessorType(XmlAccessType.FIELD)
public class My511DepartureTimes<T> {
	@XmlElementWrapper( name="answerList" )
	@XmlElement( name="answer" )
	List<T> answerList = new ArrayList<T>();
}
