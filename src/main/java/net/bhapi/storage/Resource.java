package net.bhapi.storage;

import java.io.IOException;
import java.io.InputStream;

public class Resource {
	private static int globalID = 0;
	private final int id = globalID++;
	private final InputStream stream;
	private final String path;
	private String name;
	
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
		if (name == null) {
			int index = path.lastIndexOf('/');
			if (index < 0) index = path.lastIndexOf('\\');
			name = path.substring(index + 1, path.lastIndexOf('.'));
		}
		return name;
	}
	
	public void close() throws IOException {
		stream.close();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof Resource)) return false;
		Resource resource = (Resource) obj;
		return resource.path.equals(path);
	}
	
	@Override
	public int hashCode() {
		return id;
	}
}
