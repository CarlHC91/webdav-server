package org.raspberry.webdav.app;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.raspberry.webdav.service.WebdavService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebdavRest {

	@Autowired
	private WebdavService webdavService;

	@RequestMapping(path = "/**", method = RequestMethod.GET)
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doGet(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.PUT)
	public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doPut(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.POST)
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doPost(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.DELETE)
	public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doDelete(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.OPTIONS)
	public void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doOptions(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.COPY)
	public void doCopy(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doCopy(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.MOVE)
	public void doMove(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doMove(request, response);
	}

	@RequestMapping(path = "/**", method = RequestMethod.MKCOL)
	public void doMkCol(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doMkCol(request, response);
	}
	
	@RequestMapping(path = "/**", method = RequestMethod.PROPFIND)
	public void doPropFind(HttpServletRequest request, HttpServletResponse response) throws IOException {
		webdavService.doPropFind(request, response);
	}
	
}
