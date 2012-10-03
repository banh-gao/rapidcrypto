package net.sf.oneWayCrypto.dirService;


import java.util.*;
import java.io.*;

/**
 * The extending class can be scheduled by a {@link Timer} and will be notified on files changes in the listened directory
 *
 */
public abstract class DirWatcher extends TimerTask {
	private final File directory;
	private final Map<File, Long> lastContent = new HashMap<File, Long>();
	private final Filter fileFilter;

	/**
	 * Create a directory watcher that will notify for all the files events
	 * @param directory
	 * @throws IOException When the passed directory is not valid
	 */
	public DirWatcher(File directory) throws IOException {
		this(directory, ".*");
	}

	/**
	 * Create a directory watcher that will notify only for the file with the specified name pattern
	 * @param directory The directory to watch
	 * @param fileFilter The filename pattern of the files to notify
	 * @throws IOException When the passed directory is not valid
	 */
	public DirWatcher(File directory, String fileFilter) throws IOException {
		if (directory == null || !directory.isDirectory())
			throw new IOException("The passed file is not a valid directory");

		this.directory = directory;
		this.fileFilter = new Filter(fileFilter);

		// Store current file list modified time in the map
		for (File f : directory.listFiles(this.fileFilter)) {
			lastContent.put(f, f.lastModified());
		}
	}

	public final void run() {
		File[] currentFiles = directory.listFiles(fileFilter);
		
		Set<File> checkedFiles = new HashSet<File>();
		
		// scan the files and check for modification/addition
		for (File f : currentFiles) {

			Long last = lastContent.get(f);
			if (last == null) {
				onCreated(f);
			} else if (last.longValue() != f.lastModified()) {
				onChanged(f);
			}
			lastContent.put(f,f.lastModified());
			checkedFiles.add(f);
		}

		// now check for deleted files
		Set<File> deletedFiles = new HashSet<File>(lastContent.keySet());
		deletedFiles.removeAll(checkedFiles);
		
		for (File deletedFile : deletedFiles) {
			lastContent.remove(deletedFile);
			onDeleted(deletedFile);
		}
	}

	/**
	 * Called when a new is created
	 * @param file The created file
	 */
	protected void onCreated(File file) {
	}

	/**
	 * Called when a file is changed
	 * @param file The changed file
	 */
	protected void onChanged(File file) {
	}

	/**
	 * Called when a file is deleted
	 * @param file The deleted file
	 */
	protected void onDeleted(File file) {
	}

	/**
	 * Filter files based on the specified name pattern
	 */
	private class Filter implements FileFilter {
		private final String pathPattern;

		public Filter(String pattern) {
			this.pathPattern = pattern;
		}

		public boolean accept(File file) {
			if (".*".equals(pathPattern)) {
				return true;
			}
			return (file.getAbsolutePath().matches(pathPattern));
		}
	}
}