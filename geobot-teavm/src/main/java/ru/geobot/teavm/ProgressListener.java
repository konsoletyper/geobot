package ru.geobot.teavm;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
public interface ProgressListener {
    void progressChanged(int current, int total);
}
