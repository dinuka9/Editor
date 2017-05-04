package lk.dinuka.editor;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class CryptoManager {
	private static String initVector = "RandomInitVector";

	public static String encrypt(String key, String initVector, String value)
			throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		System.out.println("String to encrypt : " + value);
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

		byte[] encrypted = cipher.doFinal(value.getBytes());
		System.out.println("encrypted string  : " + Base64.encodeBase64String(encrypted));

		return Base64.encodeBase64String(encrypted);

	}

	public static String decrypt(String key, String initVector, String encrypted)
			throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
			NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

		System.out.println("String to decrypt : " + encrypted);
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

		byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

		System.out.println("decrypted String  : " + new String(original));
		return new String(original);
	}

	// creates a string with 16 chars from a string with chars lass than 16
	// ex : if the input string is 1234
	// method returns 1234123412341234
	public String passwordPadding(String shortpassword) {
		char[] c = shortpassword.toCharArray();
		System.out.println("password length : " + c.length);
		StringBuilder builder = new StringBuilder();
		while (builder.length() < 16) {
			for (int i = 0; i < c.length; i++) {
				if (builder.length() < 16) {
					builder.append(c[i]);
				}
			}
		}
		System.out.println("padded password : " + builder.toString());
		return builder.toString();
	}
}
