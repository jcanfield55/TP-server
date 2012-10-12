/*
 * @author nirmal
 */
package com.nimbler.tp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

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
}
