package org.raspberry.webdav.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Depth {

	DEPTH_0("0"), DEPTH_1("1"), DEPTH_INFINITY("Infinity");

	private final String regex;

	private Depth(String regex) {
		this.regex = regex;
	}

	public String getRegex() {
		return regex;
	}

	public static Depth parse(String value) {
		for (Depth depth : Depth.values()) {
			Pattern pattern = Pattern.compile(depth.getRegex());
			Matcher matcher = pattern.matcher(value);

			if (matcher.find()) {
				return depth;
			}
		}

		return null;
	}

}
