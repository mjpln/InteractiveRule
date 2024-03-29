package com.knowology.km.webServiceClient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for createQueueResponse complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="createQueueResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://service.DQ.knowology.com/}msgQueue" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createQueueResponse", propOrder = { "_return" })
public class CreateQueueResponse {

	@XmlElement(name = "return")
	protected MsgQueue _return;

	/**
	 * Gets the value of the return property.
	 * 
	 * @return possible object is {@link MsgQueue }
	 * 
	 */
	public MsgQueue getReturn() {
		return _return;
	}

	/**
	 * Sets the value of the return property.
	 * 
	 * @param value
	 *            allowed object is {@link MsgQueue }
	 * 
	 */
	public void setReturn(MsgQueue value) {
		this._return = value;
	}

}
