package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.tabbed.JTabbedPaneTab;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JTabbedPaneIconTab extends JTabbedPaneTab {

    protected static final int TAB_ICON_SIZE = 32;

    public JTabbedPaneIconTab(String title, Component component) {
        super(title, component);
    }

    public JTabbedPaneIconTab(String title, Icon icon, Component component) {
        super(title, icon, component);
    }

    public JTabbedPaneIconTab(String title, Icon icon, Component component, String tooltipText) {
        super(title, icon, component, tooltipText);
    }

    public JTabbedPaneIconTab(String title, Icon icon, Component tabComponent, Component component, String tooltipText) {
        super(title, icon, tabComponent, component, tooltipText);
    }

    protected static Component tabComponentFor(String title, Image image) {
        if(image == null)
            image = createTransparentImage(TAB_ICON_SIZE, TAB_ICON_SIZE);

        final JPanel tabPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                // Instead of setting the component opaque, do this
                // otherwise DarkLaf tries to fix a bug with antialiasing and ends up drawing the text with a bg
                // See https://github.com/weisJ/darklaf/blob/69ea0119a1f9f98dadf50e955c8e5e862a3f18b3/core/src/main/java/com/github/weisj/darklaf/graphics/StringPainter.java#L131
            }
        };
        tabPanel.setLayout(new MigLayout(new LC().flowY().fill().insetsAll("1").gridGap("1", "1")));

        final JLabel iconLabel = new JLabel(new ImageIcon(image));
        tabPanel.add(iconLabel, new CC().alignX("center"));

        final JLabel nameLabel = new JLabel(title);
        nameLabel.setMaximumSize(new Dimension(64, (int) nameLabel.getMaximumSize().getHeight()));
        tabPanel.add(nameLabel, new CC().alignX("center"));

        return tabPanel;
    }

    private static BufferedImage createTransparentImage(int width, int height) {
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setBackground(new Color(0, true));
        graphics.clearRect(0, 0, width, height);
        graphics.dispose();

        return bufferedImage;
    }
}
