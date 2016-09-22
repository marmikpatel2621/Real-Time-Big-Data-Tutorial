package com.videoprocessing.utils;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Created by user on 21/09/2016.
 */
public class PolygonDrawingListener implements MouseListener {

    private Polygon polygon;

    /**
     * Default constructor
     */
    public PolygonDrawingListener() {
        this.polygon = new Polygon();
    }

    /**
     * Reset the polygon.
     */
    public void reset() {
        this.polygon = new Polygon();
    }
    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        this.polygon.getVertices().add(new Point2dImpl(e.getX(),e.getY()));
    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }
    public Polygon getPolygon() {
        return this.polygon;
    }

    /**
     * Draw the polygon onto an image.
     * @param image the image to draw on.
     */
    public void drawPoints(MBFImage image) {
        Polygon p = getPolygon();
        MBFImageRenderer renderer = image.createRenderer();

        if(p.getVertices().size() > 2) {
            renderer.drawPolygon(p, 3, RGBColour.RED);
        }

        for(Point2d point : p.getVertices()) {
            renderer.drawPoint(point, RGBColour.BLUE, 5);
        }
    }
}
