package org.raspberry.webdav.service;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.raspberry.cloud.exception.ServiceException;
import org.raspberry.webdav.enums.Depth;
import org.raspberry.webdav.enums.PropName;
import org.raspberry.webdav.parser.ResourceDetailsParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class DocumentDetailsService {

	@Autowired
	private ResourceDetailsParser resourceDetailsParser;

	public String doPropFind(File root, String path, Depth depth, int size, InputStream input) {
		if (size > 0) {
			Document document = parse(input);

			if (existNode("/propfind/prop", document)) {
				List<PropName> propNames = new ArrayList<>();

				for (String name : getNodeNames("/propfind/prop/*", document)) {
					propNames.add(PropName.parse(name));
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append("<multistatus xmlns='DAV:'>");
				buffer.append(parseResourceProp(root, path, depth, propNames));
				buffer.append("</multistatus>");

				return buffer.toString();
			}

			if (existNode("/propfind/propname", document)) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("<multistatus xmlns='DAV:'>");
				buffer.append(parseResourcePropName(root, path, depth));
				buffer.append("</multistatus>");

				return buffer.toString();
			}
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("<multistatus xmlns='DAV:'>");
		buffer.append(parseResourceProp(root, path, depth));
		buffer.append("</multistatus>");

		return buffer.toString();
	}

	///

	private Document parse(InputStream input) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			return builder.parse(input);
		} catch (Exception ex) {
			throw new ServiceException(ex);
		}
	}

	private boolean existNode(String expression, Document document) {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

			return nodes != null && nodes.getLength() > 0;
		} catch (Exception ex) {
			throw new ServiceException(ex);
		}
	}

	private List<String> getNodeNames(String expression, Document document) {
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();

			NodeList nodes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);

			List<String> nodeNames = new LinkedList<String>();

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				nodeNames.add(node.getNodeName());
			}

			return nodeNames;
		} catch (Exception ex) {
			throw new ServiceException(ex);
		}
	}

	///

	private String parseResourceProp(File root, String path, Depth depth) {
		List<PropName> propNames = new ArrayList<>();

		for (PropName propName : PropName.values()) {
			propNames.add(propName);
		}

		return parseResourceProp(root, path, depth, propNames);
	}

	private String parseResourceProp(File root, String path, Depth depth, List<PropName> propNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<response>");
		buffer.append("<href>" + path + "</href>");
		buffer.append("<propstat>");
		buffer.append("<prop>");

		for (PropName propName : propNames) {
			if (propName == PropName.DISPLAY_NAME) {
				String displayName = resourceDetailsParser.getDisplayName(root, path);
				if (displayName == null) {
					buffer.append("<displayname/>");
				} else {
					buffer.append("<displayname>" + displayName + "</displayname>");
				}
			}

			if (propName == PropName.CONTENT_TYPE) {
				String contentType = resourceDetailsParser.getContentType(root, path);
				if (contentType == null) {
					buffer.append("<getcontenttype/>");
				} else {
					buffer.append("<getcontenttype>" + contentType + "</getcontenttype>");
				}
			}

			if (propName == PropName.CONTENT_LENGTH) {
				Long contentLength = resourceDetailsParser.getContentLength(root, path);
				if (contentLength == null) {
					buffer.append("<getcontentlength/>");
				} else {
					buffer.append("<getcontentlength>" + contentLength + "</getcontentlength>");
				}
			}

			if (propName == PropName.CREATION_DATE) {
				Date creationDate = resourceDetailsParser.getCreationDate(root, path);
				if (creationDate == null) {
					buffer.append("<creationdate/>");
				} else {
					buffer.append("<creationdate>" + creationDate + "</creationdate>");
				}
			}

			if (propName == PropName.LAST_MODIFIED) {
				Date lastModified = resourceDetailsParser.getLastModified(root, path);
				if (lastModified == null) {
					buffer.append("<getlastmodified/>");
				} else {
					buffer.append("<getlastmodified>" + lastModified + "</getlastmodified>");
				}
			}

			if (propName == PropName.RESOURCE_TYPE) {
				if (!resourceDetailsParser.isCollection(root, path)) {
					buffer.append("<resourcetype/>");
				} else {
					buffer.append("<resourcetype>" + "<collection/>" + "</resourcetype>");
				}
			}
		}

		buffer.append("</prop>");
		buffer.append("<status>HTTP/1.1 200 OK</status>");
		buffer.append("</propstat>");
		buffer.append("</response>");

		if (resourceDetailsParser.isCollection(root, path) && depth == Depth.DEPTH_1) {
			for (String member : resourceDetailsParser.getMembers(root, path)) {
				buffer.append(parseResourceProp(root, path + "/" + member, Depth.DEPTH_0, propNames));
			}
		}

		if (resourceDetailsParser.isCollection(root, path) && depth == Depth.DEPTH_INFINITY) {
			for (String member : resourceDetailsParser.getMembers(root, path)) {
				buffer.append(parseResourceProp(root, path + "/" + member, Depth.DEPTH_INFINITY, propNames));
			}
		}

		return buffer.toString();
	}

	private String parseResourcePropName(File root, String path, Depth depth) {
		List<PropName> propNames = new ArrayList<>();

		for (PropName propName : PropName.values()) {
			propNames.add(propName);
		}

		return parseResourcePropName(root, path, depth, propNames);
	}

	private String parseResourcePropName(File root, String path, Depth depth, List<PropName> propNames) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<response>");
		buffer.append("<href>" + path + "</href>");
		buffer.append("<propstat>");
		buffer.append("<prop>");

		for (PropName propName : propNames) {
			if (propName == PropName.DISPLAY_NAME) {
				buffer.append("<displayname/>");
			}

			if (propName == PropName.CONTENT_TYPE) {
				buffer.append("<getcontenttype/>");
			}

			if (propName == PropName.CONTENT_LENGTH) {
				buffer.append("<getcontentlength/>");
			}

			if (propName == PropName.CREATION_DATE) {
				buffer.append("<creationdate/>");
			}

			if (propName == PropName.LAST_MODIFIED) {
				buffer.append("<getlastmodified/>");
			}

			if (propName == PropName.RESOURCE_TYPE) {
				buffer.append("<resourcetype/>");
			}
		}

		buffer.append("</prop>");
		buffer.append("<status>HTTP/1.1 200 OK</status>");
		buffer.append("</propstat>");
		buffer.append("</response>");

		if (resourceDetailsParser.isCollection(root, path) && depth == Depth.DEPTH_1) {
			for (String member : resourceDetailsParser.getMembers(root, path)) {
				buffer.append(parseResourcePropName(root, path + "/" + member, Depth.DEPTH_0, propNames));
			}
		}

		if (resourceDetailsParser.isCollection(root, path) && depth == Depth.DEPTH_INFINITY) {
			for (String member : resourceDetailsParser.getMembers(root, path)) {
				buffer.append(parseResourcePropName(root, path + "/" + member, Depth.DEPTH_INFINITY, propNames));
			}
		}

		return buffer.toString();
	}

}
