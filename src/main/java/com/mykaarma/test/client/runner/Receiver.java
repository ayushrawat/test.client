/**
 * 
 */
package com.mykaarma.test.client.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kaarya.utils.XMLHandler;
import com.mykaarma.test.client.config.MongoCollections;
import com.mykaarma.test.client.model.KaarmaEntityXML;
import com.mykaarma.test.client.model.MQEntity;


/**
 * @author root
 *
 */
@Component
public class Receiver {
	
	@Autowired
	private MongoTemplate mongoTemplate;

	private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

	public void receiveMessage(String message) {
		try {
			ObjectMapper mapper =  new ObjectMapper();
			MQEntity mqEntity = mapper.readValue(message, MQEntity.class);
			LOGGER.info(mqEntity.getMessageID()+" Querying MongoDB now...");
			Query query = new Query(Criteria.where("entityKey").is(mqEntity.getEntityID()).and("dealerID").is(mqEntity.getDealerID()));
			KaarmaEntityXML kaarmaEntityXML = mongoTemplate.findOne(query, KaarmaEntityXML.class, MongoCollections.KAARMADEALERORDER.name());
			
			if(kaarmaEntityXML == null)
			{
				LOGGER.error("TEST_CONTROLLER_DATA_MISMATCH dealer_id="+mqEntity.getDealerID()+" ro_number="+mqEntity.getEntityID()+" message_id_received="+mqEntity.getMessageID()+" message_id_inxml=null");
				return;
			}
			Document dom = XMLHandler.xmlString2Dom(kaarmaEntityXML.getXml());
			if(validateCheckSum(mqEntity.getMessageID(), kaarmaEntityXML.getXml(), mqEntity.getEntityID(), mqEntity.getDealerID(), dom))
			{
				LOGGER.info(mqEntity.getMessageID()+"success!");
			}
		} catch (Exception e) {
			LOGGER.error("TEST_CONTROLLER_DATA_MISMATCH ERROR", e);
		}
	}
	
	private boolean validateCheckSum(String messageId, String xml, String entityID, Long dealerID, Document dom) throws Exception {
		try {
			String checkSumString = com.mykaarma.test.client.runner.XmlProcessorUtil.getValueForXpath("/RepairOrderWrap/CheckSum[1]", dom);
			
			if(!messageId.equalsIgnoreCase(checkSumString)) {
				LOGGER.error("TEST_CONTROLLER_DATA_MISMATCH dealer_id="+dealerID+" ro_number="+entityID+" message_id_received="+messageId+" message_id_inxml="+checkSumString);
				return false;
			}
		} catch (Exception e) {
			LOGGER.error("ERROR while matching checksum string for ro_number="+entityID+" dealerid="+dealerID, e);
			throw e;
		}
		return true;
	}
	
}
