package com.synavos.maps.utils;

import static com.synavos.maps.utils.CommonUtils.isNotNull;
import static com.synavos.maps.utils.CommonUtils.isNullOrEmptyArray;

/**
 * The Class StringUtils.
 *
 * @author Ibraheem Faiq
 * @since Mar 26, 2018
 */
public final class StringUtils {

    private StringUtils() {
	super();
    }

    /**
     * To integer.
     *
     * @param str
     *            the str
     * @return the integer
     */
    public static Integer toInteger(final String str) {
	return toInteger(str, null);
    }

    /**
     * Checks if is integer.
     *
     * @param str the str
     * @return true, if is integer
     */
    public static boolean isInteger(final String str) {
	boolean isInteger = false;

	if (!isNullOrEmptyStr(str)) {
	    try {
		Long.parseLong(str);
		isInteger = true;
	    }
	    catch (final NumberFormatException ex) {
		// do nothing
	    }
	}

	return isInteger;
    }

    /**
     * To integer.
     *
     * @param str
     *            the str
     * @param defaultValue
     *            the default value
     * @return the int
     */
    public static Integer toInteger(final String str, Integer defaultValue) {
	Integer result = defaultValue;

	if (!isNullOrEmptyStr(str)) {
	    try {
		result = Integer.valueOf(str);
	    }
	    catch (final NumberFormatException ex) {
		// Nothing to do here
	    }
	}

	return result;
    }

    /**
     * Checks if is null or empty str.
     *
     * @param str
     *            the str
     * @return true, if is null or empty str
     */
    public static boolean isNullOrEmptyStr(final String... strings) {
	boolean result = true;
	if (!CommonUtils.isNullOrEmptyArray(strings)) {
	    result = false;
	    for (final String str : strings) {
		result = null == str || str.trim().length() == 0;
		if (result) {
		    break;
		}
	    }
	}

	return result;
    }

    /**
     * Concat values.
     *
     * @param values
     *            the values
     * @return the string
     */
    public static String concatValues(Object... values) {
	return _concatValues(true, values);
    }

    /**
     * Concat not null values.
     *
     * @param values
     *            the values
     * @return the string
     */
    public static String concatNotNullValues(Object... values) {
	return _concatValues(false, values);
    }

    private static String _concatValues(boolean concatNullValues, Object... values) {
	String concatedValues = null;

	if (!isNullOrEmptyArray(values)) {
	    final StringBuilder builder = new StringBuilder();

	    for (final Object value : values) {
		if (isNotNull(value) || concatNullValues) {
		    builder.append(String.valueOf(value));
		}
	    }

	    concatedValues = builder.toString();
	}

	return concatedValues;
    }

}
