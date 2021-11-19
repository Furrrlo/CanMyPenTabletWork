package me.ferlo.cmptw.gui.tabbed;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class JMyTabbedPane<T extends JTabbedPaneTab> extends javax.swing.JTabbedPane {

    private final Collection<T> tabs = new HashSet<>();
    private final Collection<T> unmodifiableTabs = Collections.unmodifiableCollection(tabs);

    private final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    private boolean dontRedirectSuperCalls = false;

    public Collection<T> getTabs() {
        return unmodifiableTabs;
    }

    public T getTabForIndex(int index) {
        // Bounds check
        getComponentAt(index);
        return getTabs().stream()
                .filter(t -> t.tabIndex == index)
                .findFirst()
                .orElseThrow(() -> new AssertionError(String.format(
                        "Missing tab even if it's in bounds (index: %s, size: %s)",
                        index, getTabCount())));
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        // TODO
//        insertTab(new JTabbedPaneTab(title, icon, component, tip), index);
        throw new UnsupportedOperationException("JTabbedPane#insertTab(String title, Icon icon, Component component, String tip, int index)");
    }

    public T addTab(T tab) {
        return insertTab(tab, getTabCount());
    }

    public T insertTab(T tab, int index) {
        tab.tabbedPane = this;
        tab.tabIndex = index;

        tabs.stream()
                .filter(t -> t.tabIndex >= index)
                .forEach(t -> t.tabIndex = t.tabIndex + 1);
        tabs.add(tab);

        try {
            dontRedirectSuperCalls = true;
            super.insertTab(tab.getTitle(), tab.getIcon(), tab.getComponent(), tab.getTooltipText(), index);
        } finally {
            dontRedirectSuperCalls = false;
        }

        if(tab.getTabComponent() != null)
            super.setTabComponentAt(index, tab.getTabComponent());

        tab.title().addListener((oldV, newV) -> super.setTitleAt(index, newV));
        tab.tooltipText().addListener((oldV, newV) -> super.setToolTipTextAt(index, newV));
        tab.icon().addListener((oldV, newV) -> super.setIconAt(index, newV));
        tab.tabComponent().addListener((oldV, newV) -> super.setTabComponentAt(index, newV));
        tab.component().addListener((oldV, newV) -> super.setComponentAt(index, newV));

        return tab;
    }

    @Override
    public void removeTabAt(int index) {
        tabs.removeIf(t -> {
            if(t.tabIndex == index) {
                t.tabIndex = -1;
                return true;
            }

            if(t.tabIndex > index)
                t.tabIndex = t.tabIndex - 1;
            return false;
        });

        try {
            dontRedirectSuperCalls = true;
            super.removeTabAt(index);
        } finally {
            dontRedirectSuperCalls = false;
        }
    }

    public T getSelectedTab() {
        return getTabForIndex(getSelectedIndex());
    }

    @Override
    public void setTitleAt(int index, String title) {
        if(dontRedirectSuperCalls && isCalledByJTabbedPane()) {
            super.setTitleAt(index, title);
            return;
        }

        getTabForIndex(index).setTitle(title);
    }

    @Override
    public void setIconAt(int index, Icon icon) {
        if(dontRedirectSuperCalls && isCalledByJTabbedPane()) {
            super.setIconAt(index, icon);
            return;
        }

        getTabForIndex(index).setIcon(icon);
    }

    @Override
    public void setToolTipTextAt(int index, String toolTipText) {
        if(dontRedirectSuperCalls && isCalledByJTabbedPane()) {
            super.setToolTipTextAt(index, toolTipText);
            return;
        }

        getTabForIndex(index).setTooltipText(toolTipText);
    }

    @Override
    public void setComponentAt(int index, Component component) {
        if(dontRedirectSuperCalls && isCalledByJTabbedPane()) {
            super.setComponentAt(index, component);
            return;
        }

        getTabForIndex(index).setComponent(component);
    }

    @Override
    public void setTabComponentAt(int index, Component component) {
        if(dontRedirectSuperCalls && isCalledByJTabbedPane()) {
            super.setTabComponentAt(index, component);
            return;
        }

        getTabForIndex(index).setTabComponent(component);
    }

    private boolean isCalledByJTabbedPane() {
        final var maybeClass = walker.walk(s -> s
                .skip(2)
                .map(StackWalker.StackFrame::getDeclaringClass)
                .findFirst());
        return maybeClass.isPresent() && maybeClass.get().equals(JTabbedPane.class);
    }

}
