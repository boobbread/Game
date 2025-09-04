package com.mjolkster.artifice.util.ai;

import com.badlogic.gdx.math.Vector2;

import java.util.Objects;

public class Node {

    private final int x, y;
    public Vector2 position;
    private double g;
    private double h;
    private double f;
    private Node parent;

    public Node(Vector2 pos, double g, double h, Node parent) {
        this.position = pos;
        this.x = (int) pos.x;
        this.y = (int) pos.y;
        this.g = g;
        this.h = h;
        this.f = g + h;
        this.parent = parent;
    }

    public Node(Vector2 pos) {
        this(pos, Double.POSITIVE_INFINITY, 0.0F, null);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
        this.f = this.g + this.h;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
        this.f = this.h + this.g;
    }

    public double getF() {
        return f;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return "Node(" + x + ", " + y + ", g=" + g + ", h=" + h + ", f=" + f + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node)) return false;
        Node other = (Node) obj;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getX(), getY());
    }
}
