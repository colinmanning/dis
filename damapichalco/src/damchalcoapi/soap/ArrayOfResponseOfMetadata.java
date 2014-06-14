
package damchalcoapi.soap;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ArrayOfResponseOfMetadata complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ArrayOfResponseOfMetadata">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ResponseOfMetadata" type="{http://tempuri.org/}ResponseOfMetadata" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfResponseOfMetadata", propOrder = {
    "responseOfMetadata"
})
public class ArrayOfResponseOfMetadata {

    @XmlElement(name = "ResponseOfMetadata", nillable = true)
    protected List<ResponseOfMetadata> responseOfMetadata;

    /**
     * Gets the value of the responseOfMetadata property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the responseOfMetadata property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResponseOfMetadata().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResponseOfMetadata }
     * 
     * 
     */
    public List<ResponseOfMetadata> getResponseOfMetadata() {
        if (responseOfMetadata == null) {
            responseOfMetadata = new ArrayList<ResponseOfMetadata>();
        }
        return this.responseOfMetadata;
    }

}
