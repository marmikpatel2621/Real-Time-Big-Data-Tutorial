package com.videoprocessing.utils;

import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.image.renderer.ScanRasteriser;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Created by user on 21/09/2016.
 */
public class PolygonExtractionProcessor <T, S extends Image<T, S>> implements SinglebandImageProcessor<T, S> {
    private Polygon polygon;
    private T background;

    /**
     * Construct with the given polygon and background colour
     *
     * @param p
     * @param colour
     */
    public PolygonExtractionProcessor(Polygon p, T colour) {
        this.polygon = p;
        this.background = colour;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
     * .image.Image)
     */
    public void processImage(final S image) {
        final Polygon p = this.polygon;
        final Rectangle r = p.calculateRegularBoundingBox();

        final int dx = (int) r.x;
        final int dy = (int) r.y;

        final S output = image.newInstance((int) r.width, (int) r.height);
        output.fill(background);

        ScanRasteriser.scanFill(p.getVertices(), new ScanRasteriser.ScanLineListener() {
            public void process(int x1, int x2, int y) {
                for (int x = x1; x <= x2; x++) {
                    output.setPixel(x - dx, y - dy, image.getPixel(x, y));
                }
            }
        });

        image.internalAssign(output);
    }
}
