
package damchalcoapi.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfResponseOfAsset complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfResponseOfAsset">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseOfAsset" type="{http://tempuri.org/}ResponseOfAsset" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfResponseOfAsset", propOrder = {
    "responseOfAsset"
})
public class ArrayOfResponseOfAsset {

    @XmlElement(name = "ResponseOfAsset", nillable = true)
    protected List<ResponseOfAsset> responseOfAsset;

    /**
     * Gets the value of the responseOfAsset property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the responseOfAsset property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponseOfAsset().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResponseOfAsset }
     * 
     * 
     */
    public List<ResponseOfAsset> getResponseOfAsset() {
        if (responseOfAsset == null) {
            responseOfAsset = new ArrayList<ResponseOfAsset>();
        }
        return this.responseOfAsset;
    }

}
