package net.bhapi.storage;

import java.io.IOException;
import java.io.InputStream;

public class Resource {
	private final InputStream stream;
	private final String path;
	
	public Resource(InputStream stream, String path) {
		this.stream = stream;
		this.path = path;
	}
	
	public InputStream getStream() {
		return stream;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getName() {
		int index = path.lastIndexOf('/');
		if (index < 0) index = path.lastIndexOf('\\');
		return path.substring(index + 1, path.lastIndexOf('.'));
	}
	
	public void close() throws IOException {
		stream.close();
	}
}
