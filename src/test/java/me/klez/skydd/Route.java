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
package me.klez.skydd;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Generated;
import lombok.Data;
import mockwebserver3.MockResponse;
import okhttp3.Headers;

import java.io.Serializable;
import java.util.Map;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

@Data
@Generated("com.robohorse.robopojogenerator")
public class Route implements Serializable {
	@JsonProperty("path")
	private String path;

	@JsonProperty("response")
	private Response response;

	public MockResponse toMockResponse(String origin) {
		String baseUrl = response.getUrl().replace("${origin}", origin);
		Map<String, String> headers = response.getHeaders();
		headers.replaceAll((k, v) -> v == null ? "" : v.replace("${origin}", origin));
		return new MockResponse()
				.setResponseCode(response.getStatus())
				.setHeaders(Headers.of(headers))
				.setBody(response.getBody().formatted(escapeHtml4(baseUrl)));
	}
}
