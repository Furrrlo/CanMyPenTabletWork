package me.ferlo.cmptw.gui;

import jiconfont.icons.font_awesome.FontAwesome;
import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconFont;
import me.ferlo.cmptw.gui.tabbed.JComboBoxTabbedPane;
import me.ferlo.cmptw.hook.Hook;
import me.ferlo.cmptw.hook.HookService;
import me.ferlo.cmptw.hook.KeyboardHookService;
import me.ferlo.cmptw.process.ProcessService;
import me.ferlo.cmptw.script.ScriptEngine;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.oxbow.swingbits.dialog.task.TaskDialogs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

class HooksPane extends JComboBoxTabbedPane<ListenableValue<Hook>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HooksPane.class);

    private final KeyboardHookService keyboardHookService;
    private final ScriptEngine scriptEngine;
    private final ProcessService processService;
    private final HookService hookService;

    private final Hook dummyHook = new Hook(new Hook.Device("", "", ""), Collections.emptyList(), Hook.FallbackBehavior.IGNORE);

    private final JButton addDeviceBtn;
    private final JButton removeDeviceBtn;

    private final JButton applyBtn;
    private final JButton cancelBtn;

    private final ListenableValue<List<Hook>> currentConfig = new ListenableValue<>(new ArrayList<>());

    public HooksPane(HookService hookService,
                     KeyboardHookService keyboardHookService,
                     ScriptEngine scriptEngine,
                     ProcessService processService) {
        super(DefaultOption::new);

        this.keyboardHookService = keyboardHookService;
        this.scriptEngine = scriptEngine;
        this.processService = processService;
        this.hookService = hookService;

        setDefaultRenderer(new BasicComboBoxRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof ListenableValue v && v.get() instanceof Hook hook && c instanceof JLabel label)
                    label.setText(hook.device().name());
                return c;
            }
        });

        addDeviceBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.PLUS, 14, new Color(0, 150, 0))));
        addDeviceBtn.setMargin(new Insets(2, 2, 2, 2));
        addDeviceBtn.addActionListener(evt -> SelectDeviceDialog
                .selectDevice(SwingUtilities.windowForComponent(this), keyboardHookService)
                .thenAccept(device -> SwingUtilities.invokeLater(() -> {
                    if(device == null)
                        return;

                    final Optional<Hook> maybeHook = getOptions().stream()
                            .map(Option::getKey)
                            .map(ListenableValue::get)
                            .filter(d -> d.device().id().equals(device.getId()))
                            .findFirst();
                    if(maybeHook.isPresent()) {
                        JOptionPane.showMessageDialog(
                                SwingUtilities.windowForComponent(this),
                                String.format("Device was already added as \"%s\"", maybeHook.get().device().name()),
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    final var hook = new Hook(
                            new Hook.Device(device.getId(), device.getDesc(), device.getDesc()),
                            Collections.emptyList(),
                            Hook.FallbackBehavior.IGNORE);
                    currentConfig.update(l -> {
                        l.add(hook);
                        return l;
                    });
                    addHookComponent(hook);
                })));

        removeDeviceBtn = new JButton(new ImageIcon(new MultiResolutionIconFont(FontAwesome.MINUS, 14, new Color(150, 0, 0))));
        removeDeviceBtn.addActionListener(evt -> {
            final var selectedOption = getSelectedOption();
            if(selectedOption == null)
                return;

            currentConfig.update(l -> {
                l.remove(selectedOption.getKey().get());
                return l;
            });
            removeOption(selectedOption);
        });
        removeDeviceBtn.setMargin(new Insets(2, 2, 2, 2));
        selectedOption().addListener((oldOpt, newOpt) -> removeDeviceBtn.setEnabled(!newOpt.getKey().get().equals(dummyHook)));

        applyBtn = new JButton("Apply");
        currentConfig.addListener((oldV, newV) -> applyBtn.setEnabled(true));
        applyBtn.addActionListener(evt -> {
            try {
                hookService.save(currentConfig.get());
            } catch (IOException ex) {
                LOGGER.error("Failed to save config", ex);
                TaskDialogs.showException(new Exception("Failed to save config", ex));
                return;
            }

            applyBtn.setEnabled(false);
        });

        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(evt -> {
            if(!applyBtn.isEnabled())
                return;

            currentConfig.update(l -> new ArrayList<>());
            reloadSavedHooks();
        });

        addHookComponent(dummyHook);
        reloadSavedHooks();
    }

    @Override
    protected void layoutComponent(JPanel mainPanel, JComboBox<?> comboBox, Component currentPanel) {
        setLayout(new MigLayout(
                new LC().wrapAfter(1).fill().maxWidth("100%").maxHeight("100%"),
                new AC().grow().align("center"),
                new AC().gap().grow().gap().grow(0)
        ));

        add(comboBox, new CC().growX().split(3));
        add(addDeviceBtn);
        add(removeDeviceBtn);

        add(currentPanel, new CC().grow().pushY());

        add(applyBtn, new CC().tag("apply").split(2));
        add(cancelBtn, new CC().tag("cancel"));
    }

    private void addHookComponent(Hook hookIn) {
        final var hook = new ListenableValue<>(hookIn);
        final var device = new ListenableValue<>(hook.get().device());
        device.addListener((oldV, newV) -> hook.update(h -> h.withDevice(newV)));

        final var panel = new DevicePanel(keyboardHookService, scriptEngine, processService, hook, device);
        // If it's the dummy one, disable everything
        if(hookIn == dummyHook) {
            final Deque<Component> components = new LinkedList<>(List.of(panel));
            while(!components.isEmpty()) {
                final var component = components.pop();
                component.setEnabled(false);
                if(component instanceof Container container)
                    Collections.addAll(components, container.getComponents());
            }
        }

        addOption(hook, panel);
        hook.addListener((oldV, newV) -> {
            currentConfig.update(l -> {
                final int idx;
                if((idx = l.indexOf(oldV)) != -1)
                    l.set(idx, newV);
                return l;
            });

            revalidate();
            repaint();
        });
    }

    private void reloadSavedHooks() {
        new ArrayList<>(getOptions()).forEach(option -> {
            if(!option.getKey().get().equals(dummyHook))
                removeOption(option);
        });

        hookService.getSaved().forEach(hook -> {
            currentConfig.update(l -> {
                l.add(hook);
                return l;
            });
            addHookComponent(hook);
        });
        if(getOptions().size() > 1)
            setSelectedOption(1);
        applyBtn.setEnabled(false);
    }
}
