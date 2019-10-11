package com.knowology.km.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.knowology.UtilityOperate.Base64;


public class DESBase64 {
	private static Logger logger = Logger.getLogger(DESBase64.class);
	// 秘钥必须24位
	public static final String TOKEN = "VFNSMzQ1Njc4OTAxMjM0NTY3ODkwVFNS";

	public static String encryptStringBase64(String sText, String base64Key) {
		if (StringUtils.isBlank(sText) || StringUtils.isBlank(base64Key)) {
			return null;
		}
		try {
			byte[] kb = Base64.decode(base64Key.getBytes("utf-8"));

			SecretKeySpec k = new SecretKeySpec(kb, "DESede");

			Cipher cp = Cipher.getInstance("DESede");
			cp.init(Cipher.ENCRYPT_MODE, k);

			byte[] b = sText.getBytes("utf-8");
			byte[] b2 = cp.doFinal(b);

			return new String(Base64.encode(b2), "utf-8");
		} catch (Exception e) {
			logger.error("加密失败，原文{" + sText + "}");
		}
		return null;
	}

	public static String decryptStringBase64(String base64Text, String base64Key) {
		if (StringUtils.isBlank(base64Text) || StringUtils.isBlank(base64Key)) {
			return null;
		}
		try {
			byte[] kb = Base64.decode(base64Key.getBytes("utf-8"));

			SecretKeySpec k = new SecretKeySpec(kb, "DESede");

			Cipher cp = Cipher.getInstance("DESede");
			cp.init(Cipher.DECRYPT_MODE, k);

			byte[] c = Base64.decode(base64Text.getBytes("utf-8"));
			byte[] b = cp.doFinal(c);
			return new String(b, "utf-8");
		} catch (Exception e) {
			logger.error("解密失败，原始密文{" + base64Text + "}");
		}
		return null;
	}
	
	public static void main(String[] args) {
		String username = "znkf";
		String password = "ETC_nkipwd2019";
		String url = "jdbc:mysql://172.30.2.2:3306/znkf?useUnicode=true&characterEncoding=utf8&useOldAliasMetadataBehavior=true";

		System.out.println("username 明文: "+username);
		System.out.println("username 密文："+encryptStringBase64(username, TOKEN));
		System.out.println("-----------------------------------------------------");
		System.out.println("password 明文: "+password);
		System.out.println("password 密文："+encryptStringBase64(password, TOKEN));
		System.out.println("-----------------------------------------------------");
		System.out.println("url 明文: "+url);
		System.out.println("url 密文："+encryptStringBase64(url, TOKEN));
		
		System.out.println("url 明文："+decryptStringBase64("ziC6dN8HqowCWINV2xW9K1f7g20ntcrb4hz8PiOKLSYgDyju2WnmMXxE/f8hFzGmxzXJjmkxxsGMyuYui6LwqFfbzKULcKtsOsm7mySUg35LyXthofvi90TeulHwaPHs7zgh3Al6+XsDBjz5pcjv+w==", TOKEN));
		System.out.println("username 明文："+decryptStringBase64("2k/hQG/1SJs=", TOKEN));
		System.out.println("password 明文："+decryptStringBase64("s/toCGkHsrA=", TOKEN));
		
		
	}
}
