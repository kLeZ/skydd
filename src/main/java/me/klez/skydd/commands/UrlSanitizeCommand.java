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
package me.klez.skydd.commands;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.klez.skydd.services.UrlSanitizerService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UrlSanitizeCommand {
	UrlSanitizerService urlSanitizerService;

	@ShellMethod(value = "Sanitize the given URL.", key = {"sanitize", "s"})
	public String sanitize(
			@ShellOption(value = {"-u", "--url"}, help = "The URL to sanitize.") String url,
			@ShellOption(value = {"-pq", "--param-query"}, help = "Additional query parameters to remove.", defaultValue = "") String[] paramQueries,
			@ShellOption(value = {"-s", "--shortlink"}, help = "Enable shortlink (url shortener-generated url) resolution.") boolean shortLink
	) {
		if (shortLink) {
			return urlSanitizerService.resolveShortLink(url, paramQueries);
		} else {
			return urlSanitizerService.sanitize(url, paramQueries);
		}
	}
}
