package no.hvl.dat110.util;

/**
 * project 3
 * @author tdoy
 *
 */

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash { 
	
	private static BigInteger hashint; 

	public static int mbit = 4;			// using SHA-1 compresses/hashes to 160bits	
	
	public static int sbit = 4;			// we use this for the size of the finger table
	
	public static BigInteger hashOf(String entity) {		
		
		byte[] entityByte = entity.getBytes();
		MessageDigest msgDig;
		
		try {
			msgDig = MessageDigest.getInstance("MD5");
			entityByte = msgDig.digest(entity.getBytes());
			
			String res = toHex(entityByte);
			
			hashint = new BigInteger(res, 16);
			
		 } catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
	

		return hashint;
		}
	
	
	public static BigInteger addressSize() {
		
		// Task: compute the address size of MD5
				// get the digest length
				MessageDigest md;
				try {
					md = MessageDigest.getInstance("MD5");
					int length = md.getDigestLength();
					int numBit = length * 8;
					BigInteger svar = new BigInteger("2").pow(numBit);
					return svar;
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
				
				// compute the number of bits = digest length * 8
				
				// compute the address size = 2 ^ number of bits
				
				// return the address size
				
				
			}
	
	public static int bitSize() {
		
		int digestlen = 0;
		
		// find the digest length
		
		return digestlen*8;
	}
	
	public static String toHex(byte[] digest) {
		StringBuilder strbuilder = new StringBuilder();
		for(byte b : digest) {
			strbuilder.append(String.format("%02x", b&0xff));
		}
		return strbuilder.toString();
	}

}
