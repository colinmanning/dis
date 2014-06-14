
package damchalcoapi.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TextSearchResult" type="{http://tempuri.org/}ArrayOfResponseOfAsset" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "textSearchResult"
})
@XmlRootElement(name = "TextSearchResponse")
public class TextSearchResponse {

    @XmlElement(name = "TextSearchResult")
    protected ArrayOfResponseOfAsset textSearchResult;

    /**
     * Gets the value of the textSearchResult property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfResponseOfAsset }
     *     
     */
    public ArrayOfResponseOfAsset getTextSearchResult() {
        return textSearchResult;
    }

    /**
     * Sets the value of the textSearchResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfResponseOfAsset }
     *     
     */
    public void setTextSearchResult(ArrayOfResponseOfAsset value) {
        this.textSearchResult = value;
    }

}
