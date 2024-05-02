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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

import org.hl7.fhir.r4.model.Duration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Date utility for UTC dates.
 *
 * @author richard.ettema
 *
 */
public class UTCDateUtil {

	private Logger log = Logger.getLogger("UTCDateUtil");

	public static final String DATE_ONLY_FORMAT_UTC = "d MMM yyyy";
	public static final String DATE_FORMAT_UTC = "EEE, d MMM yyyy HH:mm:ss";
	public static final String DATE_FORMAT_FULL = DATE_FORMAT_UTC + "z";
	public static final String DATE_ONLY_PARAMETER_FORMAT = "yyyy-MM-dd";
	public static final String DATE_PARAMETER_FORMAT = "yyyy-MM-dd'T'hh:mm:ssXXX";
	public static final String DATE_PARAMETER_MILLI_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSSXX";
	public static final String DATE_SORT_FORMAT = "yyyyMMdd";
	public static final String DATETIME_SORT_FORMAT = "yyyyMMddHHmmss";

	public static final String TIME_ZONE_UTC = "GMT-0:00";

	/*
	 * HTTP Date formats
	 */
	public static final String PATTERN_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	public static final String PATTERN_RFC1036 = "EEEE, dd-MMM-yy HH:mm:ss zzz";
	public static final String PATTERN_ASCTIME = "EEE MMM d HH:mm:ss yyyy";
	public static final String PATTERN_ASCTIME_RESTEASY = "EEE, d MMM yyyy HH:mm:ss'GMT'";

	private static final Collection<String> DEFAULT_PATTERNS = Arrays.asList(
			new String[] {PATTERN_ASCTIME_RESTEASY, PATTERN_ASCTIME, PATTERN_RFC1036, PATTERN_RFC1123});

	private static final Date DEFAULT_TWO_DIGIT_YEAR_START;

