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

import jakarta.annotation.Nonnull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.klez.skydd.config.SanitizerConfig;
import mockwebserver3.Dispatcher;
import mockwebserver3.MockResponse;
import mockwebserver3.RecordedRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class JsonDispatcher extends Dispatcher {
	Supplier<String> hostname;
	Supplier<Integer> port;
	String dispatcherDataFile;

	@Nonnull
	@Override
	public MockResponse dispatch(@Nonnull RecordedRequest request) {
		try (InputStream resource = SanitizerConfig.class.getResourceAsStream(dispatcherDataFile)) {
			final DispatcherData dispatcherData = JsonUtils.createMapper().readValue(resource, DispatcherData.class);
			final String origin = computeOrigin();
			final String path = request.getPath();
			return dispatcherData.getRoutes().stream()
					.filter(r -> r.getPath().equalsIgnoreCase(requireNonNull(path)))
					.map(route -> route.toMockResponse(origin))
					.findAny()
					.orElse(new MockResponse().setResponseCode(404));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private String computeOrigin() {
		String origin;
		try {
			URL baseUrl = new URL("http", hostname.get(), port.get(), "");
			origin = baseUrl.toString();
		} catch (MalformedURLException e) {
			//noinspection HttpUrlsUsage
			origin = "http://%s:%d".formatted(hostname.get(), port.get());
		}
		return origin;
	}
}
