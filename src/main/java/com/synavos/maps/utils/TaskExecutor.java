package com.synavos.maps.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TaskExecutor.
 *
 * @author Ibraheem Faiq
 * @since Mar 28, 2018
 */

public class TaskExecutor {

    public static final String CLASS_NAME = TaskExecutor.class.getName();

    private static final Logger LGR = LoggerFactory.getLogger(CLASS_NAME);

    /** The Constant MAX_PARALLEL_REQUESTS. */
    public static final int MAX_PARALLEL_REQUESTS = 10;

    public static Collection<Future<Object>> executeTasks(final Collection<Callable<Object>> tasks) {
	return executeTasks(tasks, MAX_PARALLEL_REQUESTS);
    }

    public static Collection<Future<Object>> executeTasks(final Collection<Callable<Object>> tasks,
	    int _maxParallelRequests) {
	Collection<Future<Object>> results = null;
	final int maxParallelRequest = _maxParallelRequests > 0 ? _maxParallelRequests : 1;

	if (null != tasks && tasks.size() > 0) {
	    log("Executing tasks : ", tasks.size());

	    results = new ArrayList<>(tasks.size());
	    ExecutorService executor = null;

	    try {
		executor = Executors.newFixedThreadPool(maxParallelRequest);

		final Iterator<Callable<Object>> iterator = tasks.iterator();
		while (iterator.hasNext()) {

		    final List<Callable<Object>> _tasks = new ArrayList<>(maxParallelRequest);
		    for (int i = 0; i < maxParallelRequest && iterator.hasNext(); i++) {
			_tasks.add(iterator.next());
		    }

		    final List<Future<Object>> _results = executor.invokeAll(_tasks);
		    results.addAll(_results);

		    log("########## [", results.size(), "] Tasks of [", tasks.size(), "] executed ##########");
		    Thread.sleep(1000);
		}
	    }
	    catch (final Exception ex) {
		ex.printStackTrace();
	    }
	    finally {
		if (null != executor) {
		    executor.shutdown();
		}
	    }
	}

	return results;
    }

    private static void log(Object... strings) {
	LGR.info(LGR.isInfoEnabled() ? StringUtils.concatValues(strings) : null);
    }

}
