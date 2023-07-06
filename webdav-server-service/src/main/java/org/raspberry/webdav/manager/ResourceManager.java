package org.raspberry.webdav.manager;

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
import java.util.Date;

import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.util.UriEncoder;

@Component
public class ResourceManager {

	public void doRead(File root, URI srcPath, OutputStream outputStream) throws IOException {
		File srcRoot = new File(root, srcPath.getPath());

		InputStream inputStream = new FileInputStream(srcRoot);

		transferStreams(inputStream, outputStream);

		inputStream.close();
	}

	public void doWrite(File root, URI srcPath, InputStream inputStream) throws IOException {
		File srcRoot = new File(root, srcPath.getPath());

		OutputStream outputStream = new FileOutputStream(srcRoot);

		transferStreams(inputStream, outputStream);

		outputStream.close();
	}

	public boolean doCopy(File root, URI srcPath, URI dstPath) throws IOException {
		File srcRoot = new File(root, srcPath.getPath());
		File dstRoot = new File(root, dstPath.getPath());

		if (srcRoot.isDirectory()) {
			if (!dstRoot.mkdir()) {
				return false;
			}

			for (String child : srcRoot.list()) {
				URI srcChild = URI.create(srcPath + "/" + UriEncoder.encode(child));
				URI dstChild = URI.create(dstPath + "/" + UriEncoder.encode(child));

				if (!doCopy(root, srcChild, dstChild)) {
					return false;
				}
			}
		}

		if (srcRoot.isFile()) {
			InputStream inputStream = new FileInputStream(srcRoot);
			OutputStream outputStream = new FileOutputStream(dstRoot);

			transferStreams(inputStream, outputStream);

			inputStream.close();
			outputStream.close();
		}

		return true;
	}

	public boolean doMove(File root, URI srcPath, URI dstPath) throws IOException {
		File srcRoot = new File(root, srcPath.getPath());
		File dstRoot = new File(root, dstPath.getPath());

		if (srcRoot.isDirectory()) {
			if (!dstRoot.mkdir()) {
				return false;
			}

			for (String child : srcRoot.list()) {
				URI srcChild = URI.create(srcPath + "/" + UriEncoder.encode(child));
				URI dstChild = URI.create(dstPath + "/" + UriEncoder.encode(child));

				if (!doMove(root, srcChild, dstChild)) {
					return false;
				}
			}

			if (!srcRoot.delete()) {
				return false;
			}
		}

		if (srcRoot.isFile()) {
			InputStream inputStream = new FileInputStream(srcRoot);
			OutputStream outputStream = new FileOutputStream(dstRoot);

			transferStreams(inputStream, outputStream);

			inputStream.close();
			outputStream.close();

			if (!srcRoot.delete()) {
				return false;
			}
		}

		return true;
	}

	public boolean doDelete(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		if (srcRoot.isDirectory()) {
			for (String child : srcRoot.list()) {
				URI srcChild = URI.create(srcPath + "/" + UriEncoder.encode(child));

				if (!doDelete(root, srcChild)) {
					return false;
				}
			}

			if (!srcRoot.delete()) {
				return false;
			}
		}

		if (srcRoot.isFile()) {
			if (!srcRoot.delete()) {
				return false;
			}
		}

		return true;
	}

	public boolean doMkCol(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return srcRoot.mkdir();
	}

	///

	public String getDisplayName(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return UriEncoder.encode(srcRoot.getName());
	}

	public String getContentType(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		if (srcRoot.isDirectory()) {
			return "inode/directory";
		}

		return null;
	}

	public int getContentLength(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return (int) srcRoot.length();
	}

	public Date getCreationDate(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(srcRoot.getPath()), BasicFileAttributes.class);

			return new Date(attr.creationTime().toMillis());
		} catch (IOException ex) {
			return null;
		}
	}

	public Date getLastModified(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		try {
			BasicFileAttributes attr = Files.readAttributes(Path.of(srcRoot.getPath()), BasicFileAttributes.class);

			return new Date(attr.lastModifiedTime().toMillis());
		} catch (IOException ex) {
			return null;
		}
	}

	public boolean exists(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return srcRoot.exists();
	}

	public boolean isCollection(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return srcRoot.isDirectory();
	}

	public String[] list(File root, URI srcPath) {
		File srcRoot = new File(root, srcPath.getPath());

		return srcRoot.list();
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
