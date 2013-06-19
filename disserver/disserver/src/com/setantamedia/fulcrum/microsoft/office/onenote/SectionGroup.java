//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.04.26 at 07:40:13 PM MESZ 
//


package com.setantamedia.fulcrum.microsoft.office.onenote;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 * 			
 * 
 * <p>Java class for SectionGroup complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SectionGroup">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Section" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}Section" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="SectionGroup" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}SectionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://schemas.microsoft.com/office/onenote/2010/onenote}HierarchyObjectAttributes"/>
 *       &lt;attGroup ref="{http://schemas.microsoft.com/office/onenote/2010/onenote}FileObjectAttributes"/>
 *       &lt;attribute name="isUnread" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="isRecycleBin" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SectionGroup", propOrder = {
    "section",
    "sectionGroup"
})
public class SectionGroup {

    @XmlElement(name = "Section")
    protected List<Section> section;
    @XmlElement(name = "SectionGroup")
    protected List<SectionGroup> sectionGroup;
    @XmlAttribute
    protected Boolean isUnread;
    @XmlAttribute
    protected Boolean isRecycleBin;
    @XmlAttribute(name = "ID")
    protected String id;
    @XmlAttribute
    protected String name;
    @XmlAttribute
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar lastModifiedTime;
    @XmlAttribute
    protected Boolean isCurrentlyViewed;
    @XmlAttribute
    protected Boolean isInRecycleBin;
    @XmlAttribute
    protected String path;

    /**
     * Gets the value of the section property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the section property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Section }
     * 
     * 
     */
    public List<Section> getSection() {
        if (section == null) {
            section = new ArrayList<Section>();
        }
        return this.section;
    }

    /**
     * Gets the value of the sectionGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the sectionGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSectionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SectionGroup }
     * 
     * 
     */
    public List<SectionGroup> getSectionGroup() {
        if (sectionGroup == null) {
            sectionGroup = new ArrayList<SectionGroup>();
        }
        return this.sectionGroup;
    }

    /**
     * Gets the value of the isUnread property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsUnread() {
        return isUnread;
    }

    /**
     * Sets the value of the isUnread property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsUnread(Boolean value) {
        this.isUnread = value;
    }

    /**
     * Gets the value of the isRecycleBin property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsRecycleBin() {
        if (isRecycleBin == null) {
            return false;
        } else {
            return isRecycleBin;
        }
    }

    /**
     * Sets the value of the isRecycleBin property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsRecycleBin(Boolean value) {
        this.isRecycleBin = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setID(String value) {
        this.id = value;
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
     * Gets the value of the lastModifiedTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the value of the lastModifiedTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLastModifiedTime(XMLGregorianCalendar value) {
        this.lastModifiedTime = value;
    }

    /**
     * Gets the value of the isCurrentlyViewed property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsCurrentlyViewed() {
        if (isCurrentlyViewed == null) {
            return false;
        } else {
            return isCurrentlyViewed;
        }
    }

    /**
     * Sets the value of the isCurrentlyViewed property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsCurrentlyViewed(Boolean value) {
        this.isCurrentlyViewed = value;
    }

    /**
     * Gets the value of the isInRecycleBin property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsInRecycleBin() {
        if (isInRecycleBin == null) {
            return false;
        } else {
            return isInRecycleBin;
        }
    }

    /**
     * Sets the value of the isInRecycleBin property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsInRecycleBin(Boolean value) {
        this.isInRecycleBin = value;
    }

    /**
     * Gets the value of the path property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPath() {
        return path;
    }

    /**
     * Sets the value of the path property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPath(String value) {
        this.path = value;
    }

}