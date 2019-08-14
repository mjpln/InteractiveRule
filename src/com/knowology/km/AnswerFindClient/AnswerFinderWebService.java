package com.knowology.km.AnswerFindClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.1.3-hudson-390-
 * Generated source version: 2.0
 * <p>
 * An example of how this class may be used:
 * 
 * <pre>
 * AnswerFinderWebService service = new AnswerFinderWebService();
 * AnswerFinderServiceDelegate portType = service.getAnswerFinderService();
 * portType.findAnswer(...);
 * </pre>
 * 
 * </p>
 * 
 */
@WebServiceClient(name = "AnswerFinderWebService", targetNamespace = "http://Services.AnswerFinderWebService.knowology.com/", wsdlLocation = "http://222.186.101.213:7080/AnswerFinderWebService/AnswerFinderService?wsdl")
public class AnswerFinderWebService extends Service {

	private final static URL ANSWERFINDERWEBSERVICE_WSDL_LOCATION;
	private final static Logger logger = Logger
			.getLogger(com.knowology.km.AnswerFindClient.AnswerFinderWebService.class
					.getName());

	static {
		URL url = null;
		try {
			URL baseUrl;
			baseUrl = com.knowology.km.AnswerFindClient.AnswerFinderWebService.class
					.getResource(".");
			url = new URL(baseUrl,
					"http://222.186.101.213:7080/AnswerFinderWebService/AnswerFinderService?wsdl");
		} catch (MalformedURLException e) {
			logger
					.warning("Failed to create URL for the wsdl Location: 'http://222.186.101.213:7080/AnswerFinderWebService/AnswerFinderService?wsdl', retrying as a local file");
			logger.warning(e.getMessage());
		}
		ANSWERFINDERWEBSERVICE_WSDL_LOCATION = url;
	}

	public AnswerFinderWebService(URL wsdlLocation, QName serviceName) {
		super(wsdlLocation, serviceName);
	}

	public AnswerFinderWebService() {
		super(ANSWERFINDERWEBSERVICE_WSDL_LOCATION, new QName(
				"http://Services.AnswerFinderWebService.knowology.com/",
				"AnswerFinderWebService"));
	}

	/**
	 * 
	 * @return returns AnswerFinderServiceDelegate
	 */
	@WebEndpoint(name = "AnswerFinderService")
	public AnswerFinderServiceDelegate getAnswerFinderService() {
		return super.getPort(new QName(
				"http://Services.AnswerFinderWebService.knowology.com/",
				"AnswerFinderService"), AnswerFinderServiceDelegate.class);
	}

}
