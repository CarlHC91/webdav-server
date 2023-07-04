package org.raspberry.webdav.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.raspberry.cloud.exception.ServiceException;
import org.raspberry.webdav.parser.ResourceDetailsParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceDetailsService {

	@Autowired
	private ResourceDetailsParser resourceDetailsParser;

	public void doGet(File root, String path, OutputStream output) {
		File file = new File(root, path);

		try {
			InputStream input = new FileInputStream(file);
			transferStreams(input, output);
		} catch (IOException ex) {
			throw new ServiceException(ex);
		}
	}

	public void doPut(File root, String path, InputStream input) {
		File file = new File(root, path);

		try {
			OutputStream output = new FileOutputStream(file);
			transferStreams(input, output);
		} catch (IOException ex) {
			throw new ServiceException(ex);
		}
	}

	public void doPost(File root, String path, InputStream input) {
		File file = new File(root, path);

		try {
			OutputStream output = new FileOutputStream(file);
			transferStreams(input, output);
		} catch (IOException ex) {
			throw new ServiceException(ex);
		}
	}

	public void doDelete(File root, String path) {
		File file = new File(root, path);

		if (resourceDetailsParser.isCollection(root, path)) {
			for (String member : resourceDetailsParser.getMembers(root, path)) {
				doDelete(root, path + "/" + member);
			}

			file.delete();
		}

		if (!resourceDetailsParser.isCollection(root, path)) {
			file.delete();
		}
	}

	public void doCopy(File root, String srcPath, String dstPath) {
		File srcFile = new File(root, srcPath);
		File dstFile = new File(root, dstPath);

		if (resourceDetailsParser.isCollection(root, srcPath)) {
			dstFile.mkdir();

			for (String member : resourceDetailsParser.getMembers(root, srcPath)) {
				doCopy(root, srcPath + "/" + member, dstPath + "/" + member);
			}
		}

		if (!resourceDetailsParser.isCollection(root, srcPath)) {
			try {
				InputStream input = new FileInputStream(srcFile);
				OutputStream output = new FileOutputStream(dstFile);
				transferStreams(input, output);
			} catch (IOException ex) {
				throw new ServiceException(ex);
			}
		}
	}

	public void doMove(File root, String srcPath, String dstPath) {
		File srcFile = new File(root, srcPath);
		File dstFile = new File(root, dstPath);

		if (resourceDetailsParser.isCollection(root, srcPath)) {
			dstFile.mkdir();

			for (String member : resourceDetailsParser.getMembers(root, srcPath)) {
				doMove(root, srcPath + "/" + member, dstPath + "/" + member);
			}

			srcFile.delete();
		}

		if (!resourceDetailsParser.isCollection(root, srcPath)) {
			try {
				InputStream input = new FileInputStream(srcFile);
				OutputStream output = new FileOutputStream(dstFile);
				transferStreams(input, output);
			} catch (IOException ex) {
				throw new ServiceException(ex);
			}

			srcFile.delete();
		}
	}

	public void doMkCol(File root, String path) {
		File file = new File(root, path);

		file.mkdir();
	}

	///

	private void transferStreams(InputStream input, OutputStream output) throws IOException {
		int pos = 0;
		byte[] buffer = new byte[1024];

		while ((pos = input.read(buffer)) != -1) {
			output.write(buffer, 0, pos);
		}
	}

}
