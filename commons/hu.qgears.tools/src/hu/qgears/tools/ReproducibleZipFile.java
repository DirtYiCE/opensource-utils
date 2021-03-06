package hu.qgears.tools;

import java.util.zip.ZipEntry;

/**
 * Create a reproducible zip file.
 * The same input results in the same output always.
 * 
 * This is done by setting all timestamps and such to a default value.
 *
 */
public class ReproducibleZipFile {


	public static ZipEntry fix(ZipEntry zipEntry, long t) {
//		zipEntry.setCreationTime(FileTime.from(0, TimeUnit.MILLISECONDS));
//		zipEntry.setLastAccessTime(FileTime.from(0, TimeUnit.MILLISECONDS));
		zipEntry.setTime(t);
		return zipEntry;
	}

}
