package com.mykaarma.test.client.model;

import java.util.Date;

public class KaarmaEntityXML {

	private String id;
	private Long dealerID;
	private String entityKey;
	private String xml;
	private String messageID;
	private Date originDate;
	private Date updatedDate;
	
	
	public Long getDealerID() {
		return dealerID;
	}
	public void setDealerID(Long dealerID) {
		this.dealerID = dealerID;
	}
	public String getEntityKey() {
		return entityKey;
	}
	public void setEntityKey(String entityKey) {
		this.entityKey = entityKey;
	}
	public String getXml() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getMessageID() {
		return messageID;
	}
	public void setMessageID(String messageID) {
		this.messageID = messageID;
	}
	public Date getOriginDate() {
		return originDate;
	}
	public void setOriginDate(Date originDate) {
		this.originDate = originDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
}
