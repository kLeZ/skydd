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

import lombok.Builder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Builder
public record UrlSanitizer(List<String> queriesToRemove) {
	public String sanitize(String url) {
		try {
			URI uri = new URI(url);
			String path = uri.getPath();
			String sanitizedQuery = removeQueryParameters(uri.getRawQuery());
			URI sanitizedUri = new URI(uri.getScheme(), uri.getAuthority(), path, sanitizedQuery, uri.getFragment());
			return sanitizedUri.toString();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String removeQueryParameters(String query) {
		if (query == null) {
			return null;
		}

		StringBuilder sanitizedQuery = new StringBuilder();
		String[] parameters = query.split("&");
		for (String parameter : parameters) {
			String[] keyValue = parameter.split("=", 2);
			if (keyValue.length == 2) {
				String key = keyValue[0];
				if (!shouldRemoveQueryParameter(key)) {
					sanitizedQuery.append(parameter).append("&");
				}
			}
		}

		if (sanitizedQuery.length() > 0) {
			sanitizedQuery.setLength(sanitizedQuery.length() - 1);
			return sanitizedQuery.toString();
		}

		return null;
	}

	private boolean shouldRemoveQueryParameter(String key) {
		for (String parameter : queriesToRemove) {
			if (key.startsWith(parameter)) {
				return true;
			}
		}
		return false;
	}
}
