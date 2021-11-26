package me.ferlo.cmptw.gui.hidpi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.concurrent.TimeUnit;

class WaitForWindowReleaseRunnable extends ComponentAdapter implements Runnable {

    private final Component component;
    private final Runnable then;
    private GraphicsConfiguration graphicsConfig;

    private long lastMoveNanos;
    private Point lastMousePosition;

    public WaitForWindowReleaseRunnable(Component component, Runnable then) {
        this.component = component;
        this.then = then;
        this.graphicsConfig = component.getGraphicsConfiguration();
        component.addComponentListener(this);
    }

    @Override
    public void run() {
        final Point currMousePos = MouseInfo.getPointerInfo().getLocation();
        final GraphicsConfiguration currGraphicsConfig = component.getGraphicsConfiguration();
        if(!graphicsConfig.equals(currGraphicsConfig)) {
            lastMousePosition = MouseInfo.getPointerInfo().getLocation();
            graphicsConfig = currGraphicsConfig;
            SwingUtilities.invokeLater(this);
            return;
        }

        if(lastMousePosition.distance(currMousePos) < 20) {
            SwingUtilities.invokeLater(this);
            return;
        }

        if(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - lastMoveNanos) < 400) {
            SwingUtilities.invokeLater(this);
            return;
        }

        component.removeComponentListener(this);
        then.run();
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        lastMoveNanos = System.nanoTime();
        lastMousePosition = MouseInfo.getPointerInfo().getLocation();
    }
}
