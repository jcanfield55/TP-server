//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.07.16 at 02:47:42 PM IST 
//


package com.nimbler.tp.dataobject.trimet.arrivals;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for layoverType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="layoverType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="location" type="{urn:trimet:arrivals}locationType"/>
 *       &lt;/sequence>
 *       &lt;attribute name="start" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *       &lt;attribute name="end" use="required" type="{http://www.w3.org/2001/XMLSchema}long" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "layoverType", propOrder = {
    "location"
})
public class LayoverType {

    @XmlElement(required = true)
    protected LocationType location;
    @XmlAttribute(required = true)
    protected long start;
    @XmlAttribute(required = true)
    protected long end;

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link LocationType }
     *     
     */
    public LocationType getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationType }
     *     
     */
    public void setLocation(LocationType value) {
        this.location = value;
    }

    /**
     * Gets the value of the start property.
     * 
     */
    public long getStart() {
        return start;
    }

    /**
     * Sets the value of the start property.
     * 
     */
    public void setStart(long value) {
        this.start = value;
    }

    /**
     * Gets the value of the end property.
     * 
     */
    public long getEnd() {
        return end;
    }

    /**
     * Sets the value of the end property.
     * 
     */
    public void setEnd(long value) {
        this.end = value;
    }

}
