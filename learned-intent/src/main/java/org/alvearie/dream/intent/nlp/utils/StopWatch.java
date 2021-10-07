/*
 * (C) Copyright IBM Corp. 2021, 2021
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package org.alvearie.dream.intent.nlp.utils;

import java.time.Duration;
import java.time.Instant;

import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * Utility class to time and log events.
 *
 */
public class StopWatch {

    private Instant start;

    private StopWatch() {
        this.start = Instant.now();
    }

    /**
     * Stops this {@link StopWatch} object.
     *
     * @return a string with the formatted elapsed duration
     */
    public String stop() {
        Instant end = Instant.now();
        Duration duration = Duration.between(start, end);
        return DurationFormatUtils.formatDuration(duration.toMillis(), "H:mm:ss", true);
    }

    /**
     * Creates and starts a new {@link StopWatch} object.
     *
     * @return the new stop watch
     */
    public static StopWatch start() {
        return new StopWatch();
    }
}
