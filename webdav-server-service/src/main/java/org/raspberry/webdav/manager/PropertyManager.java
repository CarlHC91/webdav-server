package org.raspberry.webdav.manager;

import java.io.File;
import java.net.URI;
import java.util.Date;

import org.raspberry.webdav.enums.Depth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.util.UriEncoder;

@Component
public class PropertyManager {

	@Autowired
	private ResourceManager resourceManager;

	public StringBuffer doPropFind(File root, URI srcPath, Depth depth) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<multistatus xmlns='DAV:'>");
		buffer.append(parseResourceProp(root, srcPath, depth));
		buffer.append("</multistatus>");

		return buffer;
	}

	private StringBuffer parseResourceProp(File root, URI srcPath, Depth depth) {
		StringBuffer buffer = new StringBuffer();

		buffer.append("<response>");
		buffer.append("<href>" + srcPath + "</href>");
		buffer.append("<propstat>");
		buffer.append("<prop>");

		String displayName = resourceManager.getDisplayName(root, srcPath);
		if (displayName == null) {
			buffer.append("<displayname/>");
		} else {
			buffer.append("<displayname>" + displayName + "</displayname>");
		}

		String contentType = resourceManager.getContentType(root, srcPath);
		if (contentType == null) {
			buffer.append("<getcontenttype/>");
		} else {
			buffer.append("<getcontenttype>" + contentType + "</getcontenttype>");
		}

		int contentLength = resourceManager.getContentLength(root, srcPath);
		buffer.append("<getcontentlength>" + contentLength + "</getcontentlength>");

		Date creationDate = resourceManager.getCreationDate(root, srcPath);
		if (creationDate == null) {
			buffer.append("<creationdate/>");
		} else {
			buffer.append("<creationdate>" + creationDate + "</creationdate>");
		}

		Date lastModified = resourceManager.getLastModified(root, srcPath);
		if (lastModified == null) {
			buffer.append("<getlastmodified/>");
		} else {
			buffer.append("<getlastmodified>" + lastModified + "</getlastmodified>");
		}

		if (!resourceManager.isCollection(root, srcPath)) {
			buffer.append("<resourcetype/>");
		} else {
			buffer.append("<resourcetype>" + "<collection/>" + "</resourcetype>");
		}

		buffer.append("</prop>");
		buffer.append("<status>HTTP/1.1 200 OK</status>");
		buffer.append("</propstat>");
		buffer.append("</response>");

		if (resourceManager.isCollection(root, srcPath) && depth == Depth.DEPTH_1) {
			for (String child : resourceManager.list(root, srcPath)) {
				URI srcChild = URI.create(srcPath + "/" + UriEncoder.encode(child));

				buffer.append(parseResourceProp(root, srcChild, Depth.DEPTH_0));
			}
		}

		return buffer;
	}

}
