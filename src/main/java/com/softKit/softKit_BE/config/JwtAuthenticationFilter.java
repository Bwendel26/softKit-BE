package com.softKit.softKit_BE.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softKit.softKit_BE.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
	private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
			JwtService jwtService,
			@Lazy UserDetailsService userDetailsService,
			ObjectMapper objectMapper
	) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
		this.objectMapper = objectMapper;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return path.startsWith("/api/v1/auth")
				|| path.startsWith("/v3/api-docs")
				|| path.startsWith("/swagger-ui")
				|| "/swagger-ui.html".equals(path)
				|| "/error".equals(path)
				|| path.startsWith("/actuator");
	}

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

		final String jwt = authHeader.substring(7); // removing prefix "Bearer"

        try {
            final String userEmail = jwtService.extractUsername(jwt);

            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails UserDetails = userDetailsService.loadUserByUsername(userEmail);

                if (!jwtService.isTokenValid(jwt, UserDetails)) {
					writeUnauthorized(request, response, "Invalid or expired token");
					return;
                }

				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(UserDetails, null, UserDetails.getAuthorities());

				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
            }

			filterChain.doFilter(request, response);

        } catch (Exception e) {
			SecurityContextHolder.clearContext();
			writeUnauthorized(request, response, "Invalid or expired token");
        }
    }

	private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", Instant.now().toString());
		body.put("status", 401);
		body.put("error", "Unauthorized");
		body.put("message", message);
		body.put("path", request.getRequestURI());

		objectMapper.writeValue(response.getOutputStream(), body);
	}
}

