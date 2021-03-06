/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 *
 */
package nl.inl.util;

/**
 * Simple class for measuring elapsed time.
 */
public class Timer {
	private long start;

	public Timer() {
		reset();
	}

	public long elapsed() {
		return System.currentTimeMillis() - start;
	}

	public void reset() {
		start = System.currentTimeMillis();
	}

	/**
	 * Describe the elapsed time in a human-readable way.
	 *
	 * @return human-readable string for the elapsed time.
	 */
	public String elapsedDescription() {
		return elapsedDescription(false);
	}

	/**
	 * Describe the elapsed time in a human-readable way.
	 *
	 * TODO: why only elapsed time? Generalise to describe any supplied interval.
	 * @param reportMsec if true, also reports milliseconds
	 *
	 * @return human-readable string for the elapsed time.
	 */
	public String elapsedDescription(boolean reportMsec) {
		return describeInterval(elapsed(), reportMsec);
	}

	/**
	 * Describe the interval in a human-readable way.
	 *
	 * Doesn't report details below a second.
	 *
	 * @param interval time in ms
	 * @return human-readable string for the interval.
	 */
	public static String describeInterval(long interval) {
		return describeInterval(interval, false);
	}

	/**
	 * Describe the elapsed time in a human-readable way.
	 *
	 * @param interval time in ms
	 * @param reportMsec if true, also reports milliseconds
	 *
	 * @return human-readable string for the elapsed time.
	 */
	public static String describeInterval(long interval, boolean reportMsec) {
		long msec = interval % 1000;
		long sec = interval / 1000;
		long min = sec / 60;
		sec %= 60;
		StringBuilder result = new StringBuilder();
		if (min > 0) {
			result.append(min).append(" ").append(StringUtil.pluralize("minute", min)).append(", ");
		}
		result.append(sec).append(" ").append(StringUtil.pluralize("second", sec));
		if (reportMsec) {
			result.append(", ").append(msec).append(" ").append(StringUtil.pluralize("millisecond", msec));
		}
		return result.toString();
	}
}
