
package com.exlibris.dps;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "ProducerWebServices", targetNamespace = "http://dps.exlibris.com/", wsdlLocation = "https://rosetta.develop.lza.tib.eu/dpsws/deposit/ProducerWebServices?wsdl")
public class ProducerWebServices_Service
    extends Service
{

    private final static URL PRODUCERWEBSERVICES_WSDL_LOCATION;
    private final static WebServiceException PRODUCERWEBSERVICES_EXCEPTION;
    private final static QName PRODUCERWEBSERVICES_QNAME = new QName("http://dps.exlibris.com/", "ProducerWebServices");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("https://rosetta.develop.lza.tib.eu/dpsws/deposit/ProducerWebServices?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        PRODUCERWEBSERVICES_WSDL_LOCATION = url;
        PRODUCERWEBSERVICES_EXCEPTION = e;
    }

    public ProducerWebServices_Service() {
        super(__getWsdlLocation(), PRODUCERWEBSERVICES_QNAME);
    }

    public ProducerWebServices_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), PRODUCERWEBSERVICES_QNAME, features);
    }

    public ProducerWebServices_Service(URL wsdlLocation) {
        super(wsdlLocation, PRODUCERWEBSERVICES_QNAME);
    }

    public ProducerWebServices_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, PRODUCERWEBSERVICES_QNAME, features);
    }

    public ProducerWebServices_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ProducerWebServices_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * 
     * @return
     *     returns ProducerWebServices
     */
    @WebEndpoint(name = "ProducerWebServicesPort")
    public ProducerWebServices getProducerWebServicesPort() {
        return super.getPort(new QName("http://dps.exlibris.com/", "ProducerWebServicesPort"), ProducerWebServices.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns ProducerWebServices
     */
    @WebEndpoint(name = "ProducerWebServicesPort")
    public ProducerWebServices getProducerWebServicesPort(WebServiceFeature... features) {
        return super.getPort(new QName("http://dps.exlibris.com/", "ProducerWebServicesPort"), ProducerWebServices.class, features);
    }

    private static URL __getWsdlLocation() {
        if (PRODUCERWEBSERVICES_EXCEPTION!= null) {
            throw PRODUCERWEBSERVICES_EXCEPTION;
        }
        return PRODUCERWEBSERVICES_WSDL_LOCATION;
    }

}
