package org.raspberry.webdav.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.raspberry.webdav.service.ResourceDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ResourceDetailsRest {

	@Autowired
	private ResourceDetailsService resourceDetailsService;

	@RequestMapping(path = "/**", method = RequestMethod.GET)
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();
		OutputStream output = response.getOutputStream();

		System.out.println("GET " + path);

		resourceDetailsService.doGet(root, path, output);
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.PUT)
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();
		InputStream input = request.getInputStream();

		System.out.println("PUT " + path);

		resourceDetailsService.doPut(root, path, input);
		
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.POST)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();
		InputStream input = request.getInputStream();

		System.out.println("POST " + path);

		resourceDetailsService.doPost(root, path, input);
		
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.DELETE)
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();

		System.out.println("DELETE " + path);

		resourceDetailsService.doDelete(root, path);
		
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.OPTIONS)
	public void doOptions(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getRequestURI();

		System.out.println("OPTIONS " + path);

		response.setStatus(200);
		response.setHeader("DAV", "1, 2");
		response.setHeader("Allow", "GET, PUT, POST, DELETE, OPTIONS, COPY, MOVE, MKCOL, PROPFIND");
	}

	@RequestMapping(path = "/**", method = RequestMethod.COPY)
	public void doCopy(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String srcPath = request.getRequestURI();
		String dstPath = request.getHeader("Destination");

		System.out.println("COPY " + srcPath + " -> " + dstPath);

		resourceDetailsService.doCopy(root, srcPath, dstPath);
		
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.MOVE)
	public void doMove(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String srcPath = request.getRequestURI();
		String dstPath = request.getHeader("Destination");

		System.out.println("MOVE " + srcPath + " -> " + dstPath);

		resourceDetailsService.doMove(root, srcPath, dstPath);
		
		response.setStatus(200);
	}

	@RequestMapping(path = "/**", method = RequestMethod.MKCOL)
	public void doMkCol(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();

		System.out.println("MKCOL " + path);

		resourceDetailsService.doMkCol(root, path);
		
		response.setStatus(200);
	}

}
