package org.raspberry.webdav.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.raspberry.webdav.enums.Depth;
import org.raspberry.webdav.service.DocumentDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DocumentDetailsRest {

	@Autowired
	private DocumentDetailsService documentDetailsService;

	@RequestMapping(path = "/**", method = RequestMethod.PROPFIND)
	public void doPropFind(HttpServletRequest request, HttpServletResponse response) throws IOException {
		File root = new File("files");
		String path = request.getRequestURI();
		Depth depth = Depth.parse(request.getHeader("Depth"));
		int size = request.getContentLength();
		InputStream input = request.getInputStream();
		
		System.out.println("PROPFIND " + path + " (" + depth + ")");

		String payload = documentDetailsService.doPropFind(root, path, depth, size, input);
		response.setStatus(207);
		response.getWriter().write(payload);
	}

}
