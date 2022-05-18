package com.example.proyectoservicio;

import org.opencv.core.Point;

public class Circulo {
    private Point center;
    private int radius;

    public Circulo(Point center,int radius){
        this.setCenter(center);
        this.setRadius(radius);
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
