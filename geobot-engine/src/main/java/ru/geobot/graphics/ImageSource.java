package ru.geobot.graphics;

/**
 *
 * @author Alexey Andreev
 */
public interface ImageSource {
    <T> T getImages(Class<T> imageSetType);
}
