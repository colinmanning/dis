package damchalcoapi.test;

import damchalcoapi.soap.ArrayOfResponseOfAsset;
import damchalcoapi.soap.Asset;
import damchalcoapi.soap.AssetSoap;
import damchalcoapi.soap.ResponseOfAsset;

import java.util.List;

/**
 * Created by colin on 30/05/14.
 */
public class ChalcoTest {

    public final static String ENDPOINT_ADDRESS_PRODUCTINFO = "http://tablet.chalco.net/MediaMarketWSMobile/ProductInfo.asmx";

    public ChalcoTest() {
    }

    public void testTextSearch(String query) {
        try {
            AssetSoap service = new Asset().getAssetSoap();

            ArrayOfResponseOfAsset results = service.textSearch(query, 0, 5);
            //BindingProvider bp = (BindingProvider) service.getProductInfoSoap();
            //bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_ADDRESS_PRODUCTINFO);

            List<ResponseOfAsset> l = results.getResponseOfAsset();
            for (ResponseOfAsset asset:l) {
                System.out.println(asset.getName());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ChalcoTest m = new ChalcoTest();

        //String query = "name:\"016052.jpg\"";
        String query = "name:016";

        m.testTextSearch(query);
        //m.testGetProduct("542221");

    }
}
