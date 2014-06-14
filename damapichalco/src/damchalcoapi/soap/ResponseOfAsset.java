
package damchalcoapi.soap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for ResponseOfAsset complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ResponseOfAsset">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="Metadata" type="{http://tempuri.org/}ArrayOfResponseOfMetadata" minOccurs="0"/>
 *         &lt;element name="Info" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ID" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="Length" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="InsertionDate" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="FileType" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResponseOfAsset", propOrder = {
    "name",
    "metadata",
    "info",
    "id",
    "length",
    "insertionDate",
    "fileType"
})
public class ResponseOfAsset {

    @XmlElement(name = "Name")
    protected String name;
    @XmlElement(name = "Metadata")
    protected ArrayOfResponseOfMetadata metadata;
    @XmlElement(name = "Info")
    protected String info;
    @XmlElement(name = "ID")
    protected int id;
    @XmlElement(name = "Length")
    protected long length;
    @XmlElement(name = "InsertionDate", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar insertionDate;
    @XmlElement(name = "FileType")
    protected int fileType;

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
     * Gets the value of the metadata property.
     * 
     * @return
     *     possible object is
     *     {@link ArrayOfResponseOfMetadata }
     *     
     */
    public ArrayOfResponseOfMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the value of the metadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArrayOfResponseOfMetadata }
     *     
     */
    public void setMetadata(ArrayOfResponseOfMetadata value) {
        this.metadata = value;
    }

    /**
     * Gets the value of the info property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInfo() {
        return info;
    }

    /**
     * Sets the value of the info property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInfo(String value) {
        this.info = value;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public int getID() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setID(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the length property.
     * 
     */
    public long getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     */
    public void setLength(long value) {
        this.length = value;
    }

    /**
     * Gets the value of the insertionDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getInsertionDate() {
        return insertionDate;
    }

    /**
     * Sets the value of the insertionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setInsertionDate(XMLGregorianCalendar value) {
        this.insertionDate = value;
    }

    /**
     * Gets the value of the fileType property.
     * 
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * Sets the value of the fileType property.
     * 
     */
    public void setFileType(int value) {
        this.fileType = value;
    }

}
