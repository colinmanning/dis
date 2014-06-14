
package damchalcoapi.soap.admin;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the damchalcoapi.soap.admin package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: damchalcoapi.soap.admin
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetDbSchemaResponse }
     * 
     */
    public GetDbSchemaResponse createGetDbSchemaResponse() {
        return new GetDbSchemaResponse();
    }

    /**
     * Create an instance of {@link ArrayOfResponseOfField }
     * 
     */
    public ArrayOfResponseOfField createArrayOfResponseOfField() {
        return new ArrayOfResponseOfField();
    }

    /**
     * Create an instance of {@link GetDbSchema }
     * 
     */
    public GetDbSchema createGetDbSchema() {
        return new GetDbSchema();
    }

    /**
     * Create an instance of {@link ResponseOfField }
     * 
     */
    public ResponseOfField createResponseOfField() {
        return new ResponseOfField();
    }

}