	static {
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, Calendar.JANUARY, 1, 0, 0);
		DEFAULT_TWO_DIGIT_YEAR_START = calendar.getTime();
	}

	/**
	 * Returns the elapsed time as a formatted string
	 *
	 * @param startMillis
	 * @return
	 */
	public String getElapsedTime(long startMillis) {
		long different = System.currentTimeMillis() - startMillis;

		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		long elapsedDays = different / daysInMilli;
		different = different % daysInMilli;

		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;

		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;

		long elapsedSeconds = different / secondsInMilli;

		long elapsedMillis = different % secondsInMilli;

		return (elapsedDays > 0 ? elapsedDays + " day" : "") + (elapsedDays > 1 ? "s" : "") +
				(elapsedHours > 0 ? elapsedHours + " hr" : "") + (elapsedHours > 1 ? "s" : "") +
				(elapsedMinutes > 0 ? " " + elapsedMinutes + " min" : "") + (elapsedMinutes > 1 ? "s" : "") +
				(elapsedSeconds > 0 ? " " + elapsedSeconds + " sec" : "") + (elapsedSeconds > 1 ? "s" : "") +
				(elapsedMillis > 0 ? " " + elapsedMillis + " millis " : "");
	}

	public ZoneId getTimeZoneId(String dateString) throws Exception {
		String timeZoneId = "GMT"; // default to GMT

		if (dateString != null && dateString.length() > 16) {
			String timeString = dateString.substring(16);

			if (timeString.contains("GMT") || timeString.contains("Z")) {
				// Use already set default
			}
			else if (timeString.contains("-") || timeString.contains("+")) {
				int timeZonePos = -1;
				if (timeString.contains("-")) {
					timeZonePos = timeString.indexOf("-");
				}
				else if (timeString.contains("+")) {
					timeZonePos = timeString.indexOf("+");
				}

				if (timeZonePos > -1) {
					timeZoneId = timeString.substring(timeZonePos);
				}
			}
		}

		ZoneId zoneId = null;
		try {
			zoneId = ZoneId.of(timeZoneId);
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			return null;
		}

		return zoneId;
	}

	public boolean hasTimeZone(String dateString) throws Exception {
		boolean result = false;

		if (dateString != null && dateString.length() > 16) {
			String timeString = dateString.substring(16);
			if (timeString.contains("GMT") || timeString.contains("Z") || timeString.contains("-") || timeString.contains("+")) {
				result = true;
			}
		}

		return result;
	}

	public Date parseXMLDate(String dateString) throws Exception {

		DateTimeFormatter XML_DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
		Date parsedDate = null;

		try {
			DateTime dt = XML_DATE_TIME_FORMAT.parseDateTime(dateString);
			parsedDate = dt.toDate();
		}
		catch (Exception e) {
			log.severe("Exception parsing XML Date string! " + e.getMessage());
			throw e;
		}

		return parsedDate;
	}

	/**
	 * Parse a date string as a UTC date. The date may optionally have a time zone which will be used if present.
	 *
	 * @param dateString
	 *            Date string to parse
	 * @return Parsed date
	 */
	public Date parseUTCDateOptionalTimeZone(String dateString) throws Exception {
		return parseDate(dateString, DATE_FORMAT_FULL, TimeZone.getTimeZone(TIME_ZONE_UTC));
	}

	/**
	 * Format a date using a UTC format.
	 *
	 * @param sourceDate
	 *            Date to
	 * @return
	 */
	public String formatUTCDate(Date sourceDate) {
		return formatDate(sourceDate, DATE_FORMAT_UTC);
	}

	/**
	 * Format a date using a UTC format with time zone offset.
	 *
	 * @param sourceDate
	 *            Date to
	 * @return
	 */
	public String formatUTCDateOffset(Date sourceDate) {
		return formatDate(sourceDate, DATE_FORMAT_FULL);
	}

	/**
	 * Format a date using a UTC date portion only format.
	 *
	 * @param sourceDate
	 *            Date to
	 * @return
	 */
	public String formatUTCDateOnly(Date sourceDate) {
		return formatDate(sourceDate, DATE_ONLY_FORMAT_UTC);
	}

	/**
	 * Parses a string and returns a Date object having the given date format.
	 *
	 * @param dateString
	 *            String to be parsed containing a date
	 * @param dateFormat
	 *            Format of the date to be parsed
	 * @return Returns the Date object for the given string and format.
	 */
	public Date parseDate(String dateString, String dateFormat, TimeZone timeZone) throws Exception {
		// Candidate to move to a super class for other format types
		log.fine("Parsing (" + dateString + ") using format string (" + dateFormat + ") and time zone (" + ((timeZone == null) ? "none" : timeZone.getDisplayName()) + ").");

		Date parsed = null;
		if ((dateString != null) && (dateFormat != null)) {
			try {
				// Special handling when dateFormat equals DATE_PARAMETER_FORMAT and date string contains milliseconds
				if (dateFormat.equals(DATE_PARAMETER_FORMAT) && dateString.contains(".")) {
					dateFormat = DATE_PARAMETER_MILLI_FORMAT;
					log.fine("Date Format equals DATE_PARAMETER_FORMAT and date string contains milliseconds!");
				}

				String newDateString = prepareDateString(dateFormat, dateString);
				String newDateFormat = prepareDateFormatString(dateFormat, newDateString);
				log.fine("Prepared Date String (" + newDateString + "); Prepared Date Format (" + newDateFormat + ")");

				// If timeZone is null, determine TimeZone from dateString if present
				if (timeZone == null && hasTimeZone(dateString)) {
					ZoneId zoneId = getTimeZoneId(dateString);
					if (zoneId != null) {
						timeZone = TimeZone.getTimeZone(zoneId);
						log.fine("Time zone from date string (" + ((timeZone == null) ? "none" : timeZone.getDisplayName()) + ").");
					}
				}

				DateFormat formatter = createDateFormatter(newDateFormat, timeZone);
				parsed = formatter.parse(newDateString);
				if (parsed != null) {
					log.fine("Date parsed successfully");
				}
			}
			catch (Throwable t) {
				log.severe("Error parsing '" + dateString + "' using format: '" + dateFormat + "'; exception caught: " + t.getMessage());
				//throw t;
				parsed = null;
			}
		}
		return parsed;
	}

	/**
	 * Create a date formatter.
	 *
	 * @param formatString
	 *            Date format string
	 * @param timeZone
	 *            Optional time zone. Not used if null.
	 * @return Prepared date formatter
	 */
	private DateFormat createDateFormatter(String formatString, TimeZone timeZone) {
		// Candidate to move to a super class for other format types
		if (NullChecker.isNullish(formatString)) {
			throw new IllegalArgumentException("Date format string is required to create a date formatter");
		}
		DateFormat formatter = new SimpleDateFormat(formatString);
		if (timeZone != null) {
			formatter.setTimeZone(timeZone);
		}
		return formatter;
	}

	/**
	 * Prepare a date format string based on the length of the date string to be parsed. The date Format will be reduced
	 * to meet the length of the date provided to match the accuracy of the date string.
	 *
	 * @param dateFormat
	 *            Date format string (ex. yyyyMMdd'T'hhmmssZ)
	 * @param dateString
	 *            Date string to be parsed (ex. 19990205)
	 * @return Modified format string based on the date string length (ex. yyyyMMdd)
	 */
	private String prepareDateFormatString(String dateFormat, String dateString) {
		// Candidate to move to a super class for other format types
		String newDateFormat = dateFormat;

		// Special handling of 'T'
		if ((dateFormat != null) && (dateFormat.length() > 0) && (dateFormat.indexOf("'T'") > -1)) {
			dateFormat = dateFormat.replaceAll("'", "");
		}

		if ((dateString != null) && (dateFormat != null) && (dateString.length() > 0) && (dateString.length() < dateFormat.length())) {

			newDateFormat = dateFormat.substring(0, dateString.length());

			// Special handling of T
			if (newDateFormat.indexOf("T") > -1) {
				newDateFormat = newDateFormat.replaceAll("T", "'T'");
			}

			log.fine("New dateFormat: " + newDateFormat);
		}
		return newDateFormat;
	}

	/**
	 * Prepare a date string based on the length of the date format string used for parsing. The date string will be reduced
	 * to meet the length of the format provided to match the accuracy of the date format string.
	 *
	 * @param dateFormat
	 *            Date format string (ex. yyyyMMdd'T'hhmmssZ)
	 * @param dateString
	 *            Date string to be parsed (ex. 19990205)
	 * @return Modified date string based on the date format string length (ex. 20190425)
	 */
	private String prepareDateString(String dateFormat, String dateString) {
		// Candidate to move to a super class for other format types
		String newDateString = dateString;

		if ((dateString != null) && (dateFormat != null) && (dateString.length() > 0) && (dateString.length() < 25)) {

			if (dateFormat.length() < dateString.length()) {
				newDateString = dateString.substring(0, dateFormat.length());
			}
			else if ((dateString.length() > 3) && (dateFormat.length() > dateString.length())) {

				int length = dateString.length();

				// 2022-01-01T00:00:00
				// 1234567890123456789
				if (length >= 4 && length <= 6) {
					newDateString = dateString.substring(0, 4) + "-01-01T00:00:00";
				}
				else if (length >= 7 && length <= 9) {
					newDateString = dateString.substring(0, 7) + "-01T00:00:00";
				}
				else if (length >= 10 && length <= 15) {
					newDateString = dateString.substring(0, 10) + "T00:00:00";
				}
				else if (length >= 16 && length <= 18) {
					newDateString = dateString.substring(0, 16) + ":00";
				}
				else {
					newDateString = dateString;
				}

				if (dateFormat.length() < newDateString.length()) {
					newDateString = dateString.substring(0, dateFormat.length());
				}
			}

			log.fine("New dateString: " + newDateString);
		}

		return newDateString;
	}

	/**
	 * Format a date using the specified format.
	 *
	 * @param sourceDate
	 * @param formatString
	 * @param timeZone
	 * @return
	 */
	public String formatDate(Date sourceDate, String formatString) {
		return formatDate(sourceDate, formatString, TimeZone.getTimeZone(TIME_ZONE_UTC));
	}

	public String formatDate(Date sourceDate, String formatString, TimeZone timeZone) {
		return formatDate(sourceDate, formatString, timeZone, -1);
	}

	public String formatDate(Date sourceDate, String formatString, TimeZone timeZone, int formatLength) {
		// Candidate to move to a super class for other format types
		log.fine("Formatting (" + (sourceDate == null ? "null" : sourceDate.toString()) + ") using format string (" + formatString + ") and time zone (" + ((timeZone == null) ? "none" : timeZone.getDisplayName()) + ").");

		String formatted = "";
		if (sourceDate != null) {
			try {
				DateFormat dateFormatter = createDateFormatter(formatString, timeZone);
				formatted = dateFormatter.format(sourceDate);
				log.fine("Date formatted successfully '" + formatted + "'");
			}
			catch (Throwable t) {
				t.printStackTrace();
				log.severe("Failed to format a date (" + ((sourceDate == null) ? "null" : sourceDate.toString()) + ") to a formatted string using the format '" + formatString + "': " + t.getMessage());
			}
		}
		if (!formatted.isEmpty() && formatLength > 0 && formatted.length() > formatLength) {
			formatted = formatted.substring(0, formatLength);
			log.fine("Date re-formatted successfully '" + formatted + "'");
		}
		return formatted;
	}

	/**
	 * Used in search logic when formatting date criteria values
	 * Note - stored date search parameter values are either 8 or 14 characters in length
	 *
	 * @param dateString
	 * @return
	 */
	public Integer computeSortFormatLength(String dateString) {
		Integer sortFormatLength = Integer.valueOf(0);

		if (dateString != null) {
			int length = dateString.length();

			if (length <= 5) {
				sortFormatLength = 4;
			}
			else if (length <= 7) {
				sortFormatLength = 6;
			}
			else if (length <= 11) {
				sortFormatLength = 8;
			}
			else if (length <= 17) {
				sortFormatLength = 12;
			}
			else {
				sortFormatLength = 14;
			}

			log.fine("computeSortFormatLength - dateString (" + dateString + "); length (" + length + "); sortFormatLength (" + sortFormatLength + ")");
		}

		return sortFormatLength;
	}

	/**
	 * Return a person's age based on the current date and their birth date
	 *
	 * @param birthDate
	 * @return
	 */
	public Integer calculateAge(Date birthDate) {
		Integer age = Integer.valueOf(0);

		if (birthDate != null) {
			LocalDate birthdate = new LocalDate(birthDate); // Birth date
			LocalDate now = new LocalDate(); // Today's date
			Period period = new Period(birthdate, now, PeriodType.yearMonthDay());

			if (period.getYears() > 0) {
				age = Integer.valueOf(period.getYears());
			}
		}

		return age;
	}

	public Date calculateAgeStartDate(Integer ageValue) {
		DateTime today = new DateTime();

		int startAgeValue = ((ageValue.intValue() + 1) * -1);

		DateTime startAgeDateTime = today.plusYears(startAgeValue);

		return startAgeDateTime.toDate();
	}

	public Date calculateAgeEndDate(Integer ageValue) {
		DateTime today = new DateTime();

		int endtAgeValue = (ageValue.intValue() * -1);

		DateTime endAgeDateTime = today.plusYears(endtAgeValue);

		return endAgeDateTime.toDate();
	}

	/**
	 * Calculate the Period.end Date value from the Period.start and the specified Duration
	 *
	 * Allowed Duration code values and meanings:
	 * <table>
	 * <th>Code</th><th>Display</th>
	 * <tr><td>ms</td><td>milliseconds</td></tr>
	 * <tr><td>s</td><td>seconds</td></tr>
	 * <tr><td>min</td><td>minutes</td></tr>
	 * <tr><td>h</td><td>hours</td></tr>
	 * <tr><td>d</td><td>days</td></tr>
	 * <tr><td>wk</td><td>weeks</td></tr>
	 * <tr><td>mo</td><td>months</td></tr>
	 * <tr><td>a</td><td>years</td></tr>
	 * </table>
	 */
	public Date calculatePeriodEndFromDuration(Date periodStart, TimeZone periodStartTZ, Duration duration) {
		DateTime dtPeriodEnd = null;

		try {
			DateTimeZone periodStartDTZ = DateTimeZone.forTimeZone(periodStartTZ);
			DateTime dtPeriodStart = new DateTime(periodStart.getTime(), periodStartDTZ);
			int durationValue = duration.getValue().intValue();
			String durationCode = duration.getCode();

			// periodEnd = periodStart + duration
			if (durationCode != null) {
				if (durationCode.equals("ms")) {
					dtPeriodEnd = dtPeriodStart.plusMillis(durationValue);
				}
				else if (durationCode.equals("s")) {
					dtPeriodEnd = dtPeriodStart.plusSeconds(durationValue);
				}
				else if (durationCode.equals("min")) {
					dtPeriodEnd = dtPeriodStart.plusMinutes(durationValue);
				}
				else if (durationCode.equals("h")) {
					dtPeriodEnd = dtPeriodStart.plusHours(durationValue);
				}
				else if (durationCode.equals("d")) {
					dtPeriodEnd = dtPeriodStart.plusDays(durationValue);
				}
				else if (durationCode.equals("wk")) {
					dtPeriodEnd = dtPeriodStart.plusWeeks(durationValue);
				}
				else if (durationCode.equals("mo")) {
					dtPeriodEnd = dtPeriodStart.plusMonths(durationValue);
				}
				else if (durationCode.equals("a")) {
					dtPeriodEnd = dtPeriodStart.plusYears(durationValue);
				}
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			return null;
		}

		if (dtPeriodEnd != null) {
			return dtPeriodEnd.toDate();
		}
		else {
			return null;
		}
	}

	/**
	 * Calculate the Period.start Date value from the Period.end and the specified Duration
	 *
	 * Allowed Duration code values and meanings:
	 * <table>
	 * <th>Code</th><th>Display</th>
	 * <tr><td>ms</td><td>milliseconds</td></tr>
	 * <tr><td>s</td><td>seconds</td></tr>
	 * <tr><td>min</td><td>minutes</td></tr>
	 * <tr><td>h</td><td>hours</td></tr>
	 * <tr><td>d</td><td>days</td></tr>
	 * <tr><td>wk</td><td>weeks</td></tr>
	 * <tr><td>mo</td><td>months</td></tr>
	 * <tr><td>a</td><td>years</td></tr>
	 * </table>
	 */
	public Date calculatePeriodStartFromDuration(Date periodEnd, TimeZone periodEndTZ, Duration duration) {
		DateTime dtPeriodStart = null;

		try {
			DateTimeZone periodEndDTZ = DateTimeZone.forTimeZone(periodEndTZ);
			DateTime dtPeriodEnd = new DateTime(periodEnd.getTime(), periodEndDTZ);
			int durationValue = duration.getValue().intValue();
			String durationCode = duration.getCode();

			// periodStart = periodEnd - duration
			if (durationCode != null) {
				if (durationCode.equals("ms")) {
					dtPeriodStart = dtPeriodEnd.minusMillis(durationValue);
				}
				else if (durationCode.equals("s")) {
					dtPeriodStart = dtPeriodEnd.minusSeconds(durationValue);
				}
				else if (durationCode.equals("min")) {
					dtPeriodStart = dtPeriodEnd.minusMinutes(durationValue);
				}
				else if (durationCode.equals("h")) {
					dtPeriodStart = dtPeriodEnd.minusHours(durationValue);
				}
				else if (durationCode.equals("d")) {
					dtPeriodStart = dtPeriodEnd.minusDays(durationValue);
				}
				else if (durationCode.equals("wk")) {
					dtPeriodStart = dtPeriodEnd.minusWeeks(durationValue);
				}
				else if (durationCode.equals("mo")) {
					dtPeriodStart = dtPeriodEnd.minusMonths(durationValue);
				}
				else if (durationCode.equals("a")) {
					dtPeriodStart = dtPeriodEnd.minusYears(durationValue);
				}
			}
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			return null;
		}

		if (dtPeriodStart != null) {
			return dtPeriodStart.toDate();
		}
		else {
			return null;
		}
	}

	/*
	 * Original code from...
	 *
	 * A utility class for parsing and formatting HTTP dates as used in cookies
	 * and other headers. This class handles dates as defined by RFC 2616
	 * section 3.3.1 as well as some other common non-standard formats.
	 *
	 * @author Christopher Brown
	 * @author Michael Becke
	 */

	/**
	 * Parses a date value. The formats used for parsing the date value are
	 * retrieved from the default http params.
	 *
	 * @param dateValue
	 *            the date value to parse
	 * @return the parsed date
	 * @throws DateParseException
	 *             if the value could not be parsed using any of the supported
	 *             date formats
	 */
	public Date parseHTTPDate(String dateValue) {
		return parseHTTPDate(dateValue, null, null);
	}

	/**
	 * Parses the date value using the given date formats.
	 *
	 * @param dateValue
	 *            the date value to parse
	 * @param dateFormats
	 *            the date formats to use
	 * @return the parsed date
	 * @throws DateParseException
	 *             if none of the dataFormats could parse the dateValue
	 */
	public Date parseHTTPDate(String dateValue, Collection<String> dateFormats) {
		return parseHTTPDate(dateValue, dateFormats, null);
	}

	/**
	 * Parses the date value using the given date formats.
	 *
	 * @param dateValue
	 *            the date value to parse
	 * @param dateFormats
	 *            the date formats to use
	 * @param startDate
	 *            During parsing, two digit years will be placed in the range
	 *            <code>startDate</code> to <code>startDate + 100 years</code>.
	 *            This value may be <code>null</code>. When <code>null</code> is
	 *            given as a parameter, year <code>2000</code> will be used.
	 * @return the parsed date
	 * @throws DateParseException
	 *             if none of the dataFormats could parse the dateValue
	 */
	public Date parseHTTPDate(String dateValue, Collection<String> dateFormats, Date startDate) {

		if (dateValue == null) {
			throw new IllegalArgumentException("dateValue is null");
		}
		if (dateFormats == null) {
			dateFormats = DEFAULT_PATTERNS;
		}
		if (startDate == null) {
			startDate = DEFAULT_TWO_DIGIT_YEAR_START;
		}
		// trim single quotes around date if present
		// see issue #5279
		if (dateValue.length() > 1 && dateValue.startsWith("'") && dateValue.endsWith("'")) {
			dateValue = dateValue.substring(1, dateValue.length() - 1);
		}

		SimpleDateFormat dateParser = null;
		for (String format : dateFormats) {
			if (dateParser == null) {
				dateParser = new SimpleDateFormat(format, Locale.US);
				dateParser.setTimeZone(TimeZone.getTimeZone("GMT"));
				dateParser.set2DigitYearStart(startDate);
			} else {
				dateParser.applyPattern(format);
			}
			try {
				return dateParser.parse(dateValue);
			} catch (ParseException pe) {
				// ignore this exception, we will try the next format
			}
		}

		// we were unable to parse the date
		throw new RuntimeException("Unable to parse the date " + dateValue);
	}

	/**
	 * Formats the given date according to the RFC 1123 pattern.
	 *
	 * @param date
	 *            The date to format.
	 * @return An RFC 1123 formatted date string.
	 * @see #PATTERN_RFC1123
	 */
	public String formatHTTPDate(Date date) {
		return formatHTTPDate(date, PATTERN_RFC1123);
	}

	/**
	 * Formats the given date according to the specified pattern. The pattern
	 * must conform to that used by the {@link SimpleDateFormat simple date
	 * format} class.
	 *
	 * @param date
	 *            The date to format.
	 * @param pattern
	 *            The pattern to use for formatting the date.
	 * @return A formatted date string.
	 * @throws IllegalArgumentException
	 *             If the given date pattern is invalid.
	 * @see SimpleDateFormat
	 */
	public String formatHTTPDate(Date date, String pattern) {
		if (date == null) {
			throw new IllegalArgumentException("date is null");
		}
		if (pattern == null) {
			throw new IllegalArgumentException("pattern is null");
		}

		SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		return formatter.format(date);
	}

}
