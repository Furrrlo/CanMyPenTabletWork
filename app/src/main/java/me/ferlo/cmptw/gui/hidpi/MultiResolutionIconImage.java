package me.ferlo.cmptw.gui.hidpi;

import java.awt.*;
import java.awt.image.AbstractMultiResolutionImage;
import java.awt.image.ImageObserver;
import java.util.*;
import java.util.List;

public class MultiResolutionIconImage extends AbstractMultiResolutionImage {

    private final int baseSize;
    private final Map<Integer, Image> resolutionVariants = new HashMap<>();
    private final List<Image> unmodifiableResolutionVariants;

    public MultiResolutionIconImage(int baseSize, Collection<? extends Image> resolutionVariants) {
        this.baseSize = baseSize;
        this.unmodifiableResolutionVariants = List.copyOf(resolutionVariants);
        resolutionVariants.forEach(image -> this.resolutionVariants.put(image.getWidth(null), image));
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
        return resolutionVariants.computeIfAbsent((int) width, size -> unmodifiableResolutionVariants.stream()
                .filter(i -> i.getWidth(null) >= (int) width)
                .min(Comparator.comparingInt(e -> e.getWidth(null) - (int) width))
                // Fallback to the closest one
                .orElseGet(() -> unmodifiableResolutionVariants.stream()
                        .min(Comparator.comparingInt(e -> Math.abs(e.getWidth(null) - (int) width)))
                        .orElse(null)));
    }

    @Override
    public List<Image> getResolutionVariants() {
        return unmodifiableResolutionVariants;
    }
}
