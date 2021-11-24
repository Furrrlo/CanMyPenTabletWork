package me.ferlo.cmptw.gui;

import me.ferlo.cmptw.gui.hidpi.MultiResolutionIconImage;
import me.ferlo.cmptw.process.Process;
import me.ferlo.cmptw.process.ProcessService;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.jdesktop.swingx.JXTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SelectProcessDialog extends JDialog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectProcessDialog.class);
    private static final ExecutorService BACKGROUND_ICON_LOADER = Executors.newFixedThreadPool(5, new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            final var th = Executors.defaultThreadFactory().newThread(r);
            th.setName("cmptw-background-process-icon-loader-" + count.getAndIncrement());
            th.setDaemon(true);
            th.setUncaughtExceptionHandler((t, e) -> LOGGER.error("Icon background loading thread crashed", e));
            return th;
        }
    });

    private final ProcessService processService;
    private final CompletableFuture<Process> future;

    public SelectProcessDialog(ProcessService processService,
                               CompletableFuture<Process> future,
                               boolean processInfo) {
        super(null, ModalityType.APPLICATION_MODAL);
        this.processService = processService;
        this.future = future;
        init(processInfo);
    }

    private SelectProcessDialog(Frame owner,
                                ProcessService processService,
                                CompletableFuture<Process> future,
                                boolean processInfo) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.processService = processService;
        this.future = future;
        init(processInfo);
    }

    public SelectProcessDialog(Dialog owner,
                               ProcessService processService,
                               CompletableFuture<Process> future,
                               boolean processInfo) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.processService = processService;
        this.future = future;
        init(processInfo);
    }

    public SelectProcessDialog(Window owner,
                               ProcessService processService,
                               CompletableFuture<Process> future,
                               boolean processInfo) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.processService = processService;
        this.future = future;
        init(processInfo);
    }

    private void init(boolean processInfo) {
        setTitle("Select Process");

        final JPanel contentPane = new JPanel();
        contentPane.setLayout(new MigLayout(new LC().fill().flowY().align("center", "center")));

        final List<Process> processes = processService.enumerateProcesses().stream()
//                .collect(Collectors.toMap(Process::iconPath, Function.identity(), (e1, e2) -> e1))
//                .values().stream()
                .sorted(Comparator.comparing(Process::name))
                .toList();

        final JXTable processTable = new JXTable();
        processTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        processTable.setHorizontalScrollEnabled(true);
        processTable.setFillsViewportHeight(true);
        processTable.setRowMargin(0);
        processTable.setIntercellSpacing(new Dimension(0, 0));
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        processTable.setShowGrid(false);
        processTable.setShowHorizontalLines(false);
        processTable.setShowVerticalLines(false);
        final AbstractTableModel processTableModel;
        processTable.setModel(processTableModel = new AbstractTableModel() {

            private static final String[] COLUMN_NAMES = { "PID", "Icon", "Name", "File" };
            private static final Class<?>[] COLUMN_CLASSES = { int.class, ImageIcon.class, String.class, String.class };

            private final ConcurrentMap<Process, ImageIcon> processIcons = new ConcurrentHashMap<>();

            @Override
            public int getRowCount() {
                return processes.size();
            }

            @Override
            public int getColumnCount() {
                return COLUMN_NAMES.length;
            }

            @Override
            public String getColumnName(int column) {
                return COLUMN_NAMES[column];
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return COLUMN_CLASSES[columnIndex];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                final Process process = processes.get(rowIndex);
                return switch (columnIndex) {
                    case -1 -> process;
                    case 0 -> process.pid();
                    case 1 -> processIcons.computeIfAbsent(process, p -> {
                        // Schedule its loading
                        BACKGROUND_ICON_LOADER.submit(() -> {
                            final ImageIcon loadedIcon = new ImageIcon(new MultiResolutionIconImage(
                                    16,
                                    processService.extractProcessIcons(process.iconPath())));
                            processIcons.put(process, loadedIcon);
                            // Trigger reload
                            fireTableCellUpdated(rowIndex, columnIndex);
                        });

                        return new ImageIcon(new MultiResolutionIconImage(
                                16,
                                processService.getFallbackIcons()));
                    });
                    case 2 -> process.name();
                    case 3 -> process.iconPath().toAbsolutePath().toString();
                    default -> throw new IndexOutOfBoundsException("Index: " + columnIndex + ", size: " + COLUMN_NAMES.length);
                };
            }
        });
        processTable.setRowSorter(new TableRowSorter<>(processTableModel));
        processTableModel.fireTableDataChanged();
        contentPane.add(new JScrollPane(processTable), new CC().grow().pushY());

        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new MigLayout(new LC().insetsAll("0").fillX()));

        final JButton browseBtn;
        if(!processInfo) {
            browseBtn = null;
        } else {
            browseBtn = new JButton("Browse");
            browseBtn.addActionListener(evt -> {
                final var chooser = new JFileChooser();
                final var res = chooser.showOpenDialog(this);
                if (res != JFileChooser.APPROVE_OPTION)
                    return;

                final var process = new FileBasedProcess(chooser.getSelectedFile().getAbsoluteFile().toPath());
                future.complete(process);
                setVisible(false);
            });
        }

        final JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(evt -> {
            future.complete(null);
            setVisible(false);
        });

        final JButton addBtn = new JButton("Apply");
        addBtn.setEnabled(processTable.getSelectedRow() != -1);
        processTable.getSelectionModel().addListSelectionListener(evt -> addBtn.setEnabled(processTable.getSelectedRow() != -1));
        addBtn.addActionListener(evt -> {
            final int selectedRowIdx = processTable.getSelectedRow();
            if(selectedRowIdx == -1)
                return;

            final var process = (Process) processTable.getValueAt(selectedRowIdx, -1);
            future.complete(process);
            setVisible(false);
        });

        buttonsPanel.add(cancelBtn, new CC().tag("cancel").split(browseBtn == null ? 2 : 3));
        buttonsPanel.add(addBtn, new CC().tag("apply"));
        if(browseBtn != null)
            buttonsPanel.add(browseBtn);
        getRootPane().setDefaultButton(addBtn);

        contentPane.add(buttonsPanel, new CC().growX());

        add(contentPane);

        setSize(700, 500);
        processTable.packAll();
        setLocationRelativeTo(getOwner());

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    @Override
    public void dispose() {
        super.dispose();
        future.complete(null);
    }

    public static CompletableFuture<Process> selectDevice(ProcessService processService) {
        return selectDevice(processService, false);
    }

    public static CompletableFuture<Process> selectDevice(ProcessService processService, boolean processInfo) {
        final var future = new CompletableFuture<Process>();
        new SelectProcessDialog(processService, future, processInfo).setVisible(true);
        return future;
    }

    public static CompletableFuture<Process> selectDevice(Frame owner, ProcessService processService) {
        return selectDevice(owner, processService, false);
    }

    public static CompletableFuture<Process> selectDevice(Frame owner,
                                                          ProcessService processService,
                                                          boolean processInfo) {
        final var future = new CompletableFuture<Process>();
        new SelectProcessDialog(owner, processService, future, processInfo).setVisible(true);
        return future;
    }

    public static CompletableFuture<Process> selectDevice(Dialog owner, ProcessService processService) {
        return selectDevice(owner, processService, false);
    }

    public static CompletableFuture<Process> selectDevice(Dialog owner,
                                                          ProcessService processService,
                                                          boolean processInfo) {
        final var future = new CompletableFuture<Process>();
        new SelectProcessDialog(owner, processService, future, processInfo).setVisible(true);
        return future;
    }

    public static CompletableFuture<Process> selectDevice(Window owner, ProcessService processService) {
        return selectDevice(owner, processService, false);
    }

    public static CompletableFuture<Process> selectDevice(Window owner,
                                                          ProcessService processService,
                                                          boolean processInfo) {
        final var future = new CompletableFuture<Process>();
        new SelectProcessDialog(owner, processService, future, processInfo).setVisible(true);
        return future;
    }

    private static record FileBasedProcess(Path processFile) implements Process {

        @Override
        public int pid() {
            return -1;
        }

        @Override
        public String name() {
            return processFile.getFileName().toString();
        }

        @Override
        public Path iconPath() {
            return processFile;
        }
    }
}
