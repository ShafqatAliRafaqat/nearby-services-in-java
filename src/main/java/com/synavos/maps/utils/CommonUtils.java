package com.synavos.maps.utils;

import java.io.Closeable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * The Class CommonUtils.
 *
 * @author Ibraheem Faiq
 * @since Mar 26, 2018
 */

public class CommonUtils {

    private CommonUtils() {
	super();
    }

    /**
     * Checks if is null or empty array.
     *
     * @param array
     *            the array
     * @return true, if is null or empty array
     */
    public static boolean isNullOrEmptyArray(final Object[] array) {
	return null == array || array.length == 0;
    }

    /**
     * Checks if is null.
     *
     * @param obj the obj
     * @return true, if is null
     */
    public static boolean isNull(final Object... obj) {
	return !isNotNull(obj);
    }

    /**
     * Checks if is not null.
     *
     * @param obj
     *            the obj
     * @return true, if is not null
     */
    public static boolean isNotNull(final Object... obj) {
	if (null == obj) {
	    return false;
	}
	else {
	    for (final Object object : obj) {
		if (null == object) {
		    return false;
		}
	    }
	}

	return true;
    }

    /**
     * Log exception.
     *
     * @param lgr
     *            the lgr
     * @param ex
     *            the ex
     * @param className
     *            the class name
     */
    public static void logException(final Logger lgr, final Exception ex, final String className) {
	if (isNotNull(lgr, ex, className)) {
	    lgr.error(StringUtils.concatValues("#Exception# occurred at class [" + className + "]"), ex);
	}
    }

    /**
     * Checks if is null or empty collection.
     *
     * @param <T>
     *            the generic type
     * @param collection
     *            the collection
     * @return true, if is null or empty collection
     */
    public static <T> boolean isNullOrEmptyCollection(final Collection<T> collection) {
	return !isNotNull(collection) || collection.isEmpty();
    }

    /**
     * Checks if is null or empty map.
     *
     * @param <K>
     *            the key type
     * @param <V>
     *            the value type
     * @param map
     *            the map
     * @return true, if is null or empty map
     */
    public static <K, V> boolean isNullOrEmptyMap(final Map<K, V> map) {
	return !isNotNull(map) || map.isEmpty();
    }

    /**
     * Close resources.
     *
     * @param closeables
     *            the closeables
     */
    public static void closeResources(final Closeable... closeables) {
	if (!CommonUtils.isNullOrEmptyArray(closeables)) {
	    for (final Closeable closeable : closeables) {
		if (isNotNull(closeable)) {
		    try {
			closeable.close();
		    }
		    catch (final Exception ex) {
			// do nothing
		    }
		}
	    }
	}
    }

    /**
     * Truncate decimal.
     *
     * @param x
     *            the x
     * @param numberofDecimals
     *            the numberof decimals
     * @return the double
     */
    public static double truncateDecimal(double x, int numberofDecimals) {
	if (x > 0) {
	    return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR).doubleValue();
	}
	else {
	    return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING).doubleValue();
	}
    }

    public static double round(double x, int numberofDecimals) {
	return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * Checks if is any common.
     *
     * @param col1
     *            the col 1
     * @param col2
     *            the col 2
     * @return true, if is any common
     */
    public static boolean isAnyCommon(Collection<? extends Object> col1, Collection<? extends Object> col2) {
	boolean common = false;

	if (!isNullOrEmptyCollection(col1) && !isNullOrEmptyCollection(col1)) {
	    for (Object obj : col1) {
		if (col2.contains(obj)) {
		    common = true;
		    break;
		}
	    }
	}

	return common;
    }

    /**
     * Gets the list from string.
     *
     * @param placeTypes
     *            the place types
     * @param delimiter
     *            the delimiter
     * @return the list from string
     */
    public static List<String> getListFromString(final String value, final String delimiter) {
	List<String> results = null;

	if (!StringUtils.isNullOrEmptyStr(value)) {
	    if (!CommonUtils.isNotNull(delimiter) || !value.contains(delimiter)) {
		results = new ArrayList<>(1);
		results.add(value);
	    }
	    else {
		final String[] values = value.split(Pattern.quote(delimiter));
		results = new ArrayList<>(values.length);
		for (final String val : values) {
		    if (!StringUtils.isNullOrEmptyStr(val)) {
			results.add(val.trim());
		    }
		}
	    }
	}

	return results;
    }

}
