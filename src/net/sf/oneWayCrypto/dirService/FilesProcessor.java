package net.sf.oneWayCrypto.dirService;

import java.io.File;
import java.io.IOException;

public class FilesProcessor extends DirWatcher {

	public FilesProcessor(File directory) throws IOException {
		super(directory);
	}
	
	@Override
	protected void onChanged(File file) {
		System.out.println("Changed: " + file);
	}

}
