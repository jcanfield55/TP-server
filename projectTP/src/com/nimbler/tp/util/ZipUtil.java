/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class ZipUtil {

	/**
	 * Compress bytes.
	 *
	 * @param data the data
	 * @return the byte[]
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static byte[] compressToBytes(String data) throws UnsupportedEncodingException, IOException
	{
		byte[] input = data.getBytes("UTF-8"); 
		Deflater df = new Deflater();   
		df.setInput(input);

		ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length); 
		df.finish();
		byte[] buff = new byte[1024];   
		while(!df.finished())
		{
			int count = df.deflate(buff); 
			baos.write(buff, 0, count);   
		}
		baos.close();
		byte[] output = baos.toByteArray();

		return output;
	}

	/**
	 * Compress to string.
	 *
	 * @param data the data
	 * @return the string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static String compressToString(String data) throws UnsupportedEncodingException, IOException {
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(compressToBytes(data));
	}

	/**
	 * De compress string.
	 *
	 * @param data the data
	 * @return the string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DataFormatException the data format exception
	 */
	public static String deCompressString(String data) throws UnsupportedEncodingException, IOException, DataFormatException {
		BASE64Decoder decoder = new BASE64Decoder();
		return extractBytes(decoder.decodeBuffer(data));
	}
	public static void addFilesToExistingZip(File zipFile,File...files) throws IOException {
		File tempFile = File.createTempFile(zipFile.getName(), null);
		tempFile.delete();

		boolean renameOk=zipFile.renameTo(tempFile);
		if (!renameOk){
			throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
		}
		byte[] buf = new byte[1024];

		ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

		ZipEntry entry = zin.getNextEntry();
		while (entry != null) {
			String name = entry.getName();
			boolean notInFiles = true;
			for (File f : files) {
				if (f.getName().equals(name)) {
					notInFiles = false;
					break;
				}
			}
			if (notInFiles) {
				out.putNextEntry(new ZipEntry(name));
				int len;
				while ((len = zin.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			}
			entry = zin.getNextEntry();
		}
		zin.close();
		for (int i = 0; i < files.length; i++) {
			InputStream in = new FileInputStream(files[i]);
			out.putNextEntry(new ZipEntry(files[i].getName()));
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			out.closeEntry();
			in.close();
		}
		out.close();
		tempFile.delete();
	}

	/**
	 * Extract bytes.
	 *
	 * @param input the input
	 * @return the string
	 * @throws UnsupportedEncodingException the unsupported encoding exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws DataFormatException the data format exception
	 */
	public static String extractBytes(byte[] input) throws UnsupportedEncodingException, IOException, DataFormatException
	{
		Inflater ifl = new Inflater();   
		ifl.setInput(input);

		ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
		byte[] buff = new byte[1024];
		while(!ifl.finished())
		{
			int count = ifl.inflate(buff);
			baos.write(buff, 0, count);
		}
		baos.close();
		byte[] output = baos.toByteArray();
		return new String(output);
	}

	public static void writeStopTimes(File file, String res) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
		ZipEntry ze = new ZipEntry("stop_times.txt");
		zos.putNextEntry(ze);
		IOUtils.copy(new StringReader(res), zos);
		zos.closeEntry();
		zos.close();
	}
}
