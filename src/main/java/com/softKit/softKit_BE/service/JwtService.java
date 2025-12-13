package com.softKit.softKit_BE.service;

import com.softKit.softKit_BE.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

	private final JwtProperties jwtProperties;
	private final Key signingKey;

	public JwtService(JwtProperties jwtProperties) {
		this.jwtProperties = jwtProperties;

		byte[] keyBytes;
		try {
			keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
		} catch (Exception ignored) {
			keyBytes = jwtProperties.getSecret() != null
					? jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
					: new byte[0];
		}

		if (keyBytes.length < 32) {
			throw new IllegalArgumentException("JWT secret is too short. Provide a Base64-encoded 32+ byte key via security.jwt.secret / JWT_SECRET.");
		}

		this.signingKey = Keys.hmacShaKeyFor(keyBytes);

	}

	public long getExpirationMs() {
		return jwtProperties.getExpirationMs();
	}

	public String generateToken(UserDetails userDetails) {
		Instant now = Instant.now();
		Instant exp = now.plusMillis(jwtProperties.getExpirationMs());

		return Jwts.builder()
				.setSubject(userDetails.getUsername())
				.claim("roles", userDetails.getAuthorities().stream()
						.map(GrantedAuthority::getAuthority)
						.toList())
				.setIssuedAt(Date.from(now))
				.setExpiration(Date.from(exp))
				.signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();
    }

	public String extractUsername(String token) {
		return extractAllClaims(token).getSubject();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String username = extractUsername(token);
		return username != null
				&& username.equalsIgnoreCase(userDetails.getUsername())
				&& !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		Date expiration = extractAllClaims(token).getExpiration();
		return expiration == null || expiration.before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(signingKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

}
