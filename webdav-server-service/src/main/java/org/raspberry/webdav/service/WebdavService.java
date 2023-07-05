package org.raspberry.webdav.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.raspberry.cloud.exception.ServiceException;
import org.raspberry.webdav.enums.Depth;
import org.raspberry.webdav.enums.PropName;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class WebdavService {

	private File root = new File("files");

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		if (!srcFile.exists()) {
			response.sendError(404, "Not Found");
			return;
		}

		response.setStatus(200);
		response.setContentType(getContentType(srcFile));
		response.setContentLength(getContentLength(srcFile));

		InputStream inputStream = new FileInputStream(srcFile);
		OutputStream outputStream = response.getOutputStream();
		transferStreams(inputStream, outputStream);
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		if (!srcFile.exists()) {
			response.setStatus(201);
		} else {
			response.setStatus(204);
		}

		InputStream inputStream = request.getInputStream();
		OutputStream outputStream = new FileOutputStream(srcFile);
		transferStreams(inputStream, outputStream);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		if (!srcFile.exists()) {
			response.setStatus(201);
		} else {
			response.setStatus(204);
		}

		InputStream inputStream = request.getInputStream();
		OutputStream outputStream = new FileOutputStream(srcFile);
		transferStreams(inputStream, outputStream);
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		if (!srcFile.exists()) {
			response.sendError(404, "Not Found");
			return;
		}

		response.setStatus(204);

		doDelete(srcFile);
	}

	private void doDelete(File srcFile) {
		if (srcFile.isDirectory()) {
			for (String path : srcFile.list()) {
				doDelete(new File(srcFile, path));
			}
		}

		srcFile.delete();
	}

	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(200);
		response.setHeader("DAV", "1, 2");
		response.setHeader("Allow", "GET, PUT, POST, DELETE, OPTIONS, COPY, MOVE, MKCOL, PROPFIND");
	}

	public void doCopy(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		URI dstPath = URI.create(request.getHeader("Destination"));
		File dstFile = new File(root, dstPath.getPath());

		if (!srcFile.exists()) {
			response.sendError(404, "Not Found");
			return;
		}

		response.setStatus(204);

		doCopy(srcFile, dstFile);
	}

	private void doCopy(File srcFile, File dstFile) throws IOException {
		if (srcFile.isDirectory()) {
			dstFile.mkdir();

			for (String path : srcFile.list()) {
				doCopy(new File(srcFile, path), new File(dstFile, path));
			}
		} else {
			InputStream input = new FileInputStream(srcFile);
			OutputStream output = new FileOutputStream(dstFile);
			transferStreams(input, output);
		}
	}

	public void doMove(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		URI dstPath = URI.create(request.getHeader("Destination"));
		File dstFile = new File(root, dstPath.getPath());

		if (!srcFile.exists()) {
			response.sendError(404, "Not Found");
			return;
		}

		response.setStatus(204);

		doMove(srcFile, dstFile);
	}

	private void doMove(File srcFile, File dstFile) throws IOException {
		if (srcFile.isDirectory()) {
			dstFile.mkdir();

			for (String path : srcFile.list()) {
				doMove(new File(srcFile, path), new File(dstFile, path));
			}
		} else {
			InputStream input = new FileInputStream(srcFile);
			OutputStream output = new FileOutputStream(dstFile);
			transferStreams(input, output);
		}

		srcFile.delete();
	}

	public void doMkCol(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		response.setStatus(201);

		srcFile.mkdir();
	}

	public void doPropFind(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		File srcFile = new File(root, srcPath.getPath());

		Depth depth = Depth.parse(request.getHeader("Depth"));

		if (!srcFile.exists()) {
			response.sendError(404, "Not Found");
			return;
		}

		if (request.getContentLength() > 0) {
			Document document = parse(request.getInputStream());

			if (existNode("/propfind/prop", document)) {
				List<PropName> propNames = new ArrayList<>();

				for (String nodeName : getNodeNames("/propfind/prop/*", document)) {
					propNames.add(PropName.parse(nodeName));
				}

				StringBuffer buffer = new StringBuffer();
				buffer.append("<multistatus xmlns='DAV:'>");
				buffer.append(parseResourceProp(root, srcPath.getPath(), depth, propNames));
				buffer.append("</multistatus>");

				response.setStatus(207);
				response.setContentType("text/xml");
				response.setContentLength(buffer.length());
				response.getWriter().write(buffer.toString());
				return;
			} else if (existNode("/propfind/propname", document)) {
				StringBuffer buffer = new StringBuffer();
				buffer.append("<multistatus xmlns='DAV:'>");
				buffer.append(parseResourcePropName(root, srcPath.getPath(), depth));
				buffer.append("</multistatus>");

				response.setStatus(207);
				response.setContentType("text/xml");
				response.setContentLength(buffer.length());
				response.getWriter().write(buffer.toString());
				return;
			}
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("<multistatus xmlns='DAV:'>");
		buffer.append(parseResourceProp(root, srcPath.getPath(), depth));
		buffer.append("</multistatus>");

		response.setStatus(207);
		response.setContentType("text/xml");
		response.setContentLength(buffer.length());
		response.getWriter().write(buffer.toString());
	}

	///

	private String getContentType(File file) {
		if (file.isDirectory()) {
			return "inode/directory";
		} else {
			return null;
		}
	}

	private int getContentLength(File file) {
		return (int) file.length();
	}

	private void transferStreams(InputStream input, OutputStream output) throws IOException {
		int pos = 0;
		byte[] buffer = new byte[1024];

		while ((pos = input.read(buffer)) != -1) {
			output.write(buffer, 0, pos);
		}
	}

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

			List<String> nodeNames = new ArrayList<>();

			for (int i = 0; i < nodes.getLength(); i++) {
				Node node = nodes.item(i);

				nodeNames.add(node.getNodeName());
			}

			return nodeNames;
		} catch (Exception ex) {
			throw new ServiceException(ex);
		}
	}

	private String parseResourceProp(File root, String srcPath, Depth depth) throws IOException {
		List<PropName> propNames = new ArrayList<>();

		for (PropName propName : PropName.values()) {
			propNames.add(propName);
		}

		return parseResourceProp(root, srcPath, depth, propNames);
	}

	private String parseResourceProp(File root, String srcPath, Depth depth, List<PropName> propNames)
			throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<response>");
		buffer.append("<href>" + srcPath + "</href>");
		buffer.append("<propstat>");
		buffer.append("<prop>");

		for (PropName propName : propNames) {
			if (propName == PropName.DISPLAY_NAME) {
				String displayName = getDisplayName(root, srcPath);
				if (displayName == null) {
					buffer.append("<displayname/>");
				} else {
					buffer.append("<displayname>" + displayName + "</displayname>");
				}
			}

			if (propName == PropName.CONTENT_TYPE) {
				String contentType = getContentType(root, srcPath);
				if (contentType == null) {
					buffer.append("<getcontenttype/>");
				} else {
					buffer.append("<getcontenttype>" + contentType + "</getcontenttype>");
				}
			}

			if (propName == PropName.CONTENT_LENGTH) {
				Long contentLength = getContentLength(root, srcPath);
				if (contentLength == null) {
					buffer.append("<getcontentlength/>");
				} else {
					buffer.append("<getcontentlength>" + contentLength + "</getcontentlength>");
				}
			}

			if (propName == PropName.CREATION_DATE) {
				Date creationDate = getCreationDate(root, srcPath);
				if (creationDate == null) {
					buffer.append("<creationdate/>");
				} else {
					buffer.append("<creationdate>" + creationDate + "</creationdate>");
				}
			}

			if (propName == PropName.LAST_MODIFIED) {
				Date lastModified = getLastModified(root, srcPath);
				if (lastModified == null) {
					buffer.append("<getlastmodified/>");
				} else {
					buffer.append("<getlastmodified>" + lastModified + "</getlastmodified>");
				}
			}

			if (propName == PropName.RESOURCE_TYPE) {
				if (!isCollection(root, srcPath)) {
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

		if (isCollection(root, srcPath) && depth == Depth.DEPTH_1) {
			for (String member : getMembers(root, srcPath)) {
				buffer.append(parseResourceProp(root, srcPath + "/" + member, Depth.DEPTH_0, propNames));
			}
		}

		if (isCollection(root, srcPath) && depth == Depth.DEPTH_INFINITY) {
			for (String member : getMembers(root, srcPath)) {
				buffer.append(parseResourceProp(root, srcPath + "/" + member, Depth.DEPTH_INFINITY, propNames));
			}
		}

		return buffer.toString();
	}

	private String parseResourcePropName(File root, String srcPath, Depth depth) throws IOException {
		List<PropName> propNames = new ArrayList<>();

		for (PropName propName : PropName.values()) {
			propNames.add(propName);
		}

		return parseResourcePropName(root, srcPath, depth, propNames);
	}

	private String parseResourcePropName(File root, String srcPath, Depth depth, List<PropName> propNames)
			throws IOException {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<response>");
		buffer.append("<href>" + srcPath + "</href>");
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

		if (isCollection(root, srcPath) && depth == Depth.DEPTH_1) {
			for (String member : getMembers(root, srcPath)) {
				buffer.append(parseResourcePropName(root, srcPath + "/" + member, Depth.DEPTH_0, propNames));
			}
		}

		if (isCollection(root, srcPath) && depth == Depth.DEPTH_INFINITY) {
			for (String member : getMembers(root, srcPath)) {
				buffer.append(parseResourcePropName(root, srcPath + "/" + member, Depth.DEPTH_INFINITY, propNames));
			}
		}

		return buffer.toString();
	}

	private String getDisplayName(File root, String path) {
		File file = new File(root, path);

		return file.getName();
	}

	private String getContentType(File root, String path) {
		File file = new File(root, path);

		if (file.isDirectory()) {
			return "inode/directory";
		}

		return null;
	}

	private Long getContentLength(File root, String path) {
		File file = new File(root, path);

		return file.length();
	}

	private Date getCreationDate(File root, String path) {
		File file = new File(root, path);

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

			return new Date(attr.creationTime().toMillis());
		} catch (Exception ex) {
			return null;
		}
	}

	private Date getLastModified(File root, String path) {
		File file = new File(root, path);

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

			return new Date(attr.lastModifiedTime().toMillis());
		} catch (Exception ex) {
			return null;
		}
	}

	private Boolean isCollection(File root, String path) {
		File file = new File(root, path);

		return file.isDirectory();
	}

	private String[] getMembers(File root, String path) {
		File file = new File(root, path);

		return file.list();
	}

}
