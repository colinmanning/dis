//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.03.29 at 12:41:31 PM CET 
//


package com.setantamedia.fulcrum.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for servlet complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="servlet">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="servletClass" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="params" type="{}paramList" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="contextPath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "servlet", propOrder = {
    "servletClass",
    "params"
})
public class Servlet {

    @XmlElement(required = true)
    protected String servletClass;
    protected ParamList params;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "contextPath")
    protected String contextPath;

    /**
     * Gets the value of the servletClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getServletClass() {
        return servletClass;
    }

    /**
     * Sets the value of the servletClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setServletClass(String value) {
        this.servletClass = value;
    }

    /**
     * Gets the value of the params property.
     * 
     * @return
     *     possible object is
     *     {@link ParamList }
     *     
     */
    public ParamList getParams() {
        return params;
    }

    /**
     * Sets the value of the params property.
     * 
     * @param value
     *     allowed object is
     *     {@link ParamList }
     *     
     */
    public void setParams(ParamList value) {
        this.params = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the contextPath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the value of the contextPath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setContextPath(String value) {
        this.contextPath = value;
    }

}
