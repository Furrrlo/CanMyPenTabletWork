package me.ferlo.cmptw.gui.tabbed;

import me.ferlo.cmptw.gui.ListenableValue;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JTabbedPaneTab {

    JMyTabbedPane<?> tabbedPane;
    int tabIndex;

    private final ListenableValue<String> title;
    private final ListenableValue<Icon> icon;
    private final ListenableValue<Component> tabComponent;
    private final ListenableValue<Component> component;
    private final ListenableValue<String> tooltipText;

    public JTabbedPaneTab(String title, Component component) {
        this(title, null, component);
    }

    public JTabbedPaneTab(String title, Icon icon, Component component) {
        this(title, icon, component, null);
    }

    public JTabbedPaneTab(String title,
                          Icon icon,
                          Component component,
                          String tooltipText) {
        this(title, icon, null, component, tooltipText);
    }

    public JTabbedPaneTab(String title,
                          Icon icon,
                          Component tabComponent,
                          Component component,
                          String tooltipText) {
        this.title = new ListenableValue<>(title);
        this.tooltipText = new ListenableValue<>(tooltipText);
        this.icon = new ListenableValue<>(icon);
        this.tabComponent = new ListenableValue<>(tabComponent);
        this.component = new ListenableValue<>(component);
    }

    public void select() {
        tabbedPane.setSelectedIndex(tabIndex);
    }

    public void remove() {
        tabbedPane.removeTabAt(tabIndex);
    }

    public ListenableValue<String> title() {
        return title;
    }

    public String getTitle() {
        return title().get();
    }

    public void setTitle(String title) {
        title().set(title);
    }

    public ListenableValue<Icon> icon() {
        return icon;
    }

    public Icon getIcon() {
        return icon().get();
    }

    public void setIcon(Icon icon) {
        icon().set(icon);
    }

    public ListenableValue<Component> tabComponent() {
        return tabComponent;
    }

    public Component getTabComponent() {
        return tabComponent().get();
    }

    public void setTabComponent(Component tabComponent) {
        tabComponent().set(tabComponent);
    }

    public ListenableValue<Component> component() {
        return component;
    }

    public Component getComponent() {
        return component().get();
    }

    public void setComponent(Component component) {
        component().set(component);
    }

    public ListenableValue<String> tooltipText() {
        return tooltipText;
    }

    public String getTooltipText() {
        return tooltipText().get();
    }

    public void setTooltipText(String tooltipText) {
        tooltipText().set(tooltipText);
    }
}
