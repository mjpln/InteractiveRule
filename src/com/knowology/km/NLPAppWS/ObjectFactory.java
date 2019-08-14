package com.knowology.km.NLPAppWS;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the com.knowology.km.NLPAppWS package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

	private final static QName _AnalyzeResponse_QNAME = new QName(
			"http://knowology.com/", "AnalyzeResponse");
	private final static QName _FormatAnaluzeResponse_QNAME = new QName(
			"http://knowology.com/", "FormatAnaluzeResponse");
	private final static QName _ReloadScenariosResponse_QNAME = new QName(
			"http://knowology.com/", "reloadScenariosResponse");
	private final static QName _UpdateProcessController_QNAME = new QName(
			"http://knowology.com/", "UpdateProcessController");
	private final static QName _UpdateProcessControllerResponse_QNAME = new QName(
			"http://knowology.com/", "UpdateProcessControllerResponse");
	private final static QName _FormatAnaluze_QNAME = new QName(
			"http://knowology.com/", "FormatAnaluze");
	private final static QName _AddServiceInc_QNAME = new QName(
			"http://knowology.com/", "addServiceInc");
	private final static QName _ReloadScenarios_QNAME = new QName(
			"http://knowology.com/", "reloadScenarios");
	private final static QName _AddServiceIncResponse_QNAME = new QName(
			"http://knowology.com/", "addServiceIncResponse");
	private final static QName _Analyze_QNAME = new QName(
			"http://knowology.com/", "Analyze");

	/**
	 * Create a new ObjectFactory that can be used to create new instances of
	 * schema derived classes for package: com.knowology.km.NLPAppWS
	 * 
	 */
	public ObjectFactory() {
	}

	/**
	 * Create an instance of {@link AddServiceIncResponse }
	 * 
	 */
	public AddServiceIncResponse createAddServiceIncResponse() {
		return new AddServiceIncResponse();
	}

	/**
	 * Create an instance of {@link ReloadScenariosResponse }
	 * 
	 */
	public ReloadScenariosResponse createReloadScenariosResponse() {
		return new ReloadScenariosResponse();
	}

	/**
	 * Create an instance of {@link FormatAnaluzeResponse }
	 * 
	 */
	public FormatAnaluzeResponse createFormatAnaluzeResponse() {
		return new FormatAnaluzeResponse();
	}

	/**
	 * Create an instance of {@link ReloadScenarios }
	 * 
	 */
	public ReloadScenarios createReloadScenarios() {
		return new ReloadScenarios();
	}

	/**
	 * Create an instance of {@link AnalyzeResponse }
	 * 
	 */
	public AnalyzeResponse createAnalyzeResponse() {
		return new AnalyzeResponse();
	}

	/**
	 * Create an instance of {@link FormatAnaluze }
	 * 
	 */
	public FormatAnaluze createFormatAnaluze() {
		return new FormatAnaluze();
	}

	/**
	 * Create an instance of {@link AddServiceInc }
	 * 
	 */
	public AddServiceInc createAddServiceInc() {
		return new AddServiceInc();
	}

	/**
	 * Create an instance of {@link UpdateProcessController }
	 * 
	 */
	public UpdateProcessController createUpdateProcessController() {
		return new UpdateProcessController();
	}

	/**
	 * Create an instance of {@link Analyze }
	 * 
	 */
	public Analyze createAnalyze() {
		return new Analyze();
	}

	/**
	 * Create an instance of {@link UpdateProcessControllerResponse }
	 * 
	 */
	public UpdateProcessControllerResponse createUpdateProcessControllerResponse() {
		return new UpdateProcessControllerResponse();
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link AnalyzeResponse }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "AnalyzeResponse")
	public JAXBElement<AnalyzeResponse> createAnalyzeResponse(
			AnalyzeResponse value) {
		return new JAXBElement<AnalyzeResponse>(_AnalyzeResponse_QNAME,
				AnalyzeResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link FormatAnaluzeResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "FormatAnaluzeResponse")
	public JAXBElement<FormatAnaluzeResponse> createFormatAnaluzeResponse(
			FormatAnaluzeResponse value) {
		return new JAXBElement<FormatAnaluzeResponse>(
				_FormatAnaluzeResponse_QNAME, FormatAnaluzeResponse.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link ReloadScenariosResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "reloadScenariosResponse")
	public JAXBElement<ReloadScenariosResponse> createReloadScenariosResponse(
			ReloadScenariosResponse value) {
		return new JAXBElement<ReloadScenariosResponse>(
				_ReloadScenariosResponse_QNAME, ReloadScenariosResponse.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link UpdateProcessController }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "UpdateProcessController")
	public JAXBElement<UpdateProcessController> createUpdateProcessController(
			UpdateProcessController value) {
		return new JAXBElement<UpdateProcessController>(
				_UpdateProcessController_QNAME, UpdateProcessController.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link UpdateProcessControllerResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "UpdateProcessControllerResponse")
	public JAXBElement<UpdateProcessControllerResponse> createUpdateProcessControllerResponse(
			UpdateProcessControllerResponse value) {
		return new JAXBElement<UpdateProcessControllerResponse>(
				_UpdateProcessControllerResponse_QNAME,
				UpdateProcessControllerResponse.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link FormatAnaluze }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "FormatAnaluze")
	public JAXBElement<FormatAnaluze> createFormatAnaluze(FormatAnaluze value) {
		return new JAXBElement<FormatAnaluze>(_FormatAnaluze_QNAME,
				FormatAnaluze.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link AddServiceInc }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "addServiceInc")
	public JAXBElement<AddServiceInc> createAddServiceInc(AddServiceInc value) {
		return new JAXBElement<AddServiceInc>(_AddServiceInc_QNAME,
				AddServiceInc.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link ReloadScenarios }
	 * {@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "reloadScenarios")
	public JAXBElement<ReloadScenarios> createReloadScenarios(
			ReloadScenarios value) {
		return new JAXBElement<ReloadScenarios>(_ReloadScenarios_QNAME,
				ReloadScenarios.class, null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}
	 * {@link AddServiceIncResponse }{@code >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "addServiceIncResponse")
	public JAXBElement<AddServiceIncResponse> createAddServiceIncResponse(
			AddServiceIncResponse value) {
		return new JAXBElement<AddServiceIncResponse>(
				_AddServiceIncResponse_QNAME, AddServiceIncResponse.class,
				null, value);
	}

	/**
	 * Create an instance of {@link JAXBElement }{@code <}{@link Analyze }{@code
	 * >}
	 * 
	 */
	@XmlElementDecl(namespace = "http://knowology.com/", name = "Analyze")
	public JAXBElement<Analyze> createAnalyze(Analyze value) {
		return new JAXBElement<Analyze>(_Analyze_QNAME, Analyze.class, null,
				value);
	}

}
