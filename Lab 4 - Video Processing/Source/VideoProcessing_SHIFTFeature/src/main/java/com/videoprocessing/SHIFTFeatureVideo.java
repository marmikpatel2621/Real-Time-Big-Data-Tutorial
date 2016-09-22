package com.videoprocessing;

import Jama.Matrix;
import com.videoprocessing.utils.PolygonDrawingListener;
import com.videoprocessing.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.shape.*;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.*;
import org.openimaj.math.geometry.transforms.check.TransformMatrixConditionCheck;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

/**
 * Created by user on 21/09/2016.
 */
public class SHIFTFeatureVideo implements KeyListener, VideoDisplayListener<MBFImage> {
    enum RenderMode {
        SQUARE {
            @Override
            public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
                renderer.drawShape(rectangle.transform(transform), 3, RGBColour.BLUE);
            }
        },
        PICTURE {
            MBFImage toRender = null;
            private Matrix renderToBounds;

            @Override
            public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
                if (this.toRender == null) {
                    try {
                        this.toRender = ImageUtilities.readMBF(SHIFTFeatureVideo.class
                                .getResource("/org/openimaj/demos/OpenIMAJ.png"));
                    } catch (final IOException e) {
                        System.err.println("Can't load image to render");
                    }
                    this.renderToBounds = TransformUtilities.makeTransform(this.toRender.getBounds(), rectangle);
                }

                final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
                mbfPP.setMatrix(transform.times(this.renderToBounds));
                mbfPP.accumulate(this.toRender);
                mbfPP.performProjection(0, 0, renderer.getImage());

            }
        },
        VIDEO {
            private XuggleVideo toRender;
            private Matrix renderToBounds;

            @Override
            public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
                if (this.toRender == null) {
                    this.toRender = new XuggleVideo(
                            SHIFTFeatureVideo.class.getResource("/org/openimaj/demos/video/keyboardcat.flv"), true);
                    this.renderToBounds = TransformUtilities.makeTransform(new Rectangle(0, 0, this.toRender.getWidth(),
                            this.toRender.getHeight()), rectangle);
                }

                final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
                mbfPP.setMatrix(transform.times(this.renderToBounds));
                mbfPP.accumulate(this.toRender.getNextFrame());
                mbfPP.performProjection(0, 0, renderer.getImage());
            }
        };
        public abstract void render(MBFImageRenderer renderer, Matrix transform, Rectangle rectangle);
    }

    private final VideoCapture capture;
    private final VideoDisplay<MBFImage> videoFrame;
    private final DisplayUtilities.ImageComponent modelFrame;
    private final DisplayUtilities.ImageComponent matchFrame;
    private final DisplayUtilities.ImageComponent matchFrame1;

    private MBFImage modelImage;

    private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
    private final DoGSIFTEngine engine;
    private final PolygonDrawingListener polygonListener;
    private final JPanel vidPanel;
    private final JPanel modelPanel;
    private final JPanel matchPanel;
    private RenderMode renderMode = RenderMode.SQUARE;
    private MBFImage currentFrame;

    /**
     * Construct the demo
     *
     * @param window
     * @throws Exception
     */
    public SHIFTFeatureVideo(final JComponent window) throws Exception {
        this(window, new VideoCapture(320, 240));
    }

    /**
     * Construct the demo
     *
     * @param window
     * @param capture
     * @throws Exception
     */
    public SHIFTFeatureVideo(final JComponent window, final VideoCapture capture) throws Exception {
        final int width = capture.getWidth();
        final int height = capture.getHeight();
        this.capture = capture;
        this.polygonListener = new PolygonDrawingListener();

        GridBagConstraints gbc = new GridBagConstraints();

        final JLabel label = new JLabel(
                "<html><body><p>Hold an object in front of the camera, and press space. Select<br/>" +
                        "the outline of the object by clicking points on the frozen video<br/>" +
                        "image, and press C when you're done. Press space to start the video<br/>" +
                        "again, and the object should be tracked.</p></body></html>");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 8, 8, 8);
        window.add(label, gbc);

        this.vidPanel = new JPanel(new GridBagLayout());
        this.vidPanel.setBorder(BorderFactory.createTitledBorder("Live Video"));
        this.videoFrame = VideoDisplay.createVideoDisplay(capture, this.vidPanel);
        gbc = new GridBagConstraints();
        gbc.gridy = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        window.add(this.vidPanel, gbc);

        this.modelPanel = new JPanel(new GridBagLayout());
        this.modelPanel.setBorder(BorderFactory.createTitledBorder("Model"));
        this.modelFrame = new DisplayUtilities.ImageComponent(true, false);
        this.modelFrame.setShowPixelColours(false);
        this.modelFrame.setShowXYPosition(false);
        this.modelFrame.removeMouseListener(this.modelFrame);
        this.modelFrame.removeMouseMotionListener(this.modelFrame);
        this.modelFrame.setSize(width, height);
        this.modelFrame.setPreferredSize(new Dimension(width, height));
        this.modelPanel.add(this.modelFrame);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.gridy = 1;
        gbc.gridx = 1;
        window.add(this.modelPanel, gbc);

        this.matchPanel = new JPanel(new GridBagLayout());
        this.matchPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
        this.matchFrame = new DisplayUtilities.ImageComponent(true, false);
        this.matchFrame.setShowPixelColours(false);
        this.matchFrame.setShowXYPosition(false);
        this.matchFrame.removeMouseListener(this.matchFrame);
        this.matchFrame.removeMouseMotionListener(this.matchFrame);
        this.matchFrame.setSize(width, height);
        this.matchFrame.setPreferredSize(new Dimension(width*2, height));
        this.matchFrame1 = new DisplayUtilities.ImageComponent(true, false);
        this.matchFrame1.setShowPixelColours(false);
        this.matchFrame1.setShowXYPosition(false);
        this.matchFrame1.removeMouseListener(this.matchFrame);
        this.matchFrame1.removeMouseMotionListener(this.matchFrame);
        this.matchFrame1.setSize(width, height);
        this.matchFrame1.setPreferredSize(new Dimension(width*2, height));
        this.matchPanel.add(this.matchFrame);
        this.matchPanel.add(this.matchFrame1);
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        window.add(this.matchPanel, gbc);

        this.videoFrame.getScreen().addMouseListener(this.polygonListener);

        this.videoFrame.addVideoListener(this);
        this.engine = new DoGSIFTEngine();
        this.engine.getOptions().setDoubleInitialImage(true);
    }

    public synchronized void keyPressed(final KeyEvent key) {
        if (key.getKeyCode() == KeyEvent.VK_SPACE) {
            this.videoFrame.togglePause();
        } else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
            try {
                final org.openimaj.math.geometry.shape.Polygon p = this.polygonListener.getPolygon().clone();
                this.polygonListener.reset();
                this.modelImage = this.currentFrame.process(
                        new PolygonExtractionProcessor<Float[], MBFImage>(p, RGBColour.WHITE));

                if (this.matcher == null) {
                    final RobustAffineTransformEstimator ransac = new RobustAffineTransformEstimator(0.1, 100,
                            new RANSAC.PercentageInliersStoppingCondition(1),
                            new TransformMatrixConditionCheck<AffineTransformModel>(10000));

                    this.matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
                            new FastBasicKeypointMatcher<Keypoint>(8));
                    this.matcher.setFittingModel(ransac);

                    this.modelPanel.setPreferredSize(this.modelPanel.getSize());
                }

                this.modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(this.modelImage));

                final DoGSIFTEngine engine = new DoGSIFTEngine();
                engine.getOptions().setDoubleInitialImage(true);

                // final ASIFTEngine engine = new ASIFTEngine(true);

                final FImage modelF = Transforms.calculateIntensityNTSC_LUT(this.modelImage);
                this.matcher.setModelFeatures(engine.findFeatures(modelF));
            } catch (final Exception e) {
                e.printStackTrace();
            }
        } else if (key.getKeyChar() == '1') {
            this.renderMode = RenderMode.SQUARE;
        } else if (key.getKeyChar() == '2') {
            this.renderMode = RenderMode.PICTURE;
        } else if (key.getKeyChar() == '3') {
            this.renderMode = RenderMode.VIDEO;
        }
    }


    public void keyReleased(final KeyEvent arg0) {
    }

    public void keyTyped(final KeyEvent arg0) {
    }

    public synchronized void afterUpdate(final VideoDisplay<MBFImage> display) {
        if (this.matcher != null && !this.videoFrame.isPaused()) {
            final MBFImage capImg = this.currentFrame;
            final LocalFeatureList<Keypoint> kpl = this.engine.findFeatures(Transforms.calculateIntensityNTSC_LUT(capImg));
            final LocalFeatureList<Keypoint> kpl1 = this.engine.findFeatures(Transforms.calculateIntensity(capImg));
            final MBFImageRenderer renderer = capImg.createRenderer();
            renderer.drawPoints(kpl, RGBColour.MAGENTA, 3);
            renderer.drawPoints(kpl1,RGBColour.GREEN,4);
            MBFImage matches,matches1;
            if (this.matcher.findMatches(kpl)
                    && ((MatrixTransformProvider) this.matcher.getModel()).getTransform().cond() < 1e6)
            {
                try {
                    final Matrix boundsToPoly = ((MatrixTransformProvider) this.matcher.getModel()).getTransform()
                            .inverse();

                    if (modelImage.getBounds().transform(boundsToPoly).isConvex()) {
                        this.renderMode.render(renderer, boundsToPoly, this.modelImage.getBounds());
                    }
                } catch (final RuntimeException e) {
                }

                matches = MatchingUtilities
                        .drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
            } else {
                matches = MatchingUtilities
                        .drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
            }
            if (this.matcher.findMatches(kpl1)
                    && ((MatrixTransformProvider) this.matcher.getModel()).getTransform().cond() < 1e6)
            {
                try {
                    final Matrix boundsToPoly = ((MatrixTransformProvider) this.matcher.getModel()).getTransform()
                            .inverse();

                    if (modelImage.getBounds().transform(boundsToPoly).isConvex()) {
                        this.renderMode.render(renderer, boundsToPoly, this.modelImage.getBounds());
                    }
                } catch (final RuntimeException e) {
                }

                matches1 = MatchingUtilities
                        .drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.GREEN);
            } else {
                matches1 = MatchingUtilities
                        .drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.GREEN);
            }

            this.matchPanel.setPreferredSize(this.matchPanel.getSize());
            this.matchFrame.setImage(ImageUtilities.createBufferedImageForDisplay(matches));
            this.matchFrame1.setImage(ImageUtilities.createBufferedImageForDisplay(matches1));
        }
    }

    public void beforeUpdate(final MBFImage frame) {
        if (!this.videoFrame.isPaused())
            this.currentFrame = frame.clone();
        else {
            frame.drawImage(currentFrame, 0, 0);
        }
        this.polygonListener.drawPoints(frame);
    }

    /**
     * Stop capture
     */
    public void stop() {
        this.videoFrame.close();
        this.capture.stopCapture();
    }

    /**
     * Main method
     *
     * @param args
     *            ignored
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {
        final JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.setLayout(new GridBagLayout());
        final JPanel c = new JPanel();
        c.setLayout(new GridBagLayout());
        window.getContentPane().add(c);

        final SHIFTFeatureVideo vs = new SHIFTFeatureVideo(c);
        SwingUtilities.getRoot(window).addKeyListener(vs);
        window.pack();
        window.setVisible(true);
    }
}
