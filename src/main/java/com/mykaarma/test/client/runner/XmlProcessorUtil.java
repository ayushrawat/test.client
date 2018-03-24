package com.mykaarma.test.client.runner;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kaarya.utils.XMLHandler;

public class XmlProcessorUtil {
	
	 private static ObjectPool<XPath> pool = new GenericObjectPool<XPath>(new XPathPoolFactory());
	 private static Logger LOGGER = Logger.getLogger(XmlProcessorUtil.class); 
	
	/**
	 * Returns Transformed Xml after applying provided xslt and xml using provided parameters
	 * 
	 * @param xsltPath
	 * @param xml
	 * @param params
	 * @return {@link String} transformedXML
	 */
	public static String transformXmlUsingXsltWithParams(String xsltPath, String xml, HashMap<String, String> params)
	{
		StringReader reader = new StringReader(xml);
		Source xmlSource = new StreamSource(reader);
		return transformXmlUsingXsltWithParams(xsltPath, xmlSource, params);
	}
	
	public static String transformXmlUsingXsltWithParams(String xsltPath, Document xml, HashMap<String, String> params)
	{
		Source xmlSource = new DOMSource(xml);
		return transformXmlUsingXsltWithParams(xsltPath, xmlSource, params);
	}
	
	private static String transformXmlUsingXsltWithParams(String xsltPath, Source xml, HashMap<String, String> params)
	{
		String result = "";
		StringWriter writer = new StringWriter();
		Source xslt = null;
		if(xsltPath.contains("http"))
		{
			InputStream in = null;
			try {
				in = new URL(xsltPath).openStream();
				xslt = new StreamSource(in);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			xslt = new StreamSource(new File(xsltPath));
		
		if(xslt == null)
			return null;
		
		Transformer transformer = null;
		
		try
		{
			transformer = TransformerFactory.newInstance().newTransformer(xslt);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		
		if(params != null && !params.isEmpty())
		{
			for(String key : params.keySet())
			{
				if(transformer != null)
					transformer.setParameter(key, params.get(key));
			}
		}
		try
		{
			transformer.transform(xml, new StreamResult(writer));
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
			return null;
		}
		result = writer.toString();
		return result;
	}
	
	 public static void setValueForXpath(String path,Node node,String value) throws Exception
		{
		    XPath xpath = null;
			try {
				xpath = pool.borrowObject();
			} catch (Exception e) {
				LOGGER.error("Unable to borrow object from xpath pool.", e);
				throw e;
			}
			XPathExpression expr = null;
			try {
				expr = xpath.compile(path);
				Node returnNode =(Node) expr.evaluate(node,XPathConstants.NODE);
				if(returnNode!=null)
					returnNode.setTextContent(value!=null?value:"");
			} catch (Exception e) {
				LOGGER.error("This object has failed us. Discard it.", e);
				pool.invalidateObject(xpath);
				xpath=null;
				throw e;
			}
			pool.returnObject(xpath);
			
		}
	 
	 public static void setAttributeValueInElement(Element ele, String attrName, String value)
	  {
         	if(ele!=null)
         		ele.setAttribute(attrName,value!=null?value:"");
	  }
	 
	 public static Node getNodeForXpath(String path,Node node) throws Exception
		{
		 XPath xpath = null;
			try {
				xpath = pool.borrowObject();
			} catch (Exception e) {
				LOGGER.error("Unable to borrow object from xpath pool.", e);
				throw e;
			}
			try {
				XPathExpression expr = xpath.compile(path);
				Node returnNode =(Node) expr.evaluate(node,XPathConstants.NODE);
				pool.returnObject(xpath);
				return returnNode;
			} catch (Exception e) {
				LOGGER.error("This object has failed us. Discard it.", e);
				pool.invalidateObject(xpath);
				xpath=null;
				throw e;
			}
		}
	 
	 
	  public static String getValueForXpath(String path,Node node) throws Exception
		{
		    XPath xpath = null;
			try {
				xpath = pool.borrowObject();
			} catch (Exception e) {
				LOGGER.error("Unable to borrow object from xpath pool.", e);
				throw e;
			}
			try {
				XPathExpression expr = xpath.compile(path);
				Node returnNode =(Node) expr.evaluate(node,XPathConstants.NODE);
				pool.returnObject(xpath);
				return returnNode!=null?(returnNode.getTextContent()!=null?returnNode.getTextContent():""):null;
			} catch (Exception e) {
				LOGGER.error("This object has failed us. Discard it.", e);
				pool.invalidateObject(xpath);
				xpath=null;
				throw e;
			}
		}

	  public static NodeList getNodeListForXpath(String path,Node node) throws Exception
	 	{
		    XPath xpath = null;
			try {
				xpath = pool.borrowObject();
			} catch (Exception e) {
				LOGGER.error("Unable to borrow object from xpath pool.", e);
				throw e;
			}
			try {
		 		XPathExpression expr = xpath.compile(path);
		 		NodeList returnNodeList =(NodeList) expr.evaluate(node,XPathConstants.NODESET);
		 		pool.returnObject(xpath);
		 		return returnNodeList;
		 	} catch (Exception e) {
				LOGGER.error("This object has failed us. Discard it.", e);
				pool.invalidateObject(xpath);
				xpath=null;
				throw e;
			}
	 	}
		public static Node createNodeWithName(String nodeName) 
		{
			 Node node=null;
			 try 
			 {
				 Document dom = XMLHandler.getDocument();
				 node = dom.createElement(nodeName);
			 } 
			 catch (Exception e) 
			 {
				 Logger.getLogger(XmlProcessorUtil.class).error("", e);
			 }
			 return node;
		 }
		
		public static Node addNodeToParentGivenDocument(Document xmlDom, Node parentNode, String nodeName, String nodeValue)
		{
			try {
				Element currentNode = xmlDom.createElement(nodeName);
				if(nodeValue == null)
					nodeValue = "";
				
				currentNode.appendChild(xmlDom.createTextNode(nodeValue));
				
				parentNode.appendChild(currentNode);
				return currentNode;
			} catch (Exception e) {
				 Logger.getLogger(XmlProcessorUtil.class).error("", e);
				 return null;
			}
		}

}
