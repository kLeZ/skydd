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

import me.klez.skydd.JsonDispatcher;
import me.klez.skydd.Utils;
import me.klez.skydd.config.SanitizerConfig;
import mockwebserver3.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class UrlSanitizerServiceTest {
	private static MockWebServer server;
	private SanitizerConfig config;

	@BeforeAll
	static void beforeAll() throws IOException {
		server = new MockWebServer();
		server.setDispatcher(new JsonDispatcher(server::getHostName, server::getPort, "/dispatcher.json"));
		server.start();
	}

	@AfterAll
	static void afterAll() throws IOException {
		server.shutdown();
	}

	@BeforeEach
	void setUp() throws IOException {
		config = new SanitizerConfig();
		config.setDefaultParamQueries(Utils.readDefaultParamQueries("/application.yml"));
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/sanitize-test-data.csv", numLinesToSkip = 1)
	void sanitize(final String input, final String output) {
		String url = server.url(input).toString();
		String expected = server.url(output).toString();

		var service = new UrlSanitizerService(config, HttpClient.newHttpClient());
		String sanitized = service.sanitize(url, new String[0]);

		assertThat(sanitized).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/resolveShortLink-test-data.csv", numLinesToSkip = 1)
	void resolveShortLink(final String input, final String output) {
		String url = server.url(input).toString();
		String expected = server.url(output).toString();

		var service = new UrlSanitizerService(config, HttpClient.newHttpClient());
		String sanitized = service.resolveShortLink(url, new String[0]);

		assertThat(sanitized).isEqualTo(expected);
	}

}
