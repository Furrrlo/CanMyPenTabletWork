package me.ferlo.cmptw.gui.hidpi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

/** Fixes https://bugs.java.com/bugdatabase/view_bug.do?bug_id=JDK-8221452 */
@SuppressWarnings("unused")
public class JFrameHiDpi extends JFrame {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private Dimension unscaledMinimumSize;
    private Dimension lastSize;
    private Dimension forcedSize;

    private PropertyChangeListener propertyChangeListener;
    private ComponentListener componentListener;
    private boolean isDropListenerRunning;

    public JFrameHiDpi() throws HeadlessException {
    }

    public JFrameHiDpi(GraphicsConfiguration gc) {
        super(gc);
    }

    public JFrameHiDpi(String title) throws HeadlessException {
        super(title);
    }

    public JFrameHiDpi(String title, GraphicsConfiguration gc) {
        super(title, gc);
    }

    private void onGraphicsConfigurationChange(PropertyChangeEvent evt) {
        final var lastSize = this.lastSize;
        super.setMinimumSize(getScaledMinimumSize(unscaledMinimumSize));

        if(!isDropListenerRunning) {
            isDropListenerRunning = true;
            forcedSize = lastSize;
            SwingUtilities.invokeLater(new WaitForWindowReleaseRunnable(this, () -> {
                forcedSize = null;
                isDropListenerRunning = false;
            }));
        }
    }

    @Override
    public void setMinimumSize(Dimension minimumSize) {
        if(!IS_WINDOWS) {
            super.setMinimumSize(minimumSize);
            return;
        }

        unscaledMinimumSize = minimumSize;
        if(minimumSize != null) {
            super.setMinimumSize(getScaledMinimumSize(minimumSize));
            setSize(minimumSize);
            addPropertyChangeListener("graphicsConfiguration", propertyChangeListener = this::onGraphicsConfigurationChange);
            addComponentListener(componentListener = new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    lastSize = e.getComponent().getSize();
                    if(forcedSize != null)
                        setSize(forcedSize);
                }
            });
        } else {
            super.setMinimumSize(null);
            removePropertyChangeListener("graphicsConfiguration", propertyChangeListener);
            removeComponentListener(componentListener);
        }
    }

    private Dimension getScaledMinimumSize(Dimension minimumSize) {
        // The transform is the same one used for rendering scaled icons
        // (see SunGraphics2D#getResolutionVariant(...)), so it has to be correct
        final AffineTransform transform = getGraphicsConfiguration().getDefaultTransform();
        if(transform.isIdentity())
            return minimumSize;

        return new Dimension(
                (int) (minimumSize.getWidth() * transform.getScaleX()),
                (int) (minimumSize.getHeight() * transform.getScaleY()));
    }

    @Override
    public Dimension getMinimumSize() {
        final boolean calledFromUpdateMinimumSize = STACK_WALKER.walk(stackFrames -> stackFrames
                .skip(1)
                .findFirst()
                .map(stackFrame -> stackFrame.getMethodName().equals("updateMinimumSize") &&
                        stackFrame.getMethodType().descriptorString().equals("()V") &&
                        stackFrame.getClassName().equals("sun.awt.windows.WWindowPeer"))
                .orElse(false));
        return calledFromUpdateMinimumSize ?
                getScaledMinimumSize(unscaledMinimumSize) :
                unscaledMinimumSize;
    }
}
