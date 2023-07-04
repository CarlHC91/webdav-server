package org.raspberry.webdav.parser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

//import org.raspberry.auth.iface.user.UserSessionIface;
//import org.raspberry.auth.iface.webdav.WebdavDirectoryIface;
//import org.raspberry.auth.pojos.entities.user.UserDetailsVO;
//import org.raspberry.auth.pojos.entities.webdav.WebdavDirectoryVO;
import org.raspberry.cloud.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ResourceDetailsParser {

//	@Autowired
//	private UserSessionIface userSessionIface;
//	@Autowired
//	private WebdavDirectoryIface webdavDirectoryIface;

//	public File getRoot() {
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//		UserDetailsVO userSessionVO = new UserDetailsVO();
//		userSessionVO.setTokenApi((String) authentication.getPrincipal());
//
//		UserDetailsVO userDetailsVO = userSessionIface.findOneByTokenApi(userSessionVO);
//		if (userDetailsVO == null) {
//			throw new ServiceException("UserSession [TokenApi: " + userSessionVO.getTokenApi() + "] not exists");
//		}
//
//		WebdavDirectoryVO webdavDirectoryVO = webdavDirectoryIface.findOneByUser(userDetailsVO);
//		if (webdavDirectoryVO == null) {
//			throw new ServiceException("WebdavDirectory [IdUser: " + userDetailsVO.getIdUser() + "] not exists");
//		}
//
//		return new File(webdavDirectoryVO.getFilePath());
//	}

	public String getDisplayName(File root, String path) {
		File file = new File(root, path);

		return file.getName();
	}

	public String getContentType(File root, String path) {
		File file = new File(root, path);

		if (file.isDirectory()) {
			return "inode/directory";
		}

		return null;
	}

	public Long getContentLength(File root, String path) {
		File file = new File(root, path);

		return file.length();
	}

	public Date getCreationDate(File root, String path) {
		File file = new File(root, path);

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

			return new Date(attr.creationTime().toMillis());
		} catch (Exception ex) {
			return null;
		}
	}

	public Date getLastModified(File root, String path) {
		File file = new File(root, path);

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

			return new Date(attr.lastModifiedTime().toMillis());
		} catch (Exception ex) {
			return null;
		}
	}

	public Boolean isCollection(File root, String path) {
		File file = new File(root, path);

		return file.isDirectory();
	}

	public Boolean exists(File root, String path) {
		File file = new File(root, path);

		return file.exists();
	}

	public String[] getMembers(File root, String path) {
		File file = new File(root, path);

		return file.list();
	}

}
