package com.softKit.softKit_BE.testSupport;

import com.softKit.softKit_BE.auth.application.JwtService;
import com.softKit.softKit_BE.shared.config.jwt.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.mockito.Mockito;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Date;

import static org.mockito.Mockito.when;

public final class JwtTestSupport {

	private JwtTestSupport() {}

	public static String base64SecretOfBytes(int size) {
		byte[] bytes = new byte[size];
		for (int i = 0; i < size; i++) bytes[i] = (byte) (i + 1);
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static JwtService newService(String secret, long expirationMs) {
		JwtProperties props = Mockito.mock(JwtProperties.class);
		when(props.getSecret()).thenReturn(secret);
		when(props.getExpirationMs()).thenReturn(expirationMs);
		return new JwtService(props);
	}

	public static SecretKey signingKey(JwtService service) {
		try {
			Field f = JwtService.class.getDeclaredField("signingKey");
			f.setAccessible(true);
			return (SecretKey) f.get(service);
		} catch (Exception e) {
			throw new RuntimeException("Failed to access signingKey via reflection", e);
		}
	}

	public static Claims parseClaims(JwtService service, String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey(service))
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public static String token(JwtService service, String subject, Date issuedAt, Date expiration) {
		var b = Jwts.builder()
				.setIssuedAt(issuedAt)
				.signWith(signingKey(service), SignatureAlgorithm.HS256);

		if (subject != null) b.setSubject(subject);
		if (expiration != null) b.setExpiration(expiration);

		return b.compact();
	}

	public static String tokenWithoutExpiration(JwtService service, String subject, Date issuedAt) {
		return token(service, subject, issuedAt, null);
	}
}