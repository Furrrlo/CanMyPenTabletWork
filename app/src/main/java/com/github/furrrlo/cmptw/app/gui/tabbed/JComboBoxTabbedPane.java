package com.github.furrrlo.cmptw.app.gui.tabbed;

import com.github.furrrlo.cmptw.app.gui.ListenableValue;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.util.*;
import java.util.List;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public class JComboBoxTabbedPane<T> extends JPanel {

    private final JComboBox<Option<T>> comboBox;
    private final DefaultComboBoxModel<Option<T>> comboBoxModel;
    private final JPanel currentPanel;

    private final List<Option<T>> options = new ArrayList<>();
    private final ListenableValue<Option<T>> selectedOption = new ListenableValue<>();

    private final OptionFactory<T> optionFactory;
    private ListCellRenderer<Object> defaultRenderer;

    private boolean hasLaidOutComponent = false;

    public JComboBoxTabbedPane(OptionFactory<T> optionFactory) {
        this.optionFactory = optionFactory;

        currentPanel = new JPanel();
        currentPanel.setLayout(new MigLayout(new LC().fill().insetsAll("0"), new AC().align("center"), new AC().align("center")));

        comboBox = new JComboBox<>(comboBoxModel = new DefaultComboBoxModel<>());
        comboBox.setRenderer(new BasicComboBoxRenderer() {
            @Override
            @SuppressWarnings("unchecked")
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if(value instanceof Option) {
                    final Option<T> option = (Option<T>) value;
                    final ListCellRenderer<Object> renderer = option.getComboBoxRenderer().orElse(defaultRenderer);
                    if(renderer != null)
                        return renderer.getListCellRendererComponent(list, option.getKey(), index, isSelected, cellHasFocus);
                }
                if(defaultRenderer != null)
                    return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        comboBox.addActionListener(evt -> SwingUtilities.invokeLater(() -> {
            final Object tmp = comboBox.getSelectedItem();
            if(tmp instanceof final JComboBoxTabbedPane.Option option)
                //noinspection unchecked
                setSelectedOption(option);
        }));
        selectedOption.addListener((oldV, newV) -> setSelectedOption(newV));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void layout() {
        if(!hasLaidOutComponent) {
            layoutComponent(this, comboBox, currentPanel);
            hasLaidOutComponent = true;
        }

        super.layout();
    }

    protected void layoutComponent(JPanel mainPanel, JComboBox<?> comboBox, Component currentPanel) {
        mainPanel.setLayout(new MigLayout(
                new LC().wrapAfter(1).fill().maxWidth("100%").maxHeight("100%"),
                new AC().grow().align("center"),
                new AC().gap().grow()
        ));

        mainPanel.add(comboBox, new CC().growX());
        mainPanel.add(currentPanel, new CC().grow());
    }

    public ListCellRenderer<Object> getDefaultRenderer() {
        return defaultRenderer;
    }

    public void setDefaultRenderer(ListCellRenderer<Object> defaultRenderer) {
        this.defaultRenderer = defaultRenderer;
    }

    public Option<T> addOption(T key, JComponent component) {
        return addOption(key, null, component);
    }

    public Option<T> addOption(T key, ListCellRenderer<Object> comboBoxRenderer, JComponent component) {
        final Option<T> option = optionFactory.create(key, comboBoxModel.getSize(), comboBoxRenderer, component);
        options.add(option);
        comboBoxModel.addElement(option);
        comboBoxModel.setSelectedItem(option);
        return option;
    }

    public Option<T> removeOption(Option<T> option) {
        options.remove(option);

        final int idx;
        if(Objects.equals(comboBoxModel.getSelectedItem(), option) && (idx = comboBoxModel.getIndexOf(option)) > 0) {
            final var newSelectedItem = comboBox.getItemAt(idx - 1);
            comboBoxModel.setSelectedItem(newSelectedItem);
            comboBoxModel.removeElement(option);
            return newSelectedItem;
        }

        comboBoxModel.removeElement(option);
        return null;
    }

    public List<Option<T>> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public Option<T> getSelectedOption() {
        return selectedOption().get();
    }

    public void setSelectedOption(int index) {
        setSelectedOption(comboBox.getItemAt(index));
    }

    public void setSelectedOption(Option<T> option) {
        final var component = option.getComponent();
        while(currentPanel.getComponentCount() > 0)
            currentPanel.remove(0);
        currentPanel.add(component, new CC().grow());

        if(!Objects.equals(selectedOption.get(), option))
            selectedOption.set(option);
        if(!Objects.equals(comboBox.getSelectedItem(), option))
            comboBox.setSelectedItem(option);

        revalidate();
        repaint();
    }

    public ListenableValue<Option<T>> selectedOption() {
        return selectedOption;
    }

    public interface Option<T> {

        T getKey();

        JComponent getComponent();

        Optional<ListCellRenderer<Object>> getComboBoxRenderer();
    }

    public interface OptionFactory<T> {

        Option<T> create(T key, int idx, ListCellRenderer<Object> comboBoxRenderer, JComponent component);
    }

    public static class DefaultOption<T> implements Option<T> {

        private final T key;
        private int idx;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private Optional<ListCellRenderer<Object>> comboBoxRenderer;
        private JComponent component;

        public DefaultOption(T key, int idx, ListCellRenderer<Object> comboBoxRenderer, JComponent component) {
            this.key = key;
            this.idx = idx;
            this.comboBoxRenderer = Optional.ofNullable(comboBoxRenderer);
            this.component = component;
        }

        @Override
        public T getKey() {
            return key;
        }

        @Override
        public JComponent getComponent() {
            return component;
        }

        public void setComponent(JComponent component) {
            this.component = component;
        }

        @Override
        public Optional<ListCellRenderer<Object>> getComboBoxRenderer() {
            return comboBoxRenderer;
        }

        public void setComboBoxRenderer(ListCellRenderer<Object> comboBoxRenderer) {
            this.comboBoxRenderer = Optional.ofNullable(comboBoxRenderer);
        }
    }
}
