package ru.geobot.util;

/**
 *
 * @author Alexey Andreev <konsoletyper@gmail.com>
 */
class Edge {
    Vertex first;
    Vertex second;
    Edge previous;
    Edge next;
    Edge opposite;

    public void merge() {
        if (opposite == null) {
            throw new IllegalStateException("This edge has no an opposite edge");
        }
        next.previous = opposite.previous;
        previous.next = opposite.next;
        opposite.next.previous = previous;
        opposite.previous.next = next;
        next = null;
        first = null;
        second = null;
        previous = null;
        opposite.next = null;
        opposite.previous = null;
        opposite.opposite = null;
        opposite.first = null;
        opposite.second = null;
        opposite = null;
    }

    public void destroy() {
        first = null;
        second = null;
        if (opposite != null) {
            opposite.opposite = null;
            opposite = null;
        }
    }

    public boolean isDestroyed() {
        return first == null;
    }
}
