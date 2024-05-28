package app.diy.note_taking_app.configuration;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

	private final JwtAuthFilter jwtAuthFilter;
	private final AuthenticationProvider authenticationProvider;
	@Autowired
	@Qualifier("delegatedAuthenticationEntryPoint")
	AuthenticationEntryPoint authEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
				.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/v1/auth/**") // URL patterns as whitelist
						.permitAll()
						.anyRequest() // Except URLs in whitelist, other URLs must be authenticated
						.authenticated())
				.cors(cors -> cors.configurationSource(this
						.corsConfigurationSource()))
				.exceptionHandling(handling -> handling
						.authenticationEntryPoint(authEntryPoint))
				.sessionManagement(management -> management // Configure SessionManagement
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider)
				.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	private CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:4000"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH"));
		configuration.setExposedHeaders(Arrays.asList("Authorization", "content-type"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "content-type"));
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
