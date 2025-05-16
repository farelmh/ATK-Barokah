package admin;

import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatTanggal;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class lap_pembelian extends javax.swing.JFrame {

    private PreparedStatement statPembelian;
    private PreparedStatement statDetail;
    private ResultSet rsPembelian;
    private ResultSet rsDetail;
    private ExpandableTableModel tableModel;
    private List<Pembelian> daftarPembelian;
    private Map<String, List<DetailPembelian>> detailPembelianMap;
    private boolean[] expandedRows;
    private final int[] index = {0, 1, 2, 3};
    private final int[] indexUang = {4};
    konek k = new konek();

    public lap_pembelian() {
        initComponents();
        this.setLocationRelativeTo(null);
        k.connect();
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Pencarian"));
        isiTabel();
        tableModel = new ExpandableTableModel();
        tbl_penjualan.setModel(tableModel);
        setupTableAppearance();
        setScrollBar();
       CariData.TableSorter(tbl_penjualan, txt_cari, index, indexUang, null);
    }

    private String getTanggalMulai() {
        try {
            Date tgl = tgl_mulai.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private String getTanggalAkhir() {
        try {
            Date tgl = tgl_akhir.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    class Pembelian {

        private String id;
        private Date tanggal;
        private String namaKasir;
        private String namaSupplier;
        double total;

        public Pembelian(String id, String namaKasir, Date tanggal, double total, String namaSupplier) {
            this.id = id;
            this.tanggal = tanggal;
            this.namaKasir = namaKasir;
            this.total = total;
            this.namaSupplier = namaSupplier;
        }

        public String getId() {
            return id;
        }

        public Date getTanggal() {
            return tanggal;
        }

        public String getNamaKasir() {
            return namaKasir;
        }

        public double getTotal() {
            return total;
        }

        public String getNamaSupplier() {
            return namaSupplier;
        }
    }

    class DetailPembelian {

        private String idPembelian;
        private String namaBarang;
        private int jumlah;
        private double subtotal;

        public DetailPembelian(String idPembelian, String namaBarang, int jumlah, double subtotal) {
            this.idPembelian = idPembelian;
            this.namaBarang = namaBarang;
            this.jumlah = jumlah;
            this.subtotal = subtotal;
        }

        public String getIdPenjualan() {
            return idPembelian;
        }

        public String getNamaBarang() {
            return namaBarang;
        }

        public int getJumlah() {
            return jumlah;
        }

        public double getSubtotal() {
            return subtotal;
        }

    }

    private void setupTableAppearance() {
        // Set renderer untuk kolom tombol expand/collapse
        tbl_penjualan.getColumnModel().getColumn(5).setCellRenderer(new ExpandButtonRenderer());
        tbl_penjualan.getColumnModel().getColumn(5).setCellEditor(new ExpandButtonEditor());

        // Set lebar kolom
        TableColumnModel columnModel = tbl_penjualan.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(80);  // ID Penjualan
        columnModel.getColumn(1).setPreferredWidth(80);  // Tanggal
        columnModel.getColumn(2).setPreferredWidth(140);  // Nama Kasir
        columnModel.getColumn(3).setPreferredWidth(140);  // Nama Supplier
        columnModel.getColumn(4).setPreferredWidth(120);  // Total Penjualan
        columnModel.getColumn(5).setPreferredWidth(50);   // Tombol Expand 

        // Set tampilan header dan sorting
        JTableHeader header = tbl_penjualan.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(230, 230, 250));
        header.setForeground(new Color(50, 50, 50));

        // Set tampilan baris
        tbl_penjualan.setRowHeight(30);
        tbl_penjualan.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl_penjualan.setGridColor(new Color(220, 220, 220));
        tbl_penjualan.setShowGrid(true);
        tbl_penjualan.setIntercellSpacing(new Dimension(1, 1));

        // Set warna latar belakang baris
        tbl_penjualan.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                boolean isExpanded = false;
                int modelRow = table.convertRowIndexToModel(row);

                if (isDetailRow(modelRow)) {
                    // Warna untuk baris detail
                    setBackground(new Color(204, 204, 255));
                    setFont(new Font("Segoe UI", Font.ITALIC, 13));
                } else {
                    // Warna untuk baris utama
                    if (modelRow % 2 == 0) {
                        setBackground(new Color(255, 255, 255));
                    } else {
                        setBackground(new Color(248, 248, 248));
                    }
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                if (isSelected) {
                    setBackground(new Color(51, 153, 255));
                    setForeground(Color.WHITE);
                } else {
                    setForeground(Color.BLACK);
                }

                return comp;
            }
        });

        // Double click untuk expand/collapse
        tbl_penjualan.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tbl_penjualan.rowAtPoint(e.getPoint());
                    int col = tbl_penjualan.columnAtPoint(e.getPoint());
                    if (row >= 0 && !isDetailRow(tbl_penjualan.convertRowIndexToModel(row))) {
                        expandCollapseRow(tbl_penjualan.convertRowIndexToModel(row));
                    }
                }
            }
        });
    }

    private void expandAllRows() {
        for (int i = 0; i < daftarPembelian.size(); i++) {
            if (!expandedRows[i]) {
                expandCollapseRow(getRowIndex(i, false));
            }
        }
    }

    private void collapseAllRows() {
        for (int i = 0; i < daftarPembelian.size(); i++) {
            if (expandedRows[i]) {
                expandCollapseRow(getRowIndex(i, true));
            }
        }
    }

    private int getRowIndex(int penjualanIndex, boolean isExpanded) {
        int index = penjualanIndex;
        for (int i = 0; i < penjualanIndex; i++) {
            if (expandedRows[i]) {
                index += detailPembelianMap.get(daftarPembelian.get(i).getId()).size();
            }
        }
        return index;
    }

    private boolean isDetailRow(int row) {
        int currentRow = 0;
        for (int i = 0; i < daftarPembelian.size(); i++) {
            if (currentRow == row) {
                return false;
            }
            currentRow++;

            if (expandedRows[i]) {
                List<DetailPembelian> details = detailPembelianMap.get(daftarPembelian.get(i).getId());
                for (int j = 0; j < details.size(); j++) {
                    if (currentRow == row) {
                        return true;
                    }
                    currentRow++;
                }
            }
        }
        return false;
    }

    private void expandCollapseRow(int row) {
        int penjualanIndex = getPenjualanIndex(row);
        if (penjualanIndex >= 0) {
            expandedRows[penjualanIndex] = !expandedRows[penjualanIndex];
            tableModel.fireTableDataChanged();
        }
    }

    private int getPenjualanIndex(int row) {
        int currentRow = 0;
        for (int i = 0; i < daftarPembelian.size(); i++) {
            if (currentRow == row) {
                return i;
            }
            currentRow++;

            if (expandedRows[i]) {
                currentRow += detailPembelianMap.get(daftarPembelian.get(i).getId()).size();
            }
        }
        return -1;
    }

    public void resetPencarian() {
        tgl_mulai.setDate(null);
        tgl_akhir.setDate(null);
        txt_cari.setText("");
        isiTabel(); // Panggil method isiTabel() yang sudah ada untuk memuat semua data
        ((ExpandableTableModel) tbl_penjualan.getModel()).fireTableDataChanged();
        CariData.resetTableSorting(tbl_penjualan);
    }

    private void cariDataTanggal(String mulai, String akhir) { //belom
        daftarPembelian.clear();
        detailPembelianMap.clear();

        daftarPembelian = new ArrayList<>();
        detailPembelianMap = new HashMap<>();

        try {
            this.statPembelian = k.getCon().prepareStatement("CALL getDataPembelian(?, ?)");
            this.statPembelian.setString(1, mulai);
            this.statPembelian.setString(2, akhir);

            this.rsPembelian = this.statPembelian.executeQuery();
            while (rsPembelian.next()) {
                String id = rsPembelian.getString("id_beli");
                Date tanggal = rsPembelian.getDate("tanggal_beli");
                String kasir = rsPembelian.getString("nama_karyawan");
                double total = rsPembelian.getDouble("total");
                String supplier = rsPembelian.getString("nama_supplier");

                Pembelian pembelian = new Pembelian(id, kasir, tanggal, total, supplier);
                daftarPembelian.add(pembelian);

               this.statDetail = k.getCon().prepareStatement("CALL getDetailBeli(?)");
                statDetail.setString(1, id);
                this.rsDetail = this.statDetail.executeQuery();

                List<DetailPembelian> listDetail = new ArrayList<>();
                while (rsDetail.next()) {
                    String namaBarang = rsDetail.getString("nama_barang");
                    int jumlah = rsDetail.getInt("jumlah");
                    double subtotal = rsDetail.getDouble("total");

                    listDetail.add(new DetailPembelian(id, namaBarang, jumlah, subtotal));
                }
                detailPembelianMap.put(id, listDetail);

                rsDetail.close();
                statDetail.close();
            }

            // Inisialisasi status expanded
            expandedRows = new boolean[daftarPembelian.size()];
            Arrays.fill(expandedRows, false);

            tableModel.setData(daftarPembelian, detailPembelianMap);
            ((ExpandableTableModel) tbl_penjualan.getModel()).fireTableDataChanged();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } finally {
            try {
                if (rsPembelian != null) {
                    rsPembelian.close();
                }
                if (statPembelian != null) {
                    statPembelian.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    //isi data awal
    private void isiTabel() {
        daftarPembelian = new ArrayList<>();
        detailPembelianMap = new HashMap<>();

        try {
            this.statPembelian = k.getCon().prepareStatement("SELECT * FROM v_lap_pembelian");
            this.rsPembelian = this.statPembelian.executeQuery();
            while (rsPembelian.next()) {
                String id = rsPembelian.getString("id_beli");
                Date tanggal = rsPembelian.getDate("tanggal_beli");
                String kasir = rsPembelian.getString("nama_karyawan");
                double total = rsPembelian.getDouble("total");
                String supplier = rsPembelian.getString("nama_supplier");

                Pembelian pembelian = new Pembelian(id, kasir, tanggal, total, supplier);
                daftarPembelian.add(pembelian);

                this.statDetail = k.getCon().prepareStatement("CALL getDetailBeli(?)");
                statDetail.setString(1, id);
                this.rsDetail = this.statDetail.executeQuery();

                List<DetailPembelian> listDetail = new ArrayList<>();
                while (rsDetail.next()) {
                    String namaBarang = rsDetail.getString("nama_barang");
                    int jumlah = rsDetail.getInt("jumlah");
                    double subtotal = rsDetail.getDouble("total");

                    listDetail.add(new DetailPembelian(id, namaBarang, jumlah, subtotal));
                }
                detailPembelianMap.put(id, listDetail);

                rsDetail.close();
                statDetail.close();
            }

            // Inisialisasi status expanded
            expandedRows = new boolean[daftarPembelian.size()];
            Arrays.fill(expandedRows, false);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } finally {
            try {
                if (rsPembelian != null) {
                    rsPembelian.close();
                }
                if (statPembelian != null) {
                    statPembelian.close();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    class ExpandableTableModel extends AbstractTableModel {

        private String[] namaKolom = {"ID Penjualan", "Tanggal", "Kasir", "Supplier", "Total", "Detail"};
        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        private List<Pembelian> data = new ArrayList<>();

        @Override
        public int getRowCount() {
            int total = daftarPembelian.size();
            for (int i = 0; i < expandedRows.length; i++) {
                if (expandedRows[i]) {
                    total += detailPembelianMap.get(daftarPembelian.get(i).getId()).size();
                }
            }
            return total;
        }

        public void setData(List<Pembelian> newJual, Map<String, List<DetailPembelian>> newDetail) {
            daftarPembelian = newJual;
            detailPembelianMap = newDetail;
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return namaKolom.length;
        }

        @Override
        public String getColumnName(int column) {
            return namaKolom[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 5 ? Boolean.class : Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 5 && !isDetailRow(rowIndex);
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            // Cari data berdasarkan baris
            int currentRow = 0;
            for (int i = 0; i < daftarPembelian.size(); i++) {
                if (currentRow == rowIndex) {
                    // Ini adalah baris Penjualan
                    Pembelian p = daftarPembelian.get(i);
                    switch (columnIndex) {
                        case 0:
                            return p.getId();
                        case 1:
                            return dateFormat.format(p.getTanggal());
                        case 2:
                            return p.getNamaKasir();
                        case 3 :
                            return p.getNamaSupplier();
                        case 4:
                            return "Rp " + formatUang.formatRp(p.getTotal());
                        case 5:
                            return expandedRows[i];
                        default:
                            return null;
                    }
                }
                currentRow++;

                if (expandedRows[i]) {
                    List<DetailPembelian> details = detailPembelianMap.get(daftarPembelian.get(i).getId());
                    for (int j = 0; j < details.size(); j++) {
                        if (currentRow == rowIndex) {
                            // Ini adalah baris Detail
                            DetailPembelian d = details.get(j);
                            switch (columnIndex) {
                                case 0:
                                    return ""; // Kosong atau indentasi
                                case 1:
                                    return "";
                                case 2:
                                    return d.getNamaBarang();
                                case 3:
                                    return "Jumlah: " + d.getJumlah();
                                case 4:
                                    return "Rp " + formatUang.formatRp(d.getSubtotal());
                                case 5:
                                    return "";
                                default:
                                    return null;
                            }
                        }
                        currentRow++;
                    }
                }
            }
            return null;
        }

    }

    // Renderer untuk tombol expand/collapse
    class ExpandButtonRenderer extends JButton implements TableCellRenderer {

        public ExpandButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setContentAreaFilled(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (!isDetailRow(table.convertRowIndexToModel(row))) {
                boolean expanded = (value != null && (Boolean) value);
                //setIcon(expanded ? new ImageIcon(getClass().getResource("/icons/collapse.png"))
                //     : new ImageIcon(getClass().getResource("/icons/expand.png")));
                setText(expanded ? "[ - ]" : "[ + ]");
            } else {
                setText("");
                setIcon(null);
            }

            return this;
        }
    }

    // Editor untuk tombol expand/collapse
    class ExpandButtonEditor extends DefaultCellEditor {

        private JButton button;
        private boolean isPressed;
        private int row;

        public ExpandButtonEditor() {
            super(new JCheckBox());
            button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = table.convertRowIndexToModel(row);

            boolean expanded = (value != null && (Boolean) value);
            //button.setIcon(expanded ? new ImageIcon(getClass().getResource("/icons/collapse.png"))
            //      : new ImageIcon(getClass().getResource("/icons/expand.png")));
            button.setText(expanded ? "[ - ]" : "[ + ]");

            isPressed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPressed) {
                expandCollapseRow(row);
            }
            isPressed = false;
            return null;
        }

        @Override
        public boolean stopCellEditing() {
            isPressed = false;
            return super.stopCellEditing();
        }
    }

    private void setScrollBar() {
        // ScrollPane check (parent scroll)
        if (tbl_penjualan.getParent() instanceof JViewport) {
            Component parent = tbl_penjualan.getParent().getParent();
            if (parent instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) parent;
                // Scrollbar vertikal & horizontal
                scroll.getVerticalScrollBar().setUI(new modelTabel.ModernScrollBarUI());
                scroll.getHorizontalScrollBar().setUI(new modelTabel.ModernScrollBarUI());
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        total1 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        totalbrg = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbl_penjualan = new javax.swing.JTable();
        jLabel9 = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tgl_mulai = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        tgl_akhir = new com.toedter.calendar.JDateChooser();
        btn_cari = new javax.swing.JButton();
        txt_cari = new javax.swing.JTextField();
        btn_cari1 = new javax.swing.JButton();
        btn_cari2 = new javax.swing.JButton();
        btn_cari3 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 560));

        jButton1.setBackground(new java.awt.Color(63, 114, 175));
        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/dashboard.png"))); // NOI18N
        jButton1.setBorder(null);
        jButton1.setBorderPainted(false);
        jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton1MouseClicked(evt);
            }
        });
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(63, 114, 175));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/laporan.png"))); // NOI18N
        jButton5.setBorder(null);
        jButton5.setBorderPainted(false);

        jButton3.setBackground(new java.awt.Color(63, 114, 175));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        jButton3.setBorder(null);
        jButton3.setBorderPainted(false);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(63, 114, 175));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/karyawan.png"))); // NOI18N
        jButton4.setBorder(null);
        jButton4.setBorderPainted(false);
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });

        jButton8.setBackground(new java.awt.Color(63, 114, 175));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/supplier.png"))); // NOI18N
        jButton8.setBorder(null);
        jButton8.setBorderPainted(false);

        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/user biru1 1.png"))); // NOI18N

        txt_panggilan.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txt_panggilan.setForeground(new java.awt.Color(204, 204, 204));
        txt_panggilan.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txt_panggilan.setText("Nama");
        txt_panggilan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        txt_panggilan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_panggilanMouseClicked(evt);
            }
        });

        jLabel16.setForeground(new java.awt.Color(204, 204, 204));
        jLabel16.setText("Admin");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(48, 48, 48))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jButton8)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addComponent(jLabel16)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_panggilan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(35, 35, 35)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addContainerGap(215, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        jButton6.setBackground(new java.awt.Color(63, 114, 175));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        jButton6.setBorder(null);
        jButton6.setBorderPainted(false);
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        total1.setEditable(false);

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(51, 51, 51));
        jLabel7.setText("Total Penjualan");

        totalbrg.setEditable(false);

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(51, 51, 51));
        jLabel8.setText("Total Barang Dijual");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Laporan Pembelian");

        tbl_penjualan.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tbl_penjualan);

        jLabel9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/exporexcel.png"))); // NOI18N
        jLabel9.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel9MouseClicked(evt);
            }
        });

        filterPanel.setBackground(new java.awt.Color(218, 212, 181));

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Dari");

        tgl_mulai.setBackground(new java.awt.Color(218, 212, 181));

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Sampai");

        tgl_akhir.setBackground(new java.awt.Color(218, 212, 181));

        btn_cari.setText("cari");
        btn_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cariActionPerformed(evt);
            }
        });

        txt_cari.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_cariMouseClicked(evt);
            }
        });
        txt_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cariActionPerformed(evt);
            }
        });

        btn_cari1.setText("Perluas semua");
        btn_cari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari1ActionPerformed(evt);
            }
        });

        btn_cari2.setText("Perkecil semua");
        btn_cari2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari2ActionPerformed(evt);
            }
        });

        btn_cari3.setText("reset");
        btn_cari3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_mulai, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_akhir, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_cari)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_cari3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(btn_cari1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_cari2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_mulai, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_akhir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_cari)
                        .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cari1)
                        .addComponent(btn_cari2)
                        .addComponent(btn_cari3)))
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel6)
                .addGap(429, 429, 429))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addComponent(totalbrg, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addComponent(total1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 301, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(totalbrg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(total1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel9))
                .addContainerGap())
        );

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("TOKO BAROKAH");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(204, 204, 204));
        jLabel14.setText("Alamat toko");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(44, 44, 44)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton6)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)
                        .addGap(36, 36, 36))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton6)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)))
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 1280, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );

        setBounds(0, 0, 1296, 728);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked

    }//GEN-LAST:event_jButton6MouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked

    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked

    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked

    }//GEN-LAST:event_jButton4MouseClicked

    private void btn_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cariActionPerformed
        // TODO add your handling code here:
        Date start = tgl_mulai.getDate();
        Date end = tgl_akhir.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(null, "tanggal tidak boleh kosong");
        } else if (start.after(end)) {
            JOptionPane.showMessageDialog(null, "tanggal awal tidak boleh lebih dari tanggal akhir");
        } else {
            cariDataTanggal(getTanggalMulai(), getTanggalAkhir());
        }
    }//GEN-LAST:event_btn_cariActionPerformed

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void txt_cariMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_cariMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariMouseClicked

    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        laporan p = new laporan();
        p.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jLabel2MouseClicked

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void jLabel9MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel9MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jLabel9MouseClicked

    private void btn_cari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari1ActionPerformed
        // TODO add your handling code here:
        expandAllRows();
    }//GEN-LAST:event_btn_cari1ActionPerformed

    private void btn_cari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari2ActionPerformed
        // TODO add your handling code here:
        collapseAllRows();
    }//GEN-LAST:event_btn_cari2ActionPerformed

    private void btn_cari3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari3ActionPerformed
        // TODO add your handling code here:
        resetPencarian();
    }//GEN-LAST:event_btn_cari3ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(lap_pembelian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(lap_pembelian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(lap_pembelian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(lap_pembelian.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new lap_pembelian().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_cari;
    private javax.swing.JButton btn_cari1;
    private javax.swing.JButton btn_cari2;
    private javax.swing.JButton btn_cari3;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tbl_penjualan;
    private com.toedter.calendar.JDateChooser tgl_akhir;
    private com.toedter.calendar.JDateChooser tgl_mulai;
    private javax.swing.JTextField total1;
    private javax.swing.JTextField totalbrg;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JLabel txt_panggilan;
    // End of variables declaration//GEN-END:variables
}
