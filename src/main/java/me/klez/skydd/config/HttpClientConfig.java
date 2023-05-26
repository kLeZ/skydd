/*
 * Copyright (C) 2023 Alessandro 'kLeZ' Accardo
 *
 * This file is part of skydd.
 *
 * skydd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * skydd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with skydd.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package me.klez.skydd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.time.Duration;

@Configuration
public class HttpClientConfig {
	@Value("${http-client.proxy.enabled:false}")
	private boolean proxyEnabled;

	@Value("${http-client.proxy.host:}")
	private String proxyHost;

	@Value("${http-client.proxy.port:0}")
	private int proxyPort;

	@Value("${http-client.proxy.username:}")
	private String proxyUsername;

	@Value("${http-client.proxy.password:}")
	private String proxyPassword;

	@Value("${http-client.ssl-validation.enabled:false}")
	private boolean sslValidationEnabled;

	@Bean
	public HttpClient httpClient() throws KeyManagementException, NoSuchAlgorithmException {
		Builder httpClientBuilder = HttpClient.newBuilder().version(Version.HTTP_2).followRedirects(Redirect.NORMAL).connectTimeout(Duration.ofSeconds(10));

		if (proxyEnabled && !proxyHost.isEmpty() && proxyPort != 0) {
			// Configurazione del proxy
			if (!proxyUsername.isEmpty() && !proxyPassword.isEmpty()) {
				// Configurazione dell'autenticazione del proxy
				Authenticator.setDefault(new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						if (getRequestorType() == RequestorType.PROXY) {
							return new PasswordAuthentication(proxyUsername, proxyPassword.toCharArray());
						}
						return null;
					}
				});
			}

			ProxySelector proxySelector = ProxySelector.of(new InetSocketAddress(proxyHost, proxyPort));
			httpClientBuilder.proxy(proxySelector);
		}

		if (!sslValidationEnabled) {
			// Disabilita la validazione del certificato SSL/TLS
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s) { //NOSONAR
					// Questo metodo non è implementato per la disabilitazione della validazione del certificato.
					// In questo contesto, si ritiene che tutti i certificati client siano fidati.
					// Non viene effettuato alcun controllo sul certificato client.
				}

				@Override
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s) { //NOSONAR
					// Questo metodo non è implementato per la disabilitazione della validazione del certificato.
					// In questo contesto, si ritiene che tutti i certificati server siano fidati.
					// Non viene effettuato alcun controllo sul certificato server.
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					// In questo contesto, non sono forniti certificati fidati specifici.
					// Restituiamo una lista vuota per evitare potenziali errori nel codice client che si aspetta una lista valida.
					return new X509Certificate[0];
				}
			}}, null);

			// Configura HttpClient per utilizzare il contesto SSL personalizzato senza validazione
			httpClientBuilder.sslContext(sslContext);
			httpClientBuilder.sslParameters(sslContext.getDefaultSSLParameters());
		}

		return httpClientBuilder.build();
	}
}
