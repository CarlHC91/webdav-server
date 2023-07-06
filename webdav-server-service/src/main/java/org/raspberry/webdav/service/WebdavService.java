package org.raspberry.webdav.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.raspberry.webdav.enums.Depth;
import org.raspberry.webdav.manager.PropertyManager;
import org.raspberry.webdav.manager.ResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebdavService {

	@Autowired
	private PropertyManager propertyManager;
	@Autowired
	private ResourceManager resourceManager;

	private File root = new File("files");

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());

		if (!resourceManager.exists(root, srcPath)) {
			response.sendError(404, "Not Found");
			return;
		}

		response.setStatus(200);
		response.setContentType(resourceManager.getContentType(root, srcPath));
		response.setContentLength(resourceManager.getContentLength(root, srcPath));

		OutputStream outputStream = response.getOutputStream();
		resourceManager.doRead(root, srcPath, outputStream);
	}

	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());

		if (!resourceManager.exists(root, srcPath)) {
			response.setStatus(201);
		} else {
			response.setStatus(204);
		}

		InputStream inputStream = request.getInputStream();
		resourceManager.doWrite(root, srcPath, inputStream);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		doPut(request, response);
	}

	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());

		if (!resourceManager.exists(root, srcPath)) {
			response.sendError(404, "Not Found");
			return;
		}

		if (!resourceManager.doDelete(root, srcPath)) {
			response.sendError(500, "Internal Server Error");
			return;
		}

		response.setStatus(204);
	}

	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		response.setStatus(200);
		response.setHeader("DAV", "1");
		response.setHeader("Allow", "GET, PUT, POST, DELETE, OPTIONS, COPY, MOVE, MKCOL, PROPFIND");
	}

	public void doCopy(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		URI dstPath = URI.create(request.getHeader("Destination"));

		if (!resourceManager.exists(root, srcPath)) {
			response.sendError(404, "Not Found");
			return;
		}

		if (!resourceManager.doCopy(root, srcPath, dstPath)) {
			response.sendError(500, "Internal Server Error");
			return;
		}

		response.setStatus(204);
	}

	public void doMove(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());
		URI dstPath = URI.create(request.getHeader("Destination"));

		if (!resourceManager.exists(root, srcPath)) {
			response.sendError(404, "Not Found");
			return;
		}

		if (!resourceManager.doMove(root, srcPath, dstPath)) {
			response.sendError(500, "Internal Server Error");
			return;
		}

		response.setStatus(204);
	}

	public void doMkCol(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());

		if (!resourceManager.doMkCol(root, srcPath)) {
			response.sendError(500, "Internal Server Error");
			return;
		}

		response.setStatus(201);
	}

	public void doLock(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(501, "Not Implemented");
	}

	public void doUnlock(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(501, "Not Implemented");
	}

	public void doPropFind(HttpServletRequest request, HttpServletResponse response) throws IOException {
		URI srcPath = URI.create(request.getRequestURI());

		Depth depth = Depth.parse(request.getHeader("Depth"));

		if (!resourceManager.exists(root, srcPath)) {
			response.sendError(404, "Not Found");
			return;
		}

		StringBuffer buffer = propertyManager.doPropFind(root, srcPath, depth);

		response.setStatus(207);
		response.setContentType("text/xml");
		response.setContentLength(buffer.length());
		response.getWriter().write(buffer.toString());
	}

	public void doPropPatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.sendError(501, "Not Implemented");
	}

}
