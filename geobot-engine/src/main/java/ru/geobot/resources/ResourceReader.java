package ru.geobot.resources;

/**
 *
 * @author Alexey Andreev
 */
public interface ResourceReader {
    <T> T getResourceSet(Class<T> resourceSetType);
}
