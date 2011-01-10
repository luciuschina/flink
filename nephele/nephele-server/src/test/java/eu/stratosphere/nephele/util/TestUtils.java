/***********************************************************************************************************************
 *
 * Copyright (C) 2010 by the Stratosphere project (http://stratosphere.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/

package eu.stratosphere.nephele.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import eu.stratosphere.nephele.configuration.ConfigConstants;
import eu.stratosphere.nephele.configuration.GlobalConfiguration;
import eu.stratosphere.nephele.jobmanager.JobManagerTest;

/**
 * This class contains a selection of utility functions which are used for testing the nephele-server module.
 * 
 * @author warneke
 */
public class TestUtils {

	/**
	 * Private constructor.
	 */
	private TestUtils() {
	}

	/**
	 * Creates a file with a random name in the directory for temporary files. The directory for temporary files is read
	 * from the configuration. The file contains a sequence of integer numbers from 0 to <code>limit</code>. The
	 * individual numbers are separated by a newline.
	 * 
	 * @param limit
	 *        the upper bound for the sequence of integer numbers to generate
	 * @return a {@link File} object referring to the created file
	 * @throws IOException
	 *         thrown if an I/O error occurs while writing the file
	 */
	public static File createInputFile(int limit) throws IOException {

		if (limit < 0) {
			throw new IllegalArgumentException("limit must be >= 0");
		}

		final File inputFile = new File(getTempDir() + File.separator + getRandomFilename());

		if (inputFile.exists()) {
			inputFile.delete();
		}

		inputFile.createNewFile();
		FileWriter fw = new FileWriter(inputFile);
		for (int i = 0; i < limit; i++) {

			fw.write(Integer.toString(i) + "\n");
		}
		fw.close();

		return inputFile;
	}

	/**
	 * Constructs a random filename. The filename is a string of 16 hex characters followed by a <code>.dat</code>
	 * prefix.
	 * 
	 * @return the random filename
	 */
	public static String getRandomFilename() {

		final char[] alphabeth = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		String filename = "";
		for (int i = 0; i < 16; i++) {
			filename += alphabeth[(int) (Math.random() * alphabeth.length)];
		}

		return filename + ".dat";
	}

	/**
	 * Reads the path to the directory for temporary files from the configuration and returns it.
	 * 
	 * @return the path to the directory for temporary files
	 */
	public static String getTempDir() {

		return GlobalConfiguration.getString(ConfigConstants.TASK_MANAGER_TMP_DIR_KEY,
			ConfigConstants.DEFAULT_TASK_MANAGER_TMP_PATH);
	}

	/**
	 * Creates a jar file from the class with the given class name and stores it in the directory for temporary files.
	 * 
	 * @param className
	 *        the name of the class to create a jar file from
	 * @return a {@link File} object referring to the jar file
	 * @throws IOException
	 *         thrown if an error occurs while writing the jar file
	 */
	public static File createJarFile(String className) throws IOException {

		final String jarPath = getTempDir() + File.separator + className + ".jar";
		final File jarFile = new File(jarPath);

		if (jarFile.exists()) {
			jarFile.delete();
		}

		final JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath), new Manifest());
		final String classPath = JobManagerTest.class.getResource("").getPath() + className + ".class";
		final File classFile = new File(classPath);

		String packageName = JobManagerTest.class.getPackage().getName();
		packageName = packageName.replaceAll("\\.", "\\/");
		jos.putNextEntry(new JarEntry("/" + packageName + "/" + className + ".class"));

		final FileInputStream fis = new FileInputStream(classFile);
		final byte[] buffer = new byte[1024];
		int num = fis.read(buffer);

		while (num != -1) {
			jos.write(buffer, 0, num);
			num = fis.read(buffer);
		}

		fis.close();
		jos.close();

		return jarFile;
	}
}
