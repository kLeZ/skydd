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
package me.klez.skydd.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.klez.skydd.config.SanitizerConfig;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlSanitizerService {
	private final SanitizerConfig sanitizerConfig;
	private final HttpClient httpClient;

	public String resolveShortLink(String shortLink, String[] paramQueries) {
		String sanitized = "";
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.GET()
					.uri(new URI(shortLink))
					.build();

			String resolvedUrl;
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (HttpStatus.Series.valueOf(response.statusCode()) == HttpStatus.Series.REDIRECTION) {
				resolvedUrl = response.headers().firstValue("Location").orElse(shortLink);
			} else if (HttpStatus.Series.valueOf(response.statusCode()) == HttpStatus.Series.INFORMATIONAL ||
					HttpStatus.Series.valueOf(response.statusCode()) == HttpStatus.Series.SUCCESSFUL) {
				resolvedUrl = response.uri().toString();
			} else {
				log.warn("Encountered error either in client or in server response. Complete response is %s".formatted(response));
				resolvedUrl = shortLink;
			}
			sanitized = sanitize(resolvedUrl, paramQueries);
		} catch (IOException | URISyntaxException e) {
			log.warn("Error resolving permalink: " + e.getMessage(), e);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Permalink resolution interrupted: " + e.getMessage(), e);
		}
		return sanitized;
	}

	public String sanitize(String url, String[] paramQueries) {
		List<String> queriesToRemove = new ArrayList<>(sanitizerConfig.getDefaultParamQueries());
		if (paramQueries != null) {
			queriesToRemove.addAll(Arrays.asList(paramQueries));
		}
		return UrlSanitizer.builder().queriesToRemove(queriesToRemove).build().sanitize(url);
	}
}
