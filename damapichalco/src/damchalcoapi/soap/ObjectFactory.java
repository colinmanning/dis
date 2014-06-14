
package damchalcoapi.soap;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the damchalcoapi.soap package. 
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
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: damchalcoapi.soap
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PreviewResponse }
     * 
     */
    public PreviewResponse createPreviewResponse() {
        return new PreviewResponse();
    }

    /**
     * Create an instance of {@link TextSearchResponse }
     * 
     */
    public TextSearchResponse createTextSearchResponse() {
        return new TextSearchResponse();
    }

    /**
     * Create an instance of {@link ArrayOfResponseOfAsset }
     * 
     */
    public ArrayOfResponseOfAsset createArrayOfResponseOfAsset() {
        return new ArrayOfResponseOfAsset();
    }

    /**
     * Create an instance of {@link Preview }
     * 
     */
    public Preview createPreview() {
        return new Preview();
    }

    /**
     * Create an instance of {@link TextSearch }
     * 
     */
    public TextSearch createTextSearch() {
        return new TextSearch();
    }

    /**
     * Create an instance of {@link DownloadResponse }
     * 
     */
    public DownloadResponse createDownloadResponse() {
        return new DownloadResponse();
    }

    /**
     * Create an instance of {@link Download }
     * 
     */
    public Download createDownload() {
        return new Download();
    }

    /**
     * Create an instance of {@link ArrayOfResponseOfMetadata }
     * 
     */
    public ArrayOfResponseOfMetadata createArrayOfResponseOfMetadata() {
        return new ArrayOfResponseOfMetadata();
    }

    /**
     * Create an instance of {@link ResponseOfAsset }
     * 
     */
    public ResponseOfAsset createResponseOfAsset() {
        return new ResponseOfAsset();
    }

    /**
     * Create an instance of {@link ResponseOfMetadata }
     * 
     */
    public ResponseOfMetadata createResponseOfMetadata() {
        return new ResponseOfMetadata();
    }

}
