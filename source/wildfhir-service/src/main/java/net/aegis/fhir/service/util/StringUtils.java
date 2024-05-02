/*
 * #%L
 * WildFHIR - wildfhir-service
 * %%
 * Copyright (C) 2024 AEGIS.net, Inc.
 * All rights reserved.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of AEGIS nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package net.aegis.fhir.service.util;

import java.io.CharArrayWriter;
import java.text.CharacterIterator;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

/**
 * StringUtils
 *
 * @author venkat.keesara
 *
 */
public final class StringUtils {
	// CONSTANTS

	/**
	 * The asterisk character.
	 */
	public static final char ASTERISK = '*';

	/**
	 * The colon character.
	 */
	public static final char COLON = ':';

	/**
	 * The comma character.
	 */
	public static final char COMMA = ',';

	/**
	 * The period character (a dot).
	 */
	public static final char PERIOD = '.';

	/**
	 * The forward slash character.
	 */
	public static final char SLASH = '/';

	/**
	 * The space character.
	 */
	public static final char SPACE = ' ';

	/**
	 * The empty string.
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * A string representing 'true'
	 */
	public static final String TRUE = "true";

	/**
	 * A string representing 'false'
	 */
	public static final String FALSE = "false";

	/**
	 * The separator for aspect paths (per the Java bean naming convention).
	 */
	public static final String ASPECT_PATH_SEPARATOR = ".";

	/**
	 * A comma & space separator
	 */
	public static final String COMMA_DELIMTER = ",";

	/**
	 * A comma & space separator
	 */
	public static final String COMMA_SPACE_DELIMTER = ", ";

	/**
	 * A space separator
	 */
	public static final String SPACE_DELIMTER = " ";

	/**
	 * Default non empty drop down value
	 */
	public static final String DEFAULT_DROPDOWN_VALUE = "-1";

	/**
	 * A string representing '0' (Zero)
	 */
	public static final String ZERO = "0";

	private static final int HOURS_MAX_LIMIT = 23;
	private static final int MINUTES_MAX_LIMIT = 59;
	private static final int HOURS_MIN_LIMIT = 0;
	private static final int MINUTES_MIN_LIMIT = 0;
	private static final String DURATION_FORMAT = "00D:00H:00M";

	/**
	 * Default Decimal Number Format
	 */
	public static final int MAX_FRACTIONS_TO_BE_DISPLAYED = 2;
	public static final DecimalFormat PST_DEFAULT_DECIMAL_FORMAT = new DecimalFormat(
			"###,###,##0.00");
	public static final NumberFormat PST_DEFAULT_NUMBER_FORMAT = NumberFormat
			.getIntegerInstance();

	// ENCODING

