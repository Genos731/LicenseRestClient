
package comp9322.assignment1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the comp9322.assignment1 package. 
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

    private final static QName _ValidationFault_QNAME = new QName("http://assignment1.comp9322", "ValidationFault");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: comp9322.assignment1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link CheckAddressRequest }
     * 
     */
    public CheckAddressRequest createCheckAddressRequest() {
        return new CheckAddressRequest();
    }

    /**
     * Create an instance of {@link CheckAddressResponse }
     * 
     */
    public CheckAddressResponse createCheckAddressResponse() {
        return new CheckAddressResponse();
    }

    /**
     * Create an instance of {@link CheckEmailAddressRequest }
     * 
     */
    public CheckEmailAddressRequest createCheckEmailAddressRequest() {
        return new CheckEmailAddressRequest();
    }

    /**
     * Create an instance of {@link CheckEmailAddressResponse }
     * 
     */
    public CheckEmailAddressResponse createCheckEmailAddressResponse() {
        return new CheckEmailAddressResponse();
    }

    /**
     * Create an instance of {@link ReturnPostcodeRequest }
     * 
     */
    public ReturnPostcodeRequest createReturnPostcodeRequest() {
        return new ReturnPostcodeRequest();
    }

    /**
     * Create an instance of {@link ReturnPostcodeResponse }
     * 
     */
    public ReturnPostcodeResponse createReturnPostcodeResponse() {
        return new ReturnPostcodeResponse();
    }

    /**
     * Create an instance of {@link ServiceFaultType }
     * 
     */
    public ServiceFaultType createServiceFaultType() {
        return new ServiceFaultType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ServiceFaultType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://assignment1.comp9322", name = "ValidationFault")
    public JAXBElement<ServiceFaultType> createValidationFault(ServiceFaultType value) {
        return new JAXBElement<ServiceFaultType>(_ValidationFault_QNAME, ServiceFaultType.class, null, value);
    }

}
