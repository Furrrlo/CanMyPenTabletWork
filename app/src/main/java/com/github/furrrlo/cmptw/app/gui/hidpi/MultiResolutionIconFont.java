package com.github.furrrlo.cmptw.app.gui.hidpi;

import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

import java.awt.*;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

public class MultiResolutionIconFont extends AbstractMultiResolutionImage {

    private final IconCode iconCode;
    private final int baseSize;
    private final Color color;
    private final Map<Double, Image> resolutionVariants = new HashMap<>();

    public MultiResolutionIconFont(IconCode iconCode, int baseSize) {
        this(iconCode, baseSize, null);
    }

    public MultiResolutionIconFont(IconCode iconCode, int baseSize, Color color) {
        this(iconCode, baseSize, color, 1, 1.25d, 1.5d, 2, 2.5d);
    }

    public MultiResolutionIconFont(IconCode iconCode, int baseSize, Color color, double... multiples) {
        this.iconCode = iconCode;
        this.baseSize = baseSize;
        this.color = color;

        DoubleStream.of(multiples).forEach(multiplier -> resolutionVariants.put(baseSize * multiplier, buildImage(baseSize * multiplier)));
    }

    @Override
    public int getWidth(ImageObserver observer) {
        return baseSize;
    }

    @Override
    public int getHeight(ImageObserver observer) {
        return baseSize;
    }

    @Override
    protected Image getBaseImage() {
        return getResolutionVariant(baseSize, baseSize);
    }

    @Override
    public Image getResolutionVariant(double width, double height) {
        // We only care about width since we don't support non-rectangular icons
        return resolutionVariants.computeIfAbsent(width, this::buildImage);
    }

    private Image buildImage(double size) {
        final Image img = color == null ?
                IconFontSwing.buildImage(iconCode, (float) size) :
                IconFontSwing.buildImage(iconCode, (float) size, color);
        // Center the icon in a square, cause that's what being asked
        final int actualSize = Math.max(img.getWidth(null), img.getHeight(null));
        final BufferedImage iconImg = new BufferedImage(actualSize, actualSize, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = iconImg.createGraphics();
        try {
            g2d.setColor(new Color(0, true));
            g2d.drawRect(0, 0, actualSize, actualSize);
            g2d.drawImage(
                    img,
                    (int) ((actualSize - img.getWidth(null)) / 2d),
                    (int) ((actualSize - img.getWidth(null)) / 2d),
                    new Color(0, true),
                    null);
            return iconImg;
        } finally {
            g2d.dispose();
        }
    }

    @Override
    public List<Image> getResolutionVariants() {
        return List.copyOf(resolutionVariants.values());
    }
}
