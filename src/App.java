import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.border.EmptyBorder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class App {
    JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel infoLabel;
    private JLabel pageLabel;

    private int currentPage = 1;
    private int rowsPerPage = 10;
    private int totalRows = 0;
    private int totalPages = 1;

    private int currentUserId;
    // Tambahkan field untuk menyimpan data buku yang sedang ditampilkan di tabel
    private List<Buku> currentTableData = new ArrayList<>();
    private List<Peminjaman> currentLaporanData = new ArrayList<>();

    public App(int userId) {
        this.currentUserId = userId; //Simpan ID pengguna
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Book Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 520);
        frame.setLayout(new BorderLayout());

        JLabel logoLabel = new JLabel("📚 Perpustakaan Sederhana", JLabel.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        logoLabel.setForeground(new Color(33, 102, 172));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(new Color(230, 242, 255));
        buttonPanel.setBorder(new EmptyBorder(18, 30, 18, 30));

        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);
        Dimension btnSize = new Dimension(170, 38);

        // ➤ Kelola Buku
        JButton manageBookBtn = new JButton("📚 Kelola Buku ▼");
        manageBookBtn.setIcon(UIManager.getIcon("FileView.directoryIcon"));
        manageBookBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        manageBookBtn.setBackground(new Color(220, 240, 255));
        manageBookBtn.setFont(buttonFont);
        manageBookBtn.setFocusPainted(false);
        manageBookBtn.setBorder(BorderFactory.createLineBorder(new Color(33, 102, 172), 1, true));
        manageBookBtn.setPreferredSize(btnSize);
        manageBookBtn.setMaximumSize(btnSize);
        JPopupMenu manageBookMenu = new JPopupMenu();
        JMenuItem addBookItem = new JMenuItem("Tambah Buku", UIManager.getIcon("FileChooser.newFolderIcon"));
        addBookItem.addActionListener(e -> showAddBookDialog());
        JMenuItem deleteBookItem = new JMenuItem("Hapus Buku", UIManager.getIcon("FileView.fileIcon"));
        deleteBookItem.addActionListener(e -> showDeleteBookDialog());
        manageBookMenu.add(addBookItem);
        manageBookMenu.add(deleteBookItem);
        manageBookBtn.addActionListener(e -> manageBookMenu.show(manageBookBtn, 0, manageBookBtn.getHeight()));
        buttonPanel.add(manageBookBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(12, 0)));

        // ➤ Navigasi & Filter
        JButton navBtn = new JButton("🔍 Navigasi & Filter ▼");
        navBtn.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        navBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        navBtn.setBackground(new Color(235, 255, 235));
        navBtn.setFont(buttonFont);
        navBtn.setFocusPainted(false);
        navBtn.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 51), 1, true));
        navBtn.setPreferredSize(btnSize);
        navBtn.setMaximumSize(btnSize);
        JPopupMenu navMenu = new JPopupMenu();
        JMenuItem searchBookItem = new JMenuItem("Cari Buku", UIManager.getIcon("FileView.fileIcon"));
        searchBookItem.addActionListener(e -> showSearchBookDialog());
        JMenuItem filterBookItem = new JMenuItem("Filter Buku", UIManager.getIcon("FileView.floppyDriveIcon"));
        filterBookItem.addActionListener(e -> showFilterBookDialog());
        JMenuItem sortBookItem = new JMenuItem("Sortir Buku", UIManager.getIcon("FileChooser.detailsViewIcon"));
        sortBookItem.addActionListener(e -> showSortBookDialog());
        navMenu.add(searchBookItem);
        navMenu.add(filterBookItem);
        navMenu.add(sortBookItem);
        navBtn.addActionListener(e -> navMenu.show(navBtn, 0, navBtn.getHeight()));
        buttonPanel.add(navBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(12, 0)));

        // ➤ Data IO
        JButton ioBtn = new JButton("💾 Data IO ▼");
        ioBtn.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
        ioBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        ioBtn.setBackground(new Color(255, 245, 220));
        ioBtn.setFont(buttonFont);
        ioBtn.setFocusPainted(false);
        ioBtn.setBorder(BorderFactory.createLineBorder(new Color(204, 153, 0), 1, true));
        ioBtn.setPreferredSize(btnSize);
        ioBtn.setMaximumSize(btnSize);
        JPopupMenu ioMenu = new JPopupMenu();
        JMenuItem importItem = new JMenuItem("Import CSV", UIManager.getIcon("FileChooser.upFolderIcon"));
        importItem.addActionListener(e -> importFromCSV());
        JMenuItem exportItem = new JMenuItem("Export CSV", UIManager.getIcon("FileChooser.homeFolderIcon"));
        exportItem.addActionListener(e -> exportToCSV());
        ioMenu.add(importItem);
        ioMenu.add(exportItem);
        ioBtn.addActionListener(e -> ioMenu.show(ioBtn, 0, ioBtn.getHeight()));
        buttonPanel.add(ioBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(12, 0)));

        // ➤ Layanan
        JButton layananBtn = new JButton("🛠️ Layanan ▼");
        layananBtn.setIcon(UIManager.getIcon("FileView.computerIcon"));
        layananBtn.setHorizontalTextPosition(SwingConstants.RIGHT);
        layananBtn.setBackground(new Color(245, 235, 255));
        layananBtn.setFont(buttonFont);
        layananBtn.setFocusPainted(false);
        layananBtn.setBorder(BorderFactory.createLineBorder(new Color(102, 0, 204), 1, true));
        layananBtn.setPreferredSize(btnSize);
        layananBtn.setMaximumSize(btnSize);
        JPopupMenu layananMenu = new JPopupMenu();
        JMenuItem borrowBookItem = new JMenuItem("Pinjam Buku", UIManager.getIcon("FileChooser.listViewIcon"));
        borrowBookItem.addActionListener(e -> showBorrowBookDialog());
        // Tambahkan menu laporan peminjaman ke dropdown layanan
        JMenuItem laporanPeminjamanItem = new JMenuItem("Laporan Peminjaman", UIManager.getIcon("FileView.fileIcon"));
        laporanPeminjamanItem.addActionListener(e -> showLaporanPeminjamanDialog());
        layananMenu.add(borrowBookItem);
        layananMenu.addSeparator();
        layananMenu.add(laporanPeminjamanItem);
        layananBtn.addActionListener(e -> layananMenu.show(layananBtn, 0, layananBtn.getHeight()));
        buttonPanel.add(layananBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(12, 0)));

        // Tombol Refresh tetap menonjol
        JButton refreshButton = new JButton("Refresh");
        refreshButton.setIcon(UIManager.getIcon("FileView.computerIcon"));
        refreshButton.setBackground(new Color(102, 204, 153));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setFont(buttonFont);
        refreshButton.setFocusPainted(false);
        refreshButton.setBorder(BorderFactory.createLineBorder(new Color(0, 153, 102), 1, true));
        refreshButton.setToolTipText("Muat ulang daftar buku");
        refreshButton.setPreferredSize(btnSize);
        refreshButton.setMaximumSize(btnSize);
        refreshButton.addActionListener(e -> {
            refreshTable();
            setInfo("Daftar buku dimuat ulang.", Color.BLUE);
        });
        buttonPanel.add(refreshButton);

        // Wrap buttonPanel in a FlowLayout panel to center it
        JPanel buttonPanelWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanelWrapper.setOpaque(false); // transparent so background color from northPanel shows
        buttonPanelWrapper.add(buttonPanel);

        infoLabel = new JLabel(" ");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(new Color(33, 102, 172));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(logoLabel, BorderLayout.NORTH);
        northPanel.add(buttonPanelWrapper, BorderLayout.CENTER); // use wrapper here
        northPanel.add(infoLabel, BorderLayout.SOUTH);
        frame.add(northPanel, BorderLayout.NORTH);

        String[] columns = {"Judul", "Pengarang", "Penerbit", "Kategori", "Tahun Terbit", "Edit", "Hapus"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5 || column == 6;
            }
        };
        table = new JTable(tableModel);
        table.getColumn("Edit").setCellRenderer(new ButtonRenderer());
        table.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Edit"));
        table.getColumn("Hapus").setCellRenderer(new ButtonRenderer());
        table.getColumn("Hapus").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Hapus"));
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(scrollPane, BorderLayout.CENTER);

        table.getTableHeader().setBackground(new Color(33, 102, 172));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(204, 229, 255));
        table.setSelectionForeground(Color.BLACK);
        table.getColumn("Edit").setMaxWidth(60);
        table.getColumn("Hapus").setMaxWidth(70);

        // Keyboard shortcut: F5 untuk refresh
        frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke("F5"), "refreshTable");
        frame.getRootPane().getActionMap().put("refreshTable", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refreshTable();
                setInfo("Daftar buku dimuat ulang.", Color.BLUE);
            }
        });

        pageLabel = new JLabel();
        JPanel pagingPanel = new JPanel();
        JButton prevButton = new JButton("Prev");
        JButton nextButton = new JButton("Next");

        // Dropdown untuk rows per page
        JComboBox<Integer> rowsPerPageCombo = new JComboBox<>(new Integer[]{5, 10, 20, 50, 100});
        rowsPerPageCombo.setSelectedItem(rowsPerPage);
        rowsPerPageCombo.setToolTipText("Jumlah baris per halaman");
        rowsPerPageCombo.addActionListener(e -> {
            rowsPerPage = (Integer) rowsPerPageCombo.getSelectedItem();
            currentPage = 1;
            refreshTable();
        });

        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshTable();
            }
        });
        nextButton.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                refreshTable();
            }
        });
        pagingPanel.add(prevButton);
        pagingPanel.add(pageLabel);
        pagingPanel.add(nextButton);
        pagingPanel.add(new JLabel("Baris/hal:"));
        pagingPanel.add(rowsPerPageCombo);
        frame.add(pagingPanel, BorderLayout.SOUTH);

        refreshTable();
    }

    private void setInfo(String message, Color color) {
        infoLabel.setText(message);
        infoLabel.setForeground(color);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        totalRows = countTotalRows();
        totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;
        currentTableData = ambilBukuPagingDariDatabase(currentPage, rowsPerPage); // simpan data yang ditampilkan
        for (Buku buku : currentTableData) {
            tableModel.addRow(new Object[]{
                buku.getJudul(),
                buku.getPengarang(),
                buku.getPenerbit(),
                buku.getKategori(),
                buku.getTahunTerbit(),
                "Edit",
                "Hapus"
            });
        }
        // Update label halaman
        pageLabel.setText("Halaman " + currentPage + " dari " + totalPages);
    }

    private void showAddBookDialog() {
        JDialog dialog = new JDialog(frame, "Tambah Buku", true);
        dialog.setSize(400, 400);
        dialog.setLayout(new GridLayout(8, 2, 5, 5));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField imageField = new JTextField();
        JLabel imagePreview = new JLabel(); // Preview gambar
        imagePreview.setHorizontalAlignment(JLabel.CENTER);
        imagePreview.setPreferredSize(new Dimension(120, 160));
        JButton browseButton = new JButton("Pilih Gambar");
        browseButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File selectedFile = fc.getSelectedFile();

                    // 1. Membuat folder 'images' jika belum ada
                    java.io.File destFolder = new java.io.File("images");
                    if (!destFolder.exists()) {
                        destFolder.mkdirs(); // Buat folder 'images' jika belum ada
                    }

                    // 2. Buat nama file yang unik untuk menghindari duplikat
                    String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    java.io.File destFile = new java.io.File(destFolder.getPath() + java.io.File.separator + uniqueFileName);

                    // 3. Salin file ke folder 'images'
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // 4. Simpan HANYA nama file unik di text field
                    imageField.setText(uniqueFileName);
                    
                    // 5. Tampilkan preview dari file yang sudah disalin
                    setImagePreview(imagePreview, uniqueFileName);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Gagal memproses gambar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        imageField.addActionListener(e -> setImagePreview(imagePreview, imageField.getText().trim()));

        dialog.add(new JLabel("Judul Buku:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Pengarang Buku:"));
        dialog.add(authorField);
        dialog.add(new JLabel("Penerbit Buku:"));
        dialog.add(publisherField);
        dialog.add(new JLabel("Kategori Buku:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Tahun Terbit:"));
        dialog.add(yearField);
        dialog.add(new JLabel("Gambar:"));
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(imageField, BorderLayout.CENTER);
        imagePanel.add(browseButton, BorderLayout.EAST);
        dialog.add(imagePanel);
        dialog.add(new JLabel("Preview:"));
        dialog.add(imagePreview);

        JButton saveButton = new JButton("Simpan");
        saveButton.addActionListener(e -> {
            String judul = titleField.getText().trim();
            String pengarang = authorField.getText().trim();
            String penerbit = publisherField.getText().trim();
            String kategori = categoryField.getText().trim();
            String tahunTerbit = yearField.getText().trim();
            String gambar = imageField.getText().trim();
            // Simpan path asli gambar
            Buku bukuBaru = new Buku(judul, pengarang, penerbit, kategori, tahunTerbit, gambar);
            if (isDuplicateInDatabase(bukuBaru)) {
                JOptionPane.showMessageDialog(dialog, "Data buku sudah ada (duplikat)!");
                return;
            }
            tambahBukuKeDatabase(bukuBaru);
            JOptionPane.showMessageDialog(dialog, "Buku berhasil ditambahkan.");
            dialog.dispose();
            refreshTable();
            setInfo("Buku berhasil ditambahkan.", new Color(0, 128, 0));
        });
        dialog.add(saveButton);

        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(saveButton);
        titleField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showDeleteBookDialog() {
        JDialog dialog = new JDialog(frame, "Hapus Buku", true);
        dialog.setSize(350, 300);
        dialog.setLayout(new GridLayout(6, 2, 5, 5));

        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField publisherField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField yearField = new JTextField();

        dialog.add(new JLabel("Judul Buku:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Pengarang Buku:"));
        dialog.add(authorField);
        dialog.add(new JLabel("Penerbit Buku:"));
        dialog.add(publisherField);
        dialog.add(new JLabel("Kategori Buku:"));
        dialog.add(categoryField);
        dialog.add(new JLabel("Tahun Terbit:"));
        dialog.add(yearField);

        JButton deleteButton = new JButton("Hapus");
        deleteButton.addActionListener(e -> {
            String judul = titleField.getText().trim();
            String pengarang = authorField.getText().trim();
            String penerbit = publisherField.getText().trim();
            String kategori = categoryField.getText().trim();
            String tahunTerbit = yearField.getText().trim();
            if (!judul.isEmpty() && !pengarang.isEmpty() && !penerbit.isEmpty() && !kategori.isEmpty() && !tahunTerbit.isEmpty()) {
                Buku buku = new Buku(judul, pengarang, penerbit, kategori, tahunTerbit, null);
                hapusBukuDariDatabase(buku);
                JOptionPane.showMessageDialog(dialog, "Buku berhasil dihapus (jika ada).");
                dialog.dispose();
                refreshTable();
                setInfo("Buku berhasil dihapus (jika ada).", new Color(0, 128, 0));
            } else {
                JOptionPane.showMessageDialog(dialog, "Semua field harus diisi untuk menghapus.");
            }
        });
        dialog.add(deleteButton);

        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(deleteButton);
        titleField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showSearchBookDialog() {
        JDialog dialog = new JDialog(frame, "Cari Buku", true);
        dialog.setSize(350, 200);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));

        JTextField searchField = new JTextField();
        JComboBox<String> searchType = new JComboBox<>(new String[]{"Judul", "Pengarang", "Penerbit", "Kategori", "Tahun Terbit"});

        dialog.add(new JLabel("Cari berdasarkan:"));
        dialog.add(searchType);
        dialog.add(new JLabel("Kata kunci:"));
        dialog.add(searchField);

        JButton searchButton = new JButton("Cari");
        searchButton.addActionListener(e -> {
            String keyword = searchField.getText().trim().toLowerCase();
            String type = (String) searchType.getSelectedItem();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Masukkan kata kunci pencarian.");
                return;
            }
            List<Buku> bukuList = cariBukuDiDatabase(type, keyword);
            tableModel.setRowCount(0); // Kosongkan tabel
            for (Buku buku : bukuList) {
                tableModel.addRow(new Object[]{
                    buku.getJudul(),
                    buku.getPengarang(),
                    buku.getPenerbit(),
                    buku.getKategori(),
                    buku.getTahunTerbit(),
                    "Edit",
                    "Hapus"
                });
            }
            if (bukuList.isEmpty()) {
                setInfo("Buku tidak ditemukan.", Color.RED);
            } else {
                setInfo("Ditemukan " + bukuList.size() + " buku untuk pencarian: " + keyword, new Color(0, 128, 0));
            }
            dialog.dispose();
        });
        dialog.add(searchButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> setInfo("", Color.BLUE));
        dialog.add(resetButton);

        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(searchButton);
        searchField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showFilterBookDialog() {
        JDialog dialog = new JDialog(frame, "Filter Buku", true);
        dialog.setSize(300, 180);
        dialog.setLayout(new GridLayout(4, 2, 5, 5));

        JComboBox<String> filterType = new JComboBox<>(new String[]{"Judul", "Pengarang", "Penerbit", "Kategori", "Tahun Terbit"});
        JTextField filterField = new JTextField();

        dialog.add(new JLabel("Filter berdasarkan:"));
        dialog.add(filterType);
        dialog.add(new JLabel("Nilai:"));
        dialog.add(filterField);

        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(e -> {
            String type = (String) filterType.getSelectedItem();
            String value = filterField.getText().trim().toLowerCase();
            List<Buku> bukuList = cariBukuDiDatabase(type, value);
            tableModel.setRowCount(0); // Kosongkan tabel
            for (Buku buku : bukuList) {
                tableModel.addRow(new Object[]{
                    buku.getJudul(),
                    buku.getPengarang(),
                    buku.getPenerbit(),
                    buku.getKategori(),
                    buku.getTahunTerbit(),
                    "Edit",
                    "Hapus"
                });
            }
            if (bukuList.isEmpty()) {
                setInfo("Tidak ada buku yang sesuai filter.", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Tidak ada buku yang sesuai filter.");
            } else {
                setInfo("Ditemukan " + bukuList.size() + " buku hasil filter.", new Color(0, 128, 0));
            }
            dialog.dispose();
        });
        dialog.add(filterButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            setInfo("", Color.BLUE);
            refreshTable();
        });
        dialog.add(resetButton);

        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(filterButton);
        filterField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void showSortBookDialog() {
        JDialog dialog = new JDialog(frame, "Sortir Buku", true);
        dialog.setSize(300, 120);
        dialog.setLayout(new GridLayout(2, 2, 5, 5));

        JComboBox<String> sortType = new JComboBox<>(new String[]{"Judul", "Pengarang", "Penerbit", "Kategori", "Tahun Terbit"});

        dialog.add(new JLabel("Urutkan berdasarkan:"));
        dialog.add(sortType);

        JButton sortButton = new JButton("Urutkan");
        sortButton.addActionListener(e -> {
            String type = (String) sortType.getSelectedItem();
            List<Buku> bukuList = ambilSemuaBukuSortirDariDatabase(type);
            tableModel.setRowCount(0); // Kosongkan tabel
            for (Buku buku : bukuList) {
                tableModel.addRow(new Object[]{
                    buku.getJudul(),
                    buku.getPengarang(),
                    buku.getPenerbit(),
                    buku.getKategori(),
                    buku.getTahunTerbit(),
                    "Edit",
                    "Hapus"
                });
            }
            if (bukuList.isEmpty()) {
                setInfo("Daftar buku kosong, tidak ada yang bisa diurutkan.", Color.RED);
                JOptionPane.showMessageDialog(dialog, "Daftar buku kosong, tidak ada yang bisa diurutkan.");
            } else {
                setInfo("Buku diurutkan berdasarkan " + type + ".", new Color(0, 128, 0));
            }
            dialog.dispose();
        });
        dialog.add(sortButton);

        JButton cancelButton = new JButton("Batal");
        cancelButton.addActionListener(e -> dialog.dispose());
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(sortButton);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public void showEditBookDialog(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentTableData.size()) return;
        Buku buku = currentTableData.get(rowIndex);

        // Perbesar ukuran window edit buku
        JDialog dialog = new JDialog(frame, "Edit Buku", true);
        dialog.setSize(600, 420); // Ubah ukuran di sini sesuai kebutuhan

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(buku.getJudul(), 16);
        JTextField authorField = new JTextField(buku.getPengarang(), 16);
        JTextField publisherField = new JTextField(buku.getPenerbit(), 16);
        JTextField categoryField = new JTextField(buku.getKategori(), 16);
        JTextField yearField = new JTextField(buku.getTahunTerbit(), 16);
        JTextField imageField = new JTextField(buku.getGambar(), 16);
        JLabel imagePreview = new JLabel();
        imagePreview.setHorizontalAlignment(JLabel.RIGHT);
        imagePreview.setPreferredSize(new Dimension(120, 140));
        setImagePreview(imagePreview, buku.getGambar());
        JButton browseButton = new JButton("Pilih Gambar");
        browseButton.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.io.File selectedFile = fc.getSelectedFile();

                    // 2. Siapkan folder tujuan
                    java.io.File destFolder = new java.io.File("images");
                    if (!destFolder.exists()) {
                        destFolder.mkdirs(); // Buat folder 'images' jika belum ada
                    }

                    // 3. Buat nama file yang unik untuk menghindari duplikat
                    String uniqueFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                    java.io.File destFile = new java.io.File(destFolder.getPath() + java.io.File.separator + uniqueFileName);

                    // 4. Salin file ke folder 'images'
                    Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    // 5. Simpan HANYA nama file unik di text field
                    imageField.setText(uniqueFileName);
                    
                    // 6. Tampilkan preview dari file yang sudah disalin
                    setImagePreview(imagePreview, uniqueFileName);

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Gagal memproses gambar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        imageField.addActionListener(e -> setImagePreview(imagePreview, imageField.getText().trim()));
        JButton deleteImageButton = new JButton("Hapus Gambar");
        deleteImageButton.addActionListener(e -> {
            imageField.setText("");
            setImagePreview(imagePreview, "");
        });

        // Baris 0
        gbc.gridx = 2; gbc.gridy = 0; panel.add(new JLabel("Judul Buku:"), gbc);
        gbc.gridx = 3; gbc.gridy = 0; panel.add(titleField, gbc);

        // Baris 1
        gbc.gridx = 2; gbc.gridy = 1; panel.add(new JLabel("Pengarang Buku:"), gbc);
        gbc.gridx = 3; gbc.gridy = 1; panel.add(authorField, gbc);

        // Baris 2
        gbc.gridx = 2; gbc.gridy = 2; panel.add(new JLabel("Penerbit Buku:"), gbc);
        gbc.gridx = 3; gbc.gridy = 2; panel.add(publisherField, gbc);

        // Baris 3
        gbc.gridx = 2; gbc.gridy = 3; panel.add(new JLabel("Kategori Buku:"), gbc);
        gbc.gridx = 3; gbc.gridy = 3; panel.add(categoryField, gbc);

        // Baris 4
        gbc.gridx = 2; gbc.gridy = 4; panel.add(new JLabel("Tahun Terbit:"), gbc);
        gbc.gridx = 3; gbc.gridy = 4; panel.add(yearField, gbc);

        // Baris 5
        gbc.gridx = 2; gbc.gridy = 5; panel.add(new JLabel("Gambar:"), gbc);
        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(imageField, BorderLayout.CENTER);
        imagePanel.add(browseButton, BorderLayout.EAST);
        gbc.gridx = 3; gbc.gridy = 5; panel.add(imagePanel, gbc);

        // Baris 6
        gbc.gridx = 2; gbc.gridy = 6; panel.add(new JLabel("Preview:"), gbc);
        gbc.gridx = 3; gbc.gridy = 6; panel.add(imagePreview, gbc);

        // Baris 7
        gbc.gridx = 3; gbc.gridy = 7; gbc.anchor = GridBagConstraints.EAST; panel.add(deleteImageButton, gbc);

        // Baris 8 (tombol)
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveButton = new JButton("Simpan");
        JButton cancelButton = new JButton("Batal");
        btnPanel.add(saveButton);
        btnPanel.add(cancelButton);
        gbc.gridx = 3; gbc.gridy = 8; gbc.anchor = GridBagConstraints.EAST; panel.add(btnPanel, gbc);

        saveButton.addActionListener(e -> {
            String judul = titleField.getText().trim();
            String pengarang = authorField.getText().trim();
            String penerbit = publisherField.getText().trim();
            String kategori = categoryField.getText().trim();
            String tahunTerbit = yearField.getText().trim();
            String gambar = imageField.getText().trim();
            // Simpan path asli gambar
            Buku bukuBaru = new Buku(buku.getId(), judul, pengarang, penerbit, kategori, tahunTerbit, gambar);
            updateBukuDiDatabase(buku, bukuBaru);
            dialog.dispose();
            refreshTable();
            setInfo("Buku berhasil diupdate.", new Color(0, 128, 0));
            JOptionPane.showMessageDialog(frame, "Buku berhasil diupdate.");
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.getContentPane().add(panel);
        dialog.getRootPane().setDefaultButton(saveButton);
        titleField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    public void confirmAndDeleteBook(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentTableData.size()) return;
        Buku buku = currentTableData.get(rowIndex);
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Apakah Anda yakin ingin menghapus buku berikut?\n\n" + buku.toString(),
            "Konfirmasi Hapus Buku",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            hapusBukuDariDatabase(buku);
            refreshTable();
            setInfo("Buku berhasil dihapus.", new Color(0, 128, 0));
            JOptionPane.showMessageDialog(frame, "Buku berhasil dihapus.");
        }
    }

    private void showBorrowBookDialog() {
        JDialog dialog = new JDialog(frame, "Pinjam Buku", true);
        dialog.setSize(350, 180);
        dialog.setLayout(new GridLayout(4, 2, 8, 8)); // 4 baris, 2 kolom, jarak antar komponen

        JTextField borrowerField = new JTextField();
        JTextField titleField = new JTextField();
        JTextField borrowDateField = new JTextField(java.time.LocalDate.now().toString());

        dialog.add(new JLabel("Nama Peminjam:"));
        dialog.add(borrowerField);
        dialog.add(new JLabel("Judul Buku:"));
        dialog.add(titleField);
        dialog.add(new JLabel("Tanggal Pinjam (YYYY-MM-DD):"));
        dialog.add(borrowDateField);

        JButton borrowButton = new JButton("Pinjam");
        JButton cancelButton = new JButton("Batal");

        borrowButton.addActionListener(e -> {
            String namaPeminjam = borrowerField.getText().trim();
            String judul = titleField.getText().trim();
            String tanggalPinjam = borrowDateField.getText().trim();
            if (!namaPeminjam.isEmpty() && !judul.isEmpty() && !tanggalPinjam.isEmpty()) {
                Buku buku = null;
                for (Buku b : ambilSemuaBukuDariDatabase()) {
                    if (b.getJudul().equalsIgnoreCase(judul)) {
                        buku = b;
                        break;
                    }
                }
                if (buku == null) {
                    JOptionPane.showMessageDialog(dialog, "Judul buku tidak ditemukan di database.");
                    return;
                }
                pinjamBuku(
                   buku.getId(),
                   namaPeminjam,
                   tanggalPinjam
                );
                JOptionPane.showMessageDialog(dialog, "Buku berhasil dipinjam.");
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Semua field harus diisi.");
            }
        });
        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(borrowButton);
        dialog.add(cancelButton);

        dialog.getRootPane().setDefaultButton(borrowButton);
        borrowerField.requestFocusInWindow();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan sebagai CSV");
        int userSelection = fileChooser.showSaveDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            try (java.io.PrintWriter pw = new java.io.PrintWriter(fileToSave)) {
                pw.println("Judul, Pengarang, Penerbit, Kategori, Tahun Terbit");
                for (Buku buku : ambilSemuaBukuDariDatabase()) {
                    pw.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        buku.getJudul().replace("\"", "\"\""),
                        buku.getPengarang().replace("\"", "\"\""),
                        buku.getPenerbit().replace("\"", "\"\""),
                        buku.getKategori().replace("\"", "\"\""),
                        buku.getTahunTerbit().replace("\"", "\"\"")
                    );
                }
                JOptionPane.showMessageDialog(frame, "Data berhasil diexport ke CSV!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Gagal export: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Pilih file CSV untuk import");
        int userSelection = fileChooser.showOpenDialog(frame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToOpen = fileChooser.getSelectedFile();

            JDialog progressDialog = new JDialog(frame, "Mengimpor...", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressDialog.add(new JLabel("Sedang mengimpor data, mohon tunggu..."), BorderLayout.NORTH);
            progressDialog.add(progressBar, BorderLayout.CENTER);
            progressDialog.setSize(300, 80);
            progressDialog.setLocationRelativeTo(frame);

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                int imported = 0, skipped = 0;
                @Override
                protected Void doInBackground() {
                    try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(fileToOpen))) {
                        String line = br.readLine(); // skip header
                        while ((line = br.readLine()) != null) {
                            String[] parts = line.split("\",\"");
                            if (parts.length == 5) {
                                String judul = parts[0].replaceAll("^\"|\"$", "");
                                String pengarang = parts[1].replaceAll("^\"|\"$", "");
                                String penerbit = parts[2].replaceAll("^\"|\"$", "");
                                String kategori = parts[3].replaceAll("^\"|\"$", "");
                                String tahunTerbit = parts[4].replaceAll("^\"|\"$", "");
                                Buku buku = new Buku(judul, pengarang, penerbit, kategori, tahunTerbit, null);
                                if (!isDuplicateInDatabase(buku)) {
                                    tambahBukuKeDatabase(buku);
                                    imported++;
                                } else {
                                    skipped++;
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Gagal import: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                    return null;
                }
                @Override
                protected void done() {
                    progressDialog.dispose();
                    refreshTable();
                    JOptionPane.showMessageDialog(frame, "Import selesai!\nBerhasil: " + imported + "\nDuplikat: " + skipped);
                }
            };
            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    // --- Database Helper Methods ---

    private void tambahBukuKeDatabase(Buku buku) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO buku (judul, pengarang, penerbit, kategori, tahun_terbit, gambar, user_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, buku.getJudul());
            ps.setString(2, buku.getPengarang());
            ps.setString(3, buku.getPenerbit());
            ps.setString(4, buku.getKategori());
            ps.setString(5, buku.getTahunTerbit());
            ps.setString(6, buku.getGambar());
            ps.setInt(7, this.currentUserId); // Bind user_id
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusBukuDariDatabase(Buku buku) {
        try (Connection conn = DBUtil.getConnection()) {
            // Tidak perlu hapus file gambar fisik
            String sql = "DELETE FROM buku WHERE judul=? AND pengarang=? AND penerbit=? AND kategori=? AND tahun_terbit=? AND user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, buku.getJudul());
            ps.setString(2, buku.getPengarang());
            ps.setString(3, buku.getPenerbit());
            ps.setString(4, buku.getKategori());
            ps.setString(5, buku.getTahunTerbit());
            ps.setInt(6, this.currentUserId);
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<Buku> ambilSemuaBukuDariDatabase() {
        List<Buku> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM buku WHERE user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.currentUserId); // Bind user_id
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Buku(
                    rs.getInt("id"),
                    rs.getString("judul"),
                    rs.getString("pengarang"),
                    rs.getString("penerbit"),
                    rs.getString("kategori"),
                    rs.getString("tahun_terbit"),
                    rs.getString("gambar")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private List<Buku> cariBukuDiDatabase(String kolom, String keyword) {
        List<Buku> list = new ArrayList<>();
        String dbKolom = "";
        switch (kolom) {
            case "Judul": dbKolom = "judul"; break;
            case "Pengarang": dbKolom = "pengarang"; break;
            case "Penerbit": dbKolom = "penerbit"; break;
            case "Kategori": dbKolom = "kategori"; break;
            case "Tahun Terbit": dbKolom = "tahun_terbit"; break;
        }
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM buku WHERE LOWER(" + dbKolom + ") LIKE ? AND user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            ps.setInt(2, this.currentUserId); // Bind user_id
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Buku(
                    rs.getInt("id"),
                    rs.getString("judul"),
                    rs.getString("pengarang"),
                    rs.getString("penerbit"),
                    rs.getString("kategori"),
                    rs.getString("tahun_terbit"),
                    rs.getString("gambar")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private List<Buku> ambilSemuaBukuSortirDariDatabase(String kolom) {
        List<Buku> list = new ArrayList<>();
        String dbKolom = "";
        switch (kolom) {
            case "Judul": dbKolom = "judul"; break;
            case "Pengarang": dbKolom = "pengarang"; break;
            case "Penerbit": dbKolom = "penerbit"; break;
            case "Kategori": dbKolom = "kategori"; break;
            case "Tahun Terbit": dbKolom = "tahun_terbit"; break;
        }
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM buku WHERE user_id=? ORDER BY " + dbKolom + " ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.currentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Buku(
                    rs.getInt("id"),
                    rs.getString("judul"),
                    rs.getString("pengarang"),
                    rs.getString("penerbit"),
                    rs.getString("kategori"),
                    rs.getString("tahun_terbit"),
                    rs.getString("gambar")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private void updateBukuDiDatabase(Buku bukuLama, Buku bukuBaru) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE buku SET judul=?, pengarang=?, penerbit=?, kategori=?, tahun_terbit=?, gambar=? " +
                         "WHERE judul=? AND pengarang=? AND penerbit=? AND kategori=? AND tahun_terbit=? AND user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bukuBaru.getJudul());
            ps.setString(2, bukuBaru.getPengarang());
            ps.setString(3, bukuBaru.getPenerbit());
            ps.setString(4, bukuBaru.getKategori());
            ps.setString(5, bukuBaru.getTahunTerbit());
            ps.setString(6, bukuBaru.getGambar());
            ps.setString(7, bukuLama.getJudul());
            ps.setString(8, bukuLama.getPengarang());
            ps.setString(9, bukuLama.getPenerbit());
            ps.setString(10, bukuLama.getKategori());
            ps.setString(11, bukuLama.getTahunTerbit());
            ps.setInt(12, this.currentUserId); // Bind user_id
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isDuplicateInDatabase(Buku buku) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT COUNT(*) FROM buku WHERE judul=? AND pengarang=? AND penerbit=? AND kategori=? AND tahun_terbit=? AND user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, buku.getJudul());
            ps.setString(2, buku.getPengarang());
            ps.setString(3, buku.getPenerbit());
            ps.setString(4, buku.getKategori());
            ps.setString(5, buku.getTahunTerbit());
            ps.setInt(6, this.currentUserId); // Bind user_id
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    private List<Buku> ambilBukuPagingDariDatabase(int page, int rowsPerPage) {
        List<Buku> list = new ArrayList<>();
        int offset = (page - 1) * rowsPerPage;
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT * FROM buku WHERE user_id=? LIMIT ? OFFSET ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.currentUserId); // Bind user_id
            ps.setInt(2, rowsPerPage);
            ps.setInt(3, offset);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Buku(
                    rs.getInt("id"),
                    rs.getString("judul"),
                    rs.getString("pengarang"),
                    rs.getString("penerbit"),
                    rs.getString("kategori"),
                    rs.getString("tahun_terbit"),
                    rs.getString("gambar")
                ));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return list;
    }

    private int countTotalRows() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT COUNT(*) FROM buku WHERE user_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.currentUserId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    private void pinjamBuku(int bukuId, String namaPeminjam, String tanggalPinjam) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO peminjaman (buku_id, user_id, nama_peminjam, tanggal_pinjam, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, bukuId);
            ps.setInt(2, this.currentUserId); // ID pustakawan/user yg login
            ps.setString(3, namaPeminjam);
            ps.setString(4, tanggalPinjam);
            ps.setString(5, "Dipinjam");
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Metode ini dipanggil oleh ButtonEditor saat tombol "Kembalikan" di laporan diklik
    public void confirmAndReturnBook(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= currentLaporanData.size()) return;
        
        Peminjaman pinjaman = currentLaporanData.get(rowIndex);
        
        int confirm = JOptionPane.showConfirmDialog(
            frame,
            "Yakin ingin mengembalikan buku '" + pinjaman.getJudulBuku() + "'?",
            "Konfirmasi Pengembalian",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            kembalikanBuku(pinjaman.getId()); // Panggil metode kembalikanBuku dengan ID peminjaman
            showLaporanPeminjamanDialog(); // Refresh dialog laporan
        }
    }

    // Metode kembalikanBuku yang baru dan lebih aman
    private void kembalikanBuku(int peminjamanId) {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE peminjaman SET status=?, tanggal_kembali=? WHERE id=? AND status='Dipinjam'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "Dikembalikan");
            ps.setString(2, java.time.LocalDate.now().toString());
            ps.setInt(3, peminjamanId); // Gunakan ID unik dari transaksi peminjaman
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(frame, "Buku berhasil dikembalikan.");
            } else {
                JOptionPane.showMessageDialog(frame, "Gagal mengembalikan buku atau buku sudah dikembalikan.", "Info", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Terjadi kesalahan database:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showLaporanPeminjamanDialog() {
        JDialog dialog = new JDialog(frame, "Laporan Peminjaman Buku", true);
        dialog.setSize(850, 450);
        dialog.setLayout(new BorderLayout());

        // Tambahkan kolom baru untuk tombol "Kembalikan"
        String[] columns = {"Judul", "Pengarang", "Nama Peminjam", "Tgl Pinjam", "Tgl Kembali", "Status", "Aksi"};
        DefaultTableModel laporanModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Hanya kolom "Aksi" yang bisa diklik
            }
        };
        JTable laporanTable = new JTable(laporanModel);
        
        // Atur renderer & editor untuk tombol "Kembalikan"
        laporanTable.getColumn("Aksi").setCellRenderer(new ButtonRenderer());
        laporanTable.getColumn("Aksi").setCellEditor(new ButtonEditor(new JCheckBox(), this, "Kembalikan"));
        laporanTable.getColumn("Aksi").setMaxWidth(100);

        currentLaporanData.clear(); // Kosongkan data lama

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT p.id as peminjaman_id, b.id as buku_id, b.judul, b.pengarang, p.nama_peminjam, p.tanggal_pinjam, p.tanggal_kembali, p.status " +
                        "FROM peminjaman p JOIN buku b ON p.buku_id = b.id " +
                        "WHERE p.user_id = ? ORDER BY p.status ASC, p.tanggal_pinjam DESC";
                        
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, this.currentUserId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // Tambahkan data ke tabel model
                laporanModel.addRow(new Object[]{
                    rs.getString("judul"),
                    rs.getString("pengarang"),
                    rs.getString("nama_peminjam"),
                    rs.getString("tanggal_pinjam"),
                    rs.getString("tanggal_kembali") == null ? "-" : rs.getString("tanggal_kembali"),
                    rs.getString("status"),
                    rs.getString("status").equals("Dipinjam") ? "Kembalikan" : "" // Tampilkan tombol jika status "Dipinjam"
                });
                // Simpan data peminjaman ke list
                currentLaporanData.add(new Peminjaman(rs.getInt("peminjaman_id"), rs.getInt("buku_id"), rs.getString("judul")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Gagal mengambil data laporan: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

        JScrollPane scrollPane = new JScrollPane(laporanTable);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Tutup");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }

    private void setImagePreview(JLabel label, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            label.setIcon(null);
            label.setText("Tidak ada gambar");
            return;
        }
        try {
            // Cari file di dalam folder "images"
            java.io.File imgFile = new java.io.File("images" + java.io.File.separator + fileName);
            
            if (imgFile.exists() && imgFile.isFile()) {
                ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(100, 140, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
                label.setText("");
            } else {
                label.setIcon(null);
                label.setText("Tidak ada gambar");
            }
        } catch (Exception e) {
            label.setIcon(null);
            label.setText("Gagal load gambar");
        }
    }

    public static void main(String[] args) {
        // Mulai aplikasi dari Dashboard Utama
        //SwingUtilities.invokeLater(App::new);
        // Mulai aplikasi dengan menampilkan frame login
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

// Kelas Buku
class Buku {
    private int id;
    private String judul;
    private String pengarang;
    private String penerbit;
    private String kategori;
    private String tahunTerbit;
    private String gambar; // path gambar

    public Buku(String judul, String pengarang, String penerbit, String kategori, String tahunTerbit, String gambar) {
        this.judul = judul;
        this.pengarang = pengarang;
        this.penerbit = penerbit;
        this.kategori = kategori;
        this.tahunTerbit = tahunTerbit;
        this.gambar = gambar;
    }

    public Buku(int id, String judul, String pengarang, String penerbit, String kategori, String tahunTerbit, String gambar) {
        this(judul, pengarang, penerbit, kategori, tahunTerbit, gambar);
        this.id = id;
    }


    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getPengarang() { return pengarang; }
    public String getPenerbit() { return penerbit; }
    public String getKategori() { return kategori; }
    public String getTahunTerbit() { return tahunTerbit; }
    public String getGambar() { return gambar; }

    public void setJudul(String judul) { this.judul = judul; }
    public void setPengarang(String pengarang) { this.pengarang = pengarang; }
    public void setPenerbit(String penerbit) { this.penerbit = penerbit; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public void setTahunTerbit(String tahunTerbit) { this.tahunTerbit = tahunTerbit; }
    public void setGambar(String gambar) { this.gambar = gambar; }

    @Override
    public String toString() {
        return "Judul: " + judul + ", Pengarang: " + pengarang +
               ", Penerbit: " + penerbit + ", Kategori: " + kategori +
               ", Tahun Terbit: " + tahunTerbit;
    }
}

//Kelas Peminjaman
class Peminjaman {
    private int id;
    private int bukuId;
    private String judulBuku; // Kita simpan untuk display

    public Peminjaman(int id, int bukuId, String judulBuku) {
        this.id = id;
        this.bukuId = bukuId;
        this.judulBuku = judulBuku;
    }

    public int getId() { return id; }
    public String getJudulBuku() { return judulBuku; }
}
// Renderer untuk tombol di JTable
class ButtonRenderer extends JButton implements TableCellRenderer {
    private String text;

    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        setText(value != null ? value.toString() : "");
        return this;
    }
}

// Editor untuk tombol di JTable
class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String actionType;
    private int row;
    private App app;

    public ButtonEditor(JCheckBox checkBox, App app, String actionType) {
        super(checkBox);
        this.app = app;
        this.actionType = actionType;
        button = new JButton(actionType);
        button.addActionListener(e -> {
            if ("Edit".equals(actionType)) {
                app.showEditBookDialog(row);
            } else if ("Hapus".equals(actionType)) {
                app.confirmAndDeleteBook(row);
            } else if ("Kembalikan".equals(actionType)) { 
                app.confirmAndReturnBook(row);
            }
            fireEditingStopped();
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        this.row = row;
        button.setText(actionType); // Selalu pakai label tombol, bukan value dari model
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        return actionType; // Pastikan value yang dikembalikan adalah label tombol, bukan boolean
    }
}

// Utility Koneksi Database
class DBUtil {
    public static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/perpustakaan";
        String user = "root";
        String pass = ""; // default XAMPP password kosong
        return DriverManager.getConnection(url, user, pass);
    }
}
