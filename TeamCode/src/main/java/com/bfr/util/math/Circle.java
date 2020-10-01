package com.bfr.util.math;

public class Circle {
    public final Point center;
    public final double radius;

    public Circle(Point center, double radius){
        this.center = center;
        this.radius = radius;
    }

    /**
     * Create a circle given two points on the circumference and a tangent line
     */
    public Circle(Point p1, Point p2, Line l){
        Line eqLine = Line.findEquidistantLine(p1, p2);
        Line perpLine = l.getPerpLineAtPoint(p2);

        center = eqLine.findIntersection(perpLine);

        radius = p1.distanceTo(center);
    }
}
