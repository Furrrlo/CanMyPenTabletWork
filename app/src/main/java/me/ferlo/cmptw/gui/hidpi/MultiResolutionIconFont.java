package me.ferlo.cmptw.gui.hidpi;

import jiconfont.IconCode;
import jiconfont.swing.IconFontSwing;

import java.awt.*;
import java.awt.image.AbstractMultiResolutionImage;
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
        return color == null ?
                IconFontSwing.buildImage(iconCode, (float) size) :
                IconFontSwing.buildImage(iconCode, (float) size, color);
    }

    @Override
    public List<Image> getResolutionVariants() {
        return List.copyOf(resolutionVariants.values());
    }
}
