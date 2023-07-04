package org.raspberry.webdav.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PropName {

	DISPLAY_NAME("displayname"), CONTENT_TYPE("getcontenttype"), CONTENT_LENGTH("getcontentlength"),
	CREATION_DATE("creationdate"), LAST_MODIFIED("getlastmodified"), RESOURCE_TYPE("resourcetype");

	private final String regex;

	private PropName(String regex) {
		this.regex = regex;
	}

	public String getRegex() {
		return regex;
	}

	public static PropName parse(String value) {
		for (PropName propName : PropName.values()) {
			Pattern pattern = Pattern.compile(propName.getRegex());
			Matcher matcher = pattern.matcher(value);

			if (matcher.find()) {
				return propName;
			}
		}

		return null;
	}

}
