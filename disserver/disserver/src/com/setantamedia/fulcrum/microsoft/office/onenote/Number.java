//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.04.26 at 07:40:13 PM MESZ 
//


package com.setantamedia.fulcrum.microsoft.office.onenote;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * 			
 * 
 * <p>Java class for Number complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Number">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="numberSequence" use="required" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="numberFormat" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="restartNumberingAt" type="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger" />
 *       &lt;attribute name="font" type="{http://www.w3.org/2001/XMLSchema}string" default="Verdana" />
 *       &lt;attribute name="fontSize" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}FontSize" default="10.0" />
 *       &lt;attribute name="fontColor" type="{http://schemas.microsoft.com/office/onenote/2010/onenote}COLOR" default="automatic" />
 *       &lt;attribute name="bold" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="italic" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="language" type="{http://www.w3.org/2001/XMLSchema}string" default="1033" />
 *       &lt;attribute name="text" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Number")
public class Number {

    @XmlAttribute(required = true)
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger numberSequence;
    @XmlAttribute(required = true)
    protected String numberFormat;
    @XmlAttribute
    @XmlSchemaType(name = "nonNegativeInteger")
    protected BigInteger restartNumberingAt;
    @XmlAttribute
    protected String font;
    @XmlAttribute
    protected Double fontSize;
    @XmlAttribute
    protected String fontColor;
    @XmlAttribute
    protected Boolean bold;
    @XmlAttribute
    protected Boolean italic;
    @XmlAttribute
    protected String language;
    @XmlAttribute
    protected String text;

    /**
     * Gets the value of the numberSequence property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getNumberSequence() {
        return numberSequence;
    }

    /**
     * Sets the value of the numberSequence property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setNumberSequence(BigInteger value) {
        this.numberSequence = value;
    }

    /**
     * Gets the value of the numberFormat property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumberFormat() {
        return numberFormat;
    }

    /**
     * Sets the value of the numberFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumberFormat(String value) {
        this.numberFormat = value;
    }

    /**
     * Gets the value of the restartNumberingAt property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRestartNumberingAt() {
        return restartNumberingAt;
    }

    /**
     * Sets the value of the restartNumberingAt property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRestartNumberingAt(BigInteger value) {
        this.restartNumberingAt = value;
    }

    /**
     * Gets the value of the font property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFont() {
        if (font == null) {
            return "Verdana";
        } else {
            return font;
        }
    }

    /**
     * Sets the value of the font property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFont(String value) {
        this.font = value;
    }

    /**
     * Gets the value of the fontSize property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public double getFontSize() {
        if (fontSize == null) {
            return  10.0D;
        } else {
            return fontSize;
        }
    }

    /**
     * Sets the value of the fontSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setFontSize(Double value) {
        this.fontSize = value;
    }

    /**
     * Gets the value of the fontColor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFontColor() {
        if (fontColor == null) {
            return "automatic";
        } else {
            return fontColor;
        }
    }

    /**
     * Sets the value of the fontColor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFontColor(String value) {
        this.fontColor = value;
    }

    /**
     * Gets the value of the bold property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isBold() {
        if (bold == null) {
            return false;
        } else {
            return bold;
        }
    }

    /**
     * Sets the value of the bold property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBold(Boolean value) {
        this.bold = value;
    }

    /**
     * Gets the value of the italic property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isItalic() {
        if (italic == null) {
            return false;
        } else {
            return italic;
        }
    }

    /**
     * Sets the value of the italic property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setItalic(Boolean value) {
        this.italic = value;
    }

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLanguage() {
        if (language == null) {
            return "1033";
        } else {
            return language;
        }
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLanguage(String value) {
        this.language = value;
    }

    /**
     * Gets the value of the text property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the value of the text property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setText(String value) {
        this.text = value;
    }

}