package com.mjolkster.artifice.util.math;

public class Gaussian {

    public final double a, b, c;

    public Gaussian(double a, double b, double c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public String toString() {
        return "[" + a + ", " + b + ", " + c + "]";
    }
}