	/**
	 * Returns a string that is the CSV encoding of the argument string, namely,
	 * the argument string enclosed in double-quotes, with any embedded double
	 * quotes escaped by an additional preceding double quote.
	 *
	 * @param string
	 *            the string to be encoded
	 *
	 * @return a string that is the CSV encoding of the argument string
	 */
	public static String csvEncode(String string) {
		if (string == null)
			return "null";
		StringBuffer stringBuffer = new StringBuffer(string.length());
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '"')
				stringBuffer.append('"');
			stringBuffer.append(string.charAt(i));
		}
		return doubleQuote(stringBuffer.toString());
	}

	/**
	 * Returns a string representation of the argument object, enclosed in
	 * double quotes.
	 *
	 * @param object
	 *            the object to be represented as a double-quoted string
	 *
	 * @return a string consisting of a double-quoted string representation of
	 *         the argument
	 */
	public static String doubleQuote(Object object) {
		return "\"" + object + "\"";
	}

	public static String doubleUnQuote(String string) {
		return (string == null) ? null : string.replaceAll("\"", "");
	}

	/**
	 * Returns a string representation of the argument object, enclosed in
	 * single quotes.
	 *
	 * @param object
	 *            the object to be represented as a single-quoted string
	 *
	 * @return a string consisting of a single-quoted string representation of
	 *         the argument
	 */
	public static String singleQuote(Object object) {
		return "'" + object + "'";
	}

	public static String singleUnQuote(String string) {
		return (string == null) ? null : string.replaceAll("'", "");
	}

	/**
	 * Returns a string that is the HTML decoding of the argument string, as
	 * specified in the following table:
	 *
	 * Argument | Encoded ---------+-------- < | &lt; > | &gt; & | &amp; " |
	 * &quot;
	 *
	 * @param string
	 *            the string to be encoded
	 *
	 * @return a string that is the HTML decoding of the argument string
	 */
	public static String xmlAttributeDecode(String string) {
		if (string == null)
			return new String();

		if (string.contains("&lt;")) {
			string = string.replaceAll("&lt;", "<");
		}
		if (string.contains("&gt;")) {
			string = string.replaceAll("&gt;", ">");
		}
		if (string.contains("&amp;")) {
			string = string.replaceAll("&amp;", "&");
		}
		if (string.contains("&quot;")) {
			string = string.replaceAll("&quot;", "\"");
		}

		return string;
	}

	/**
	 * Returns a string that is the HTML encoding of the argument string, as
	 * specified in the following table:
	 *
	 * Argument | Encoded ---------+-------- < | &lt; > | &gt; & | &amp; " |
	 * &quot;
	 *
	 * @param string
	 *            the string to be encoded
	 *
	 * @return a string that is the HTML encoding of the argument string
	 */
	public static String xmlAttributeEncode(String string) {
		if (string == null)
			return new String();
		StringBuffer stringBuffer = new StringBuffer(string.length());
		for (int i = 0; i < string.length(); i++)
			switch (string.charAt(i)) {
			case '<':
				stringBuffer.append("&lt;");
				break;
			case '>':
				stringBuffer.append("&gt;");
				break;
			case '&':
				stringBuffer.append("&amp;");
				break;
			case '"':
				stringBuffer.append("&quot;");
				break;
			default:
				stringBuffer.append(string.charAt(i));
			}
		return stringBuffer.toString();
	}

	// FORMATTING

	public static String indentByWith(String aString, int aNumberOfLevels,
			String aSequence) {
		StringBuffer aBuffer = new StringBuffer(aString);
		indentByWith(aBuffer, aNumberOfLevels, aSequence);
		return aBuffer.toString();
	}

	public static void indentByWith(StringBuffer aBuffer, int aNumberOfLevels,
			String aSequence) {
		for (int i = 0; i < aNumberOfLevels; i++)
			aBuffer.append(aSequence);
	}

	/**
	 * <p>
	 * Capitalizes a String changing the first letter to title case as per
	 * {@link Character#toTitleCase(char)}. No other letters are changed.
	 * </p>
	 *
	 * <p>
	 * For a word based algorithm, see {@link WordUtils#capitalize(String)}. A
	 * <code>null</code> input String returns <code>null</code>.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.capitalize(null)  = null
	 * StringUtils.capitalize("")    = ""
	 * StringUtils.capitalize("cat") = "Cat"
	 * StringUtils.capitalize("cAt") = "CAt"
	 * </pre>
	 *
	 * @param str
	 *            the String to capitalize, may be null
	 * @return the capitalized String, <code>null</code> if null String input
	 * @see WordUtils#capitalize(String)
	 * @see #uncapitalize(String)
	 */
	public static String capitalize(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		return new StringBuffer(strLen)
				.append(Character.toTitleCase(str.charAt(0)))
				.append(str.substring(1)).toString();
	}

	/**
	 * <p>
	 * Uncapitalizes a String changing the first letter to title case as per
	 * {@link Character#toLowerCase(char)}. No other letters are changed.
	 * </p>
	 *
	 * <p>
	 * For a word based algorithm, see {@link WordUtils#uncapitalize(String)}. A
	 * <code>null</code> input String returns <code>null</code>.
	 * </p>
	 *
	 * <pre>
	 * StringUtils.uncapitalize(null)  = null
	 * StringUtils.uncapitalize("")    = ""
	 * StringUtils.uncapitalize("Cat") = "cat"
	 * StringUtils.uncapitalize("CAT") = "cAT"
	 * </pre>
	 *
	 * @param str
	 *            the String to uncapitalize, may be null
	 * @return the uncapitalized String, <code>null</code> if null String input
	 * @see WordUtils#uncapitalize(String)
	 * @see #capitalize(String)
	 */
	public static String uncapitalize(String str) {
		int strLen;
		if (str == null || (strLen = str.length()) == 0) {
			return str;
		}
		return new StringBuffer(strLen)
				.append(Character.toLowerCase(str.charAt(0)))
				.append(str.substring(1)).toString();
	}

	// TRIMMING

	public static String trim(String string) {
		return (string == null) ? null : string.trim();
	}

	public static String trimAll(String string) {
		return (string == null) ? null : string.replaceAll(" ", "");
	}

	// SUBSTITUTION

	@SuppressWarnings("rawtypes")
	public static String substituteAll(String string, Map map) {
		String result = string;
		Iterator regexPatterns = map.keySet().iterator();
		while (regexPatterns.hasNext())
			;
		{
			String regexPattern = (String) regexPatterns.next();
			String replacement = (String) map.get(regexPattern);
			result = result.replaceAll(regexPattern, replacement);
		}
		return result;
	}

	// FACTORY METHODS

	public static String valueOf(Object object) {
		return (object == null) ? new String() : object.toString();
	}

	/**
	 * Returns a new String of the indicated length whose characters are all
	 * spaces.
	 *
	 * @param length
	 *            The desired length.
	 * @return a new String of the indicated length
	 */
	public static String newEmptyString(int length) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < length; i++)
			buffer.append(" ");
		return buffer.toString();
	}

	public static String newStringFilledWith(int length, String substring) {
		StringBuffer buffer = new StringBuffer();
		while (buffer.length() < length) {
			buffer.append(substring);
		}
		buffer.setLength(length);
		return buffer.toString();
	}

	// PADDING

	public static String leftJustifyIn(String aValue, String aMask) {
		if (isNullOrEmpty(aValue) || isNullOrEmpty(aMask))
			return aValue;
		StringBuffer builder = new StringBuffer(aMask);
		int start = 0;
		int end = start + aValue.length();
		builder.replace(start, end, aValue);
		return builder.toString();
	}

	public static String rightJustifyIn(String aValue, String aMask) {
		if (isNullOrEmpty(aValue) || isNullOrEmpty(aMask))
			return aValue;
		StringBuffer builder = new StringBuffer(aMask);
		int maskLength = aMask.length();
		int valueLength = aValue.length();
		int start = Math.max(0, maskLength - valueLength);
		int end = start + valueLength;
		builder.replace(start, end, aValue);
		return builder.toString();
	}

	public static String centerJustifyIn(String aValue, String aMask) {
		if (isNullOrEmpty(aValue) || isNullOrEmpty(aMask))
			return aValue;
		StringBuffer builder = new StringBuffer(aMask);
		int maskLength = aMask.length();
		int valueLength = aValue.length();
		int start = Math.max(0, (maskLength / 2) - (valueLength / 2));
		int end = start + valueLength;
		builder.replace(start, end, aValue);
		return builder.toString();
	}

	// TESTING

	/**
	 * Returns true if a String is null or empty.
	 *
	 * @param string
	 *            the String to check
	 * @return boolean true if
	 *         <code> string == null || string.length() < 1 </code>
	 */
	public static boolean isNullOrEmpty(String string) {
		return string == null || string.equals("null")
				|| string.trim().length() < 1;
	}

	/**
	 * Returns true if a String is not null and not empty.
	 *
	 * @param string
	 *            the String to check
	 * @return boolean false if
	 *         <code> string == null || string.length() < 1 </code>
	 */
	public static boolean notNullOrEmpty(String string) {
		return !isNullOrEmpty(string);
	}

	/**
	 * Compares two String instances, allowing null in either or both strings.
	 * If both strings are null, they are considered equal. If only one string
	 * is null, the non-null string is considered greater. If both strings are
	 * non-null, this method returns the results of string1.compareTo(string2).
	 *
	 * @param string1
	 *            a String instance to compare. May be null.
	 * @param string2
	 *            a String instance to compare. May be null.
	 * @return <0 if string1 < string2, 0 if string1.equals(string2), >0 if
	 *         string1 > string2.
	 */
	public static int compareTo(String string1, String string2) {
		int result = 0;

		if (string1 != null && string2 == null)
			result = 1;
		else if (string1 == null && string2 != null)
			result = -1;
		else if (string1 != null && string2 != null)
			result = string1.compareTo(string2);

		return result;
	}

	/**
	 * Checks two string instances for equality, allowing for null in either or
	 * both strings. If both strings are null, they are considered equal.
	 *
	 * @param string1
	 *            a String instance to compare. May be null.
	 * @param string2
	 *            a String instance to compare. May be null.
	 * @return true if the String instances are lexically equal. Otherwise
	 *         false.
	 * @see compareTo(String, String)
	 */
	public static boolean areEqual(String string1, String string2) {
		return (compareTo(string1, string2) == 0);
	}

	public static boolean notEqual(String string1, String string2) {
		return !areEqual(string1, string2);
	}

	/**
	 * Checks two string instances for equality without case sensitivity,
	 * allowing for null in either or both strings. If both strings are null,
	 * they are considered equal.
	 *
	 * @param string1
	 *            a String instance to compare. May be null.
	 * @param string2
	 *            a String instance to compare. May be null.
	 * @return true if the String instances are lexically equal. Otherwise
	 *         false.
	 * @see compareTo(String, String)
	 */
	public static boolean areEqualIgnoringCase(String string1, String string2) {
		return (compareTo(string1.toLowerCase(), string2.toLowerCase()) == 0);
	}

	/**
	 * Appends the String argument to the StringBuffer, followed by the
	 * delimiter, if the String argument is not null or empty.
	 *
	 * @param element
	 *            the String to append
	 * @param delimiter
	 *            the delimiter String to append following the element String
	 * @param buffer
	 *            the buffer to append to
	 */
	public static boolean appendElement(String element, String delimiter,
			StringBuffer buffer) {
		if (notNullOrEmpty(element)) {
			buffer.append(element);
			buffer.append(delimiter);
			return true;
		}
		return false;
	}

	public static boolean isNumeric(String aString) {
		if (isNullOrEmpty(aString))
			return false;
		CharacterIterator i = new StringCharacterIterator(aString);
		for (char c = i.first(); c != CharacterIterator.DONE; c = i.next()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public static boolean isAlphabetic(String aString) {
		if (isNullOrEmpty(aString))
			return false;
		CharacterIterator i = new StringCharacterIterator(aString);
		for (char c = i.first(); c != CharacterIterator.DONE; c = i.next()) {
			if (!Character.isLetter(c))
				return false;
		}
		return true;
	}

	public static boolean isAlphaNumeric(String aString) {
		if (isNullOrEmpty(aString))
			return false;
		CharacterIterator i = new StringCharacterIterator(aString);
		for (char c = i.first(); c != CharacterIterator.DONE; c = i.next()) {
			if (!Character.isLetter(c) && !Character.isDigit(c) && c != SPACE)
				return false;
		}
		return true;
	}

	/**
	 * Returns true if aString is numeric or containing one dot (decimal).
	 *
	 * @param aString
	 *            a String to check for emptiness
	 * @return true if the string is numeric or decimal.
	 */
	public static boolean isNumericOrDecimal(String aString) {
		if (isNullOrEmpty(aString))
			return false;
		CharacterIterator i = new StringCharacterIterator(aString);
		int dotOccurrence = 0;
		for (char c = i.first(); c != CharacterIterator.DONE; c = i.next()) {
			if (!Character.isDigit(c) && c == PERIOD) {
				dotOccurrence += 1;
				if (dotOccurrence == 2)
					return false;
			} else if (!Character.isDigit(c) && c != PERIOD)
				return false;
		}
		return true;
	}

	/**
	 * Returns true only if aString is not null and is of zero length after
	 * trimming.
	 *
	 * @param aString
	 *            a String to check for emptiness
	 * @return true if the string is empty, but not null.
	 */
	public static boolean isEmpty(String aString) {
		return (aString != null && aString.trim().length() == 0);
	}

	public static boolean containsOnlyValidDecimalCharacters(String aString) {
		if (isNullOrEmpty(aString))
			return false;
		CharacterIterator i = new StringCharacterIterator(aString);
		for (char c = i.first(); c != CharacterIterator.DONE; c = i.next()) {
			if (!Character.isDigit(c) && (c != '.') && (c != '-'))
				return false;
		}
		return true;
	}

	public static boolean isOfLength(String aString, int aLength) {
		return notNullOrEmpty(aString) && aString.length() == aLength;
	}

	public static boolean islengthInRange(String string, int minLength,
			int maxLength) {
		boolean result = false;

		if (string != null) {
			int length = string.trim().length();
			result = (length >= minLength && length <= maxLength);
		}

		return result;
	}

	public static String[] parseString(String string, String delimiter) {
		String delim = (delimiter.equals(".") ? "\\." : delimiter);
		String[] strings = string.split(delim);

		if (strings.length == 0 && string.length() > 0) {
			strings = new String[] { string };
		}

		return strings;
	}

	public static String toCamelCase(String stringValue, String delimiter) {
		StringBuffer result = new StringBuffer(stringValue.length());
		String[] strings = parseString(stringValue.toLowerCase(), delimiter);

		for (int i = 0; i < strings.length; ++i) {
			char[] characters = strings[i].toCharArray();

			if (characters.length > 0) {
				characters[0] = Character.toUpperCase(characters[0]);
				result.append(characters);
			}
		}

		return result.toString();
	}

	/**
	 * Converts a camel case string into a lower-case, delimiter-separated
	 * string. For example, a call of toSeparatedString("ACamelCaseString, '_')
	 * returns a_camel_case_string.
	 *
	 * @param camelCaseString
	 *            a String in camel case to delimit.
	 * @param delimiter
	 *            the character used to separate the string.
	 * @return a String where capitals in the original are prefixed with the
	 *         given delimiter.
	 */
	public static String toSeparatedString(String camelCaseString,
			char delimiter) {
		CharArrayWriter result = new CharArrayWriter();
		char[] chars = camelCaseString.toCharArray();

		for (int i = 0; i < chars.length; ++i) {
			if (chars[i] != delimiter && Character.isUpperCase(chars[i])
					&& i > 0) {
				if ((i < chars.length - 1)
						&& (!Character.isUpperCase(chars[i + 1]) && chars[i + 1] != delimiter)
						&& chars[i - 1] != delimiter)
					result.write(delimiter);
				else if (!Character.isUpperCase(chars[i - 1])
						&& chars[i - 1] != delimiter)
					result.write(delimiter);
			}

			result.write(chars[i]);
		}

		return result.toString().toLowerCase();
	}

	/**
	 * Converts the string (presumably a single word) from plural into singular
	 * form.
	 *
	 * @param word
	 *            a single word in plural form.
	 * @return the singular form of the word.
	 */
	@SuppressWarnings("rawtypes")
	public static String singularize(String word) {
		if (isNullOrEmpty(word))
			return word;

		final Map<String, String> rules = getSingularizationRules();

		for (Entry rule : rules.entrySet()) {
			final String pattern = rule.getKey().toString();
			final String replacement = rule.getValue().toString();

			if (word.matches(pattern)) {
				return word.replaceFirst(pattern, replacement);
			}
		}

		return word.replaceFirst("([\\w]+)s$", "$1");
	}

	/**
	 * Converts the string (presumably a single word) from singular into plural
	 * form.
	 *
	 * @param word
	 *            a single word in singular form.
	 * @return the plural form of the word.
	 */
	@SuppressWarnings("rawtypes")
	public static String pluralize(String word) {
		if (isNullOrEmpty(word))
			return word;

		final Map<String, String> rules = getPluralizationRules();

		for (Entry rule : rules.entrySet()) {
			final String pattern = rule.getKey().toString();
			final String replacement = rule.getValue().toString();

			if (word.matches(pattern)) {
				return word.replaceFirst(pattern, replacement);
			}
		}

		return word.replaceFirst("([\\w]+)([^s])$", "$1$2s");
	}

	/**
	 * Truncates the string (presumably a large amount of text) down to the
	 * first whole word after the length parameter and then adds an ellipsis
	 * (...) if there is additional text.
	 *
	 * @param longString
	 *            a block of text
	 * @param length
	 *            the desired length of the resulting text string
	 * @return
	 */
	public static String truncateWithEllipsis(String longString, int length) {
		if (notNullOrEmpty(longString)) {
			// check length of text and cut off at first whole word after
			// specified nbr of characters (Jim's bytes)
			if (longString.length() > length) {
				boolean checkingForWholeWord = true;
				int characterPosition = length;

				while (checkingForWholeWord) {
					if ((longString.length() > characterPosition)
							&& (notNullOrEmpty(String.valueOf(longString
									.charAt(characterPosition))))) {
						characterPosition++;
					} else {
						checkingForWholeWord = false;
					}
				}

				String continuationIndicator = EMPTY_STRING;
				if (longString.length() > characterPosition) {
					continuationIndicator = "...";
				}

				longString = longString.substring(0, characterPosition)
						+ continuationIndicator;
			}
		}

		return longString;
	}

	/**
	 * @param aList
	 * @return a comma separated {@link String} of the List's Objects
	 */
	public static String listToDelimitedString(List<String> aList,
			String delimiter) {
		StringBuilder sb = new StringBuilder();
		if (aList != null) {
			for (int i = 0; i < aList.size(); i++) {
				sb.append(aList.get(i));
				if (i != aList.size() - 1) {
					sb.append(delimiter);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Converts argument's fractional part (after decimal) into two digits
	 * format. Example: If strValue is 9.9 then return value will be 9.90
	 *
	 * @param strValue
	 *            value in string form.
	 * @return string with two digits fractional value.
	 */
	public static String currencyFormat(String strValue) {
		StringBuilder result = new StringBuilder();
		if (isNumericOrDecimal(strValue)) {
			result.append(strValue);
			int decimalIndex = strValue.indexOf(PERIOD);
			if (decimalIndex != -1 && decimalIndex + 1 <= strValue.length()) {
				String digitsAfterDecimal = strValue
						.substring(decimalIndex + 1);
				if (digitsAfterDecimal.length() == 1) {
					result.append(ZERO);
				}
			}
		}
		return result.toString();
	}

	/**
	 * Validates precision and scale values of a decimal string value.
	 *
	 * @param strValue
	 *            a decimal value to be validated
	 * @param precision
	 *            an integer value
	 * @param scale
	 *            an integer value
	 * @return boolean if strValue is within the given precision & scale values.
	 */
	public static boolean validatePrecisionScale(String strValue,
			int precision, int scale) {
		if (precision <= scale)
			return false;

		boolean result = false;
		if (StringUtils.isNumericOrDecimal(strValue)) {
			result = true;
			int maxIntegerDigits = (precision - scale);
			int maxFractionalDigits = scale;
			int indexOfDecimal = strValue.indexOf(".");
			if (indexOfDecimal == -1 && strValue.length() > maxIntegerDigits) {
				result = false;
			} else if (indexOfDecimal > -1) {
				result = true;
				String integerDigits = strValue.substring(0, indexOfDecimal);
				String fractionalDigits = strValue.substring(
						indexOfDecimal + 1, strValue.length());
				if ((integerDigits.length() > maxIntegerDigits)
						|| (fractionalDigits.length() > maxFractionalDigits)) {
					result = false;
				}
			}
		}
		return result;
	}

	// ASPECT PATH

	public static String appendAspectToPath(String anAspect, String anAspectPath) {
		StringBuilder builder = new StringBuilder();
		if (notNullOrEmpty(anAspectPath)) {
			builder.append(anAspectPath);
			builder.append(ASPECT_PATH_SEPARATOR);
		}
		if (notNullOrEmpty(anAspect)) {
			builder.append(anAspect);
		}
		return builder.toString();
	}

	// UTILITY

	private static Map<String, String> getPluralizationRules() {
		final Map<String, String> rules = new LinkedHashMap<String, String>();

		/* handle < 0.01% of the special cases...your mileage may vary */
		rules.put("(\\w*)person$", "$1people");
		rules.put("(\\w*)child$", "$1children");
		rules.put("(\\w*)series$", "$1series");
		rules.put("(\\w*)foot$", "$1feet");
		rules.put("(\\w*)tooth$", "$1teeth");
		rules.put("(\\w*)bus$", "$1buses");
		rules.put("(\\w*)man$", "$1men");

		/* handle some more general cases */
		rules.put("(\\w+)(x|ch|ss|sh)$", "$1$2es");
		rules.put("(\\w+)([^aeiou])y$", "$1$2ies");
		rules.put("(\\w*)(f)$", "$1ves");
		rules.put("(\\w*)(fe)$", "$1ves");
		rules.put("(\\w+)(sis)$", "$1ses");

		return rules;
	}

	private static Map<String, String> getSingularizationRules() {
		final Map<String, String> rules = new LinkedHashMap<String, String>();

		/* handle < 0.01% of the special cases...your mileage may vary */
		rules.put("(\\w*)people$", "$1person");
		rules.put("(\\w*)children$", "$1child");
		rules.put("(\\w*)series$", "$1series");
		rules.put("(\\w*)feet$", "$1foot");
		rules.put("(\\w*)teeth$", "$1tooth");
		rules.put("(\\w*)buses$", "$1bus");
		rules.put("(\\w*)men$", "$1man");

		/* handle some more general cases */
		rules.put("(\\w+)(x|ch|ss)es$", "$1$2");
		rules.put("(\\w+)([^aeiou])ies", "$1$2y");
		rules.put("(\\w+)([^l])ves", "$1$2fe");
		rules.put("(\\w+)([ll])ves", "$1$2f");
		rules.put("(\\w+)(ses)$", "$1sis");

		return rules;
	}

	public static String stringArrayToStringWithDelimiter(String[] a,
			String separator) {
		StringBuffer result = new StringBuffer();
		if (a != null && a.length > 0) {
			result.append(a[0]);
			for (int i = 1; i < a.length; i++) {
				result.append(separator);
				result.append(a[i]);
			}
		}
		return result.toString();
	}

	// STRING VALIDATIONS
	/**
	 * Method to check if the input phoneNumber is in Phone Number Format: Only
	 * 10 digits entered.
	 *
	 * @param phoneNumber
	 * @return boolean
	 */
	public static boolean isTenDigitPhoneNumberFormat(String phoneNumber) {
		return (notNullOrEmpty(phoneNumber) && phoneNumber.length() == 10 && isNumeric(phoneNumber));
	}

	public static boolean is4Digit24HourTimeFormat(String inputTime) {
		if (StringUtils.isNumeric(inputTime) && inputTime.length() == 4) {
			int hours = Integer.parseInt(inputTime.substring(0, 2));
			int minutes = Integer.parseInt(inputTime.substring(2));

			if (hours >= HOURS_MIN_LIMIT && hours <= HOURS_MAX_LIMIT
					&& minutes >= MINUTES_MIN_LIMIT
					&& minutes <= MINUTES_MAX_LIMIT) {
				return true;
			}
		}
		return false;
	}

	public static boolean isMinuteFormat(String minutes) {
		if (StringUtils.isNumeric(minutes) && minutes.length() == 2) {
			int min = Integer.parseInt(minutes);
			if (min >= MINUTES_MIN_LIMIT && min <= MINUTES_MAX_LIMIT) {
				return true;
			}
		}
		return false;
	}

	public static boolean isHourFormat(String hours) {
		if (StringUtils.isNumeric(hours) && hours.length() == 2) {
			int hrs = Integer.parseInt(hours);
			if (hrs >= HOURS_MIN_LIMIT && hrs <= HOURS_MAX_LIMIT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks input string's duration format which should be in ##D:##H:##M.
	 *
	 * @param String
	 *            duration
	 * @return boolean
	 */
	public static boolean isDurationFormat(String duration) {
		char[] validCharArray = DURATION_FORMAT.toCharArray();

		if (StringUtils.notNullOrEmpty(duration)) {
			int i = 0;
			CharacterIterator ci = new StringCharacterIterator(duration);

			for (char c = ci.first(); c != CharacterIterator.DONE; c = ci
					.next(), ++i) {
				if ((i == 0 || i == 1 || i == 4 || i == 5 || i == 8 || i == 9)
						&& !Character.isDigit(c)
						&& Character.isDigit(validCharArray[i])) {
					return false;
				} else if ((i == 2 || i == 6 || i == 10)
						&& c != validCharArray[i]) {
					return false;
				} else if ((i == 3 || i == 7) && c != COLON
						&& validCharArray[i] == COLON) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Converts duration components into string array. Example: 10D:05H:30M will
	 * be converted into String [] without D, H & M characters
	 *
	 * @param String
	 *            duration
	 * @return String []
	 */
	public static String[] getDurationAsStrings(String validDuration) {
		String daysHoursMinutes = validDuration;
		daysHoursMinutes = daysHoursMinutes.replaceFirst("D", "");
		daysHoursMinutes = daysHoursMinutes.replaceFirst("H", "");
		daysHoursMinutes = daysHoursMinutes.replaceFirst("M", "");

		return daysHoursMinutes.split(":");
	}

	public static List<String> getListFromStringWithSeperator(String value,
			char separator) {
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(value,
				String.valueOf(separator));
		while (tokens.hasMoreTokens()) {
			list.add((String) tokens.nextElement());
		}

		return list;
	}

	public static String[] getStringArrayFromStringWithSeperator(String value,
			char separator) {
		StringTokenizer tokens = new StringTokenizer(value,
				String.valueOf(separator));
		int tokenCount = tokens.countTokens();
		String[] array = new String[tokenCount];
		for (int i = 0; i < tokenCount; i++) {
			array[i] = tokens.nextToken();
		}
		return array;
	}

	/**
	 * Returns a value along with trailing blank spaces according to the
	 * maxCharLength specified & ensuring that the string value has specified
	 * length.
	 *
	 * @param string
	 *            value where trailing spaces needs to be appended
	 * @param int value that specifies the actual length of the value
	 * @return a string in specified length
	 */
	public static String getValueWithTrailingSpaces(String value,
			int maxCharLength) {
		String trailingSpaces = StringUtils.EMPTY_STRING;
		String result = value;

		if (result == null) {
			result = StringUtils.indentByWith(trailingSpaces, maxCharLength,
					" ");
		} else if (result != null && result.length() < maxCharLength) {
			trailingSpaces = indentByWith(trailingSpaces,
					(maxCharLength - result.length()), " ");
			result = result + trailingSpaces;
		}

		return result;
	}

	static public String customFormatForDouble(double value) {
		DecimalFormat myFormatter = PST_DEFAULT_DECIMAL_FORMAT;
		return myFormatter.format(value);
	}

	static public String customFormatForLong(long value) {
		NumberFormat myFormatter = PST_DEFAULT_NUMBER_FORMAT;
		return myFormatter.format(value);
	}

	static public String customFormatForDouble(String pattern, double value) {
		DecimalFormat myFormatter = null;
		if (StringUtils.isNullOrEmpty(pattern)) {
			myFormatter = PST_DEFAULT_DECIMAL_FORMAT;
		} else {
			myFormatter = new DecimalFormat(pattern);
		}
		return myFormatter.format(value);

	}

	static public String localizedFormatForDouble(String pattern, double value,
			Locale loc) {
		NumberFormat nf = NumberFormat.getNumberInstance(loc);
		DecimalFormat df = (DecimalFormat) nf;
		df.applyPattern(pattern);
		return df.format(value);

	}

	/**
	 * Utility method to format a number into localized string.
	 *
	 * @param Number
	 *            aNumber
	 * @return a String of formatted number
	 */
	public static final String formatNumber(final Number aNumber) {
		return PST_DEFAULT_NUMBER_FORMAT.format((aNumber == null) ? Integer
				.valueOf(0) : aNumber);
	}

	/**
	 * Utility method to format a number into localized string with max fraction
	 * value (default 2).
	 *
	 * @param Number
	 *            aNumber
	 * @return a String of formatted number
	 */
	public static final String formatDecimalNumber(final Number aNumber) {
		NumberFormat decimalFormat = NumberFormat.getNumberInstance();
		decimalFormat.setMaximumFractionDigits(MAX_FRACTIONS_TO_BE_DISPLAYED);
		return decimalFormat.format((aNumber == null) ? Integer.valueOf(0)
				: aNumber);
	}

	/**
	 * Utility method to format a number into localized string with max fraction
	 * value (default 2).
	 *
	 * @param Number
	 *            aCurrencyValue
	 * @return a String of formatted number
	 */
	public static final String formatCurrency(final Number aCurrencyValue) {
		return NumberFormat.getCurrencyInstance().format(
				(aCurrencyValue == null) ? Integer.valueOf(0) : aCurrencyValue);
	}

	/**
	 * Utility method to test validity of a FHIR id data type
	 *
	 * @param id
	 * @return boolean - true, valid; false, invalid
	 */
	public static final boolean isValidFhirId(String id) {
		boolean valid = false;

		if (id != null && !StringUtils.isEmpty(id)) {
			valid = id.matches("[A-Za-z0-9\\-\\.]{1,64}");
		}

		return valid;
	}

}
