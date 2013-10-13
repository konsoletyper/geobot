package ru.geobot.graphics;

/**
 *
 * @author Alexey Andreev
 */
public class AffineTransform {
    public float a;
    public float b;
    public float c;
    public float d;
    public float e;
    public float f;

    public void scale(float x, float y) {
        a *= x;
        b *= x;
        c *= y;
        d *= y;
    }

    public void rotate(float angle) {
        float cos = (float)Math.cos(angle);
        float sin = (float)Math.sin(angle);
        float newA = a * cos + c * sin;
        float newC = -a * sin + c * cos;
        float newB = b * cos + d * sin;
        float newD = -b * sin + d * cos;
        a = newA;
        b = newB;
        c = newC;
        d = newD;
    }

    public void translate(float x, float y) {
        e += a * x + c * y;
        f += b * x + d * y;
    }

    public void transform(AffineTransform other) {
        float newA = a * other.a + c * other.b;
        float newB = b * other.a + d * other.b;
        float newC = a * other.c + c * other.d;
        float newD = b * other.c + d * other.d;
        float newE = a * other.e + c * other.f + e;
        float newF = b * other.e + d * other.f + f;
        a = newA;
        b = newB;
        c = newC;
        d = newD;
        e = newE;
        f = newF;
    }

    public AffineTransform copy() {
        AffineTransform copy = new AffineTransform();
        copy.a = a;
        copy.b = b;
        copy.c = c;
        copy.d = d;
        copy.e = e;
        copy.f = f;
        return copy;
    }

    public static AffineTransform identity() {
        AffineTransform transform = new AffineTransform();
        transform.a = 1;
        transform.d = 1;
        return transform;
    }

    public float getDeterminant() {
        return a * d - b * c;
    }
}
