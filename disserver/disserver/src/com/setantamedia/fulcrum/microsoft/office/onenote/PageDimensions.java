//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.04.26 at 07:40:13 PM MESZ 
//


package com.setantamedia.fulcrum.microsoft.office.onenote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			
 * 
 * <p>Java class for PageDimensions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PageDimensions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="height" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}PositiveDecimal" default="792" />
 *       &lt;attribute name="width" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}PositiveDecimal" default="612" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PageDimensions")
public class PageDimensions {

    @XmlAttribute
    protected Double height;
    @XmlAttribute
    protected Double width;

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getHeight() {
        if (height == null) {
            return  792.0D;
        } else {
            return height;
        }
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setHeight(Double value) {
        this.height = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getWidth() {
        if (width == null) {
            return  612.0D;
        } else {
            return width;
        }
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setWidth(Double value) {
        this.width = value;
    }

}