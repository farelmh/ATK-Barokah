package admin;

import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.RoundedPanel;
import fungsi_lain.formatTanggal;
import fungsi_lain.modelTabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import fungsi_lain.session;
import java.util.Date;
import java.util.Locale;

public class stokOpname extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private DefaultTableModel model2 = null;
    private final int[] index = {1, 2, 7};

    konek k = new konek();

    public stokOpname() {
        initComponents();
        k.connect();
        this.setLocationRelativeTo(null);
        setTanggal();
        setupTabbedPane();
        initTabelModel();
        tableopname();
        modelTabel.setModel(tbl_opname);
        autoFokus();
        getUser();
        tablehistori();
        CariData.TableSorter(tbl_opname, txtSearch, index, null, null);
    }

    // show tanggal otomatis
    private void setTanggal() {
        LocalDate tgl1 = LocalDate.now();
        Locale localeID = Locale.forLanguageTag("id-ID");
        Locale.setDefault(localeID);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMMM yyyy", localeID);
        String tanggal = tgl1.format(df);
        datechoose.setText(tanggal);

    }

    // show nama otomatis
    public void getUser() {
        String nama = session.getInstance().getNamaLengkap();
        txt_user.setText(nama);
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

    // tabel opname
    public void tableopname() {
        try {
            this.stat = k.getCon().prepareStatement("select id_barang, nama_barang, stok, id_barcode from barang;");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    false,
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getInt("stok"),
                    null,
                    0,
                    null,
                    rs.getString("id_barcode")
                };
                model.addRow(data);
                tbl_opname.getColumnModel().getColumn(7).setMinWidth(0);
                tbl_opname.getColumnModel().getColumn(7).setMaxWidth(0);
                tbl_opname.getColumnModel().getColumn(7).setWidth(0);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //tabel histori
    private void tablehistori() {
        model2 = new DefaultTableModel();
        model2.addColumn("ID");
        model2.addColumn("Tanggal");
        model2.addColumn("Dilakukan Oleh");
        model2.addColumn("Nama Barang");
        model2.addColumn("Stok Sistem");
        model2.addColumn("Stok Fisik");
        model2.addColumn("Selisih");
        model2.addColumn("Keterangan");

        tbl_histori.setModel(model2);
        modelTabel.setModel(tbl_histori);
        try {
            this.stat = k.getCon().prepareStatement("SELECT o.id_opname, o.tgl_opname, k.nama_karyawan, b.nama_barang, dop.stok_sistem, "
                    + "dop.stok_fisik, dop.stok_fisik - dop.stok_sistem AS selisih, dop.keterangan\n"
                    + "FROM detail_opname dop\n"
                    + "JOIN opname o ON o.id_opname = dop.id_opname\n"
                    + "JOIN karyawan k ON k.id_karyawan = o.id_karyawan\n"
                    + "JOIN barang b ON b.id_barang = dop.id_barang\n"
                    + "ORDER BY o.id_opname DESC");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_opname"),
                    rs.getString("tgl_opname"),
                    rs.getString("nama_karyawan"),
                    rs.getString("nama_barang"),
                    rs.getString("stok_sistem"),
                    rs.getString("stok_fisik"),
                    rs.getString("selisih"),
                    rs.getString("keterangan")

                };
                model2.addRow(data);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    // atur model tabbed pane
    private void setupTabbedPane() {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            tabbedPane.setTabComponentAt(i, createTab(tabbedPane.getTitleAt(i), i == 0));
        }

        tabbedPane.addChangeListener(e -> {
            for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                Component c = tabbedPane.getTabComponentAt(i);
                if (c instanceof JPanel panel) {
                    JLabel label = (JLabel) panel.getComponent(0);
                    boolean selected = (i == tabbedPane.getSelectedIndex());
                    panel.setBackground(selected ? new Color(17, 45, 78) : Color.lightGray);
                    label.setForeground(selected ? Color.WHITE : Color.BLACK);
                }
            }
        });
    }

    // atur model tabbed pane
    private JPanel createTab(String title, boolean selected) {
        JPanel panel = new RoundedPanel(45);
        panel.setOpaque(true);
        panel.setBackground(selected ? new Color(17, 45, 78) : Color.LIGHT_GRAY);
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));

        JLabel label = new JLabel(title);
        label.setForeground(selected ? Color.WHITE : Color.BLACK);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));

        panel.add(label);
        return panel;
    }

    // buat tabel opname
    private void initTabelModel() {
        model = new DefaultTableModel(new Object[]{
            "Pilih", "ID Barang", "Nama Barang", "Stok Sistem", "Stok Fisik", "Selisih", "Keterangan", "Barcode"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                if (column == 0) {
                    return true; // centang
                }
                if (column == 4) {
                    return (Boolean) getValueAt(row, 0); // stok fisik jika dicentang
                }
                if (column == 6) {
                    return (Boolean) getValueAt(row, 0); // keterangan jika dicentang
                }
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                if (columnIndex == 3 || columnIndex == 4 || columnIndex == 5) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        tbl_opname.setModel(model);
    }

    // auto fokus ke stok fisik saat klik centang
    private void autoFokus() {
        model.addTableModelListener(e -> {
            int modelRow = e.getFirstRow();
            int col = e.getColumn();

            // Jika user check
            if (col == 0) {
                Boolean checked = (Boolean) model.getValueAt(modelRow, 0);

                // Cari indeks baris di view
                int viewRow = tbl_opname.convertRowIndexToView(modelRow);
                if (viewRow == -1) {
                    return;
                }
                if (checked != null && checked) {
                    SwingUtilities.invokeLater(() -> {
                        tbl_opname.requestFocus();
                        tbl_opname.changeSelection(viewRow, 4, false, false);
                        tbl_opname.editCellAt(viewRow, 4);
                        Component editor = tbl_opname.getEditorComponent();
                        if (editor != null) {
                            editor.requestFocus();
                        }
                    });
                } else {
                    // uncheck: reset stok fisik & selisih di model
                    model.setValueAt(null, modelRow, 4);
                    model.setValueAt(0, modelRow, 5);
                    model.setValueAt(null, modelRow, 6);
                }
            }
            if (col == 4) {
                int modelRow2 = e.getFirstRow();
                Integer fisik = (Integer) model.getValueAt(modelRow2, 4);
                Integer sistem = (Integer) model.getValueAt(modelRow2, 3);
                if (fisik != null && sistem != null) {
                    model.setValueAt(fisik - sistem, modelRow2, 5);
                }
            }
        });
    }

    //cek data apa ada yang kosong
    private void prosesOpname() {
        int jumlahCentang = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Boolean isChecked = (Boolean) model.getValueAt(i, 0);
            if (isChecked != null && isChecked) {
                jumlahCentang++;

                Object stokFisik = model.getValueAt(i, 4); // kolom stok fisik
                if (stokFisik == null || !(stokFisik instanceof Integer) || (Integer) stokFisik < 0) {
                    JOptionPane.showMessageDialog(null, "Stok fisik tidak valid pada baris ke-" + (i + 1));
                    return;
                }

                Object keterangan = model.getValueAt(i, 6);
                if (keterangan == null) {
                    JOptionPane.showMessageDialog(null, "Keterangan tidak boleh kosong pada baris ke-" + (i + 1));
                    return;
                }

                String ket = keterangan.toString().trim();
                if (ket.length() < 5 || ket.length() > 30) {
                    JOptionPane.showMessageDialog(null, "Keterangan setidaknya terdiri dari 5 - 30 huruf pada baris ke-" + (i + 1));
                    return;
                }
            }
        }

        if (jumlahCentang > 0) {
            int jawab = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin memperbarui stok pada barang yang terdaftar? \n"
                    + "Perubahan tidak bisa dibatalkan",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION
            );
            if (jawab == JOptionPane.YES_OPTION) {
                tambahOpname();
            }

        } else {
            JOptionPane.showMessageDialog(null, "Tidak ada data yang dipilih!");
        }
    }

    //input ke tabel opname
    private void tambahOpname() {
        String tglOpname = formatTanggal.formatTgl(datechoose.getText());
        String idkaryawan = session.getInstance().getId();
        try {
            this.stat = k.getCon().prepareStatement("insert into opname (tgl_opname, id_karyawan) values (?, ?)");
            stat.setString(1, tglOpname);
            stat.setString(2, idkaryawan);
            int rows = stat.executeUpdate();

            if (rows > 0) {
                tambahDataDetail();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //ambil id opname terakhir dari tabel opname
    private String idAkhir() {
        try {
            this.stat = k.getCon().prepareStatement("SELECT MAX(id_opname) as id_akhir FROM opname");
            this.rs = this.stat.executeQuery();
            if (rs.next()) {
                String idakhir = rs.getString("id_akhir");
                return idakhir;
            } else {
                return null;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    // input ke tabel detail opname
    private void tambahDataDetail() {
        String id_opname = idAkhir();
        try {
            this.stat = k.getCon().prepareStatement("insert into detail_opname values (?, ?, ?, ?, ?)");

            for (int i = 0; i < model.getRowCount(); i++) {
                boolean tercentang = (boolean) model.getValueAt(i, 0);
                if (tercentang) {
                    String idBarang = model.getValueAt(i, 1).toString();
                    int stokSistem = (int) model.getValueAt(i, 3);
                    int stokFisik = (int) model.getValueAt(i, 4);
                    String keterangan = (String) model.getValueAt(i, 6);

                    stat.setString(1, id_opname);
                    stat.setString(2, idBarang);
                    stat.setInt(3, stokSistem);
                    stat.setInt(4, stokFisik);
                    stat.setString(5, keterangan);
                    stat.addBatch();
                }
            }

            stat.executeBatch();
            stat.close();

            JOptionPane.showMessageDialog(null, "Data Opname berhasil ditambahkan! \n Stok Barang berhasil diperbarui");
            model.setRowCount(0);
            tableopname();
            model2.setRowCount(0);
            tablehistori();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void cariLaporan() {
        try {
            model2.setRowCount(0);
            String tglMulai = getTanggalMulai();
            String tglAkhir = getTanggalAkhir();
            this.stat = k.getCon().prepareStatement("SELECT o.id_opname, o.tgl_opname, k.nama_karyawan, b.nama_barang, dop.stok_sistem, "
                    + "dop.stok_fisik, dop.stok_fisik - dop.stok_sistem AS selisih, dop.keterangan\n"
                    + "FROM detail_opname dop\n"
                    + "JOIN opname o ON o.id_opname = dop.id_opname\n"
                    + "JOIN karyawan k ON k.id_karyawan = o.id_karyawan\n"
                    + "JOIN barang b ON b.id_barang = dop.id_barang\n"
                    + "WHERE o.tgl_opname BETWEEN ? AND ? \n"
                    + "ORDER BY o.id_opname DESC");
            stat.setString(1, tglMulai);
            stat.setString(2, tglAkhir);
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_opname"),
                    rs.getString("tgl_opname"),
                    rs.getString("nama_karyawan"),
                    rs.getString("nama_barang"),
                    rs.getString("stok_sistem"),
                    rs.getString("stok_fisik"),
                    rs.getString("selisih"),
                    rs.getString("keterangan")
                };
                model2.addRow(data);
            }
        } catch (Exception e) {
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
        jButton5 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        txt_panggilan = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_opname = new javax.swing.JTable();
        back1 = new javax.swing.JLabel();
        btn_simpan = new javax.swing.JLabel();
        datechoose = new javax.swing.JTextField();
        txtSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txt_user = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        tgl_mulai = new com.toedter.calendar.JDateChooser();
        tgl_akhir = new com.toedter.calendar.JDateChooser();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbl_histori = new javax.swing.JTable();
        back2 = new javax.swing.JLabel();
        btn_expor = new javax.swing.JLabel();
        txtSearch1 = new javax.swing.JTextField();
        btn_cari2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 560));

        jButton5.setBackground(new java.awt.Color(63, 114, 175));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/laporan.png"))); // NOI18N
        jButton5.setBorder(null);
        jButton5.setBorderPainted(false);
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/user biru1 1.png"))); // NOI18N

        jButton3.setBackground(new java.awt.Color(63, 114, 175));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        jButton3.setBorder(null);
        jButton3.setBorderPainted(false);
        jButton3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton3MouseClicked(evt);
            }
        });
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel1))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(jButton5)
                            .addComponent(jButton8)
                            .addComponent(jButton1)
                            .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addGap(81, 81, 81))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(jLabel1)
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        jButton6.setBackground(new java.awt.Color(63, 114, 175));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/profilbiru.png"))); // NOI18N
        jButton6.setBorder(null);
        jButton6.setBorderPainted(false);
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("TOKO BAROKAH");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(204, 204, 204));
        jLabel14.setText("Alamat toko");

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Tanggal : ");

        tbl_opname.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_opname.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_opnameMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_opname);

        back1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        back1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                back1MouseClicked(evt);
            }
        });

        btn_simpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/simpan.png"))); // NOI18N
        btn_simpan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_simpanMouseClicked(evt);
            }
        });

        datechoose.setEditable(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("STOCK OPNAME BARANG");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Dilakukan Oleh : ");

        txt_user.setEditable(false);

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Cari");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(372, 372, 372)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(back1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_simpan, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(datechoose, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txt_user, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(datechoose, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel5)
                        .addComponent(jLabel6)
                        .addComponent(txt_user, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 414, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(back1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btn_simpan, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        tabbedPane.addTab("Input Opname", jPanel4);

        jPanel6.setBackground(new java.awt.Color(218, 212, 181));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("HISTORI OPNAME");

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 0));
        jLabel10.setText("Tanggal : ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Sampai dengan : ");

        tgl_mulai.setDateFormatString("dd-MM-yyyy");

        tgl_akhir.setDateFormatString("dd-MM-yyyy");

        tbl_histori.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(tbl_histori);

        back2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        back2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        back2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                back2MouseClicked(evt);
            }
        });

        btn_expor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/exporexcel.png"))); // NOI18N
        btn_expor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_exporMouseClicked(evt);
            }
        });

        btn_cari2.setText("Cari");
        btn_cari2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(back2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_expor, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(421, 421, 421)
                        .addComponent(jLabel3)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addComponent(jLabel10)
                        .addGap(14, 14, 14)
                        .addComponent(tgl_mulai, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tgl_akhir, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btn_cari2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 218, Short.MAX_VALUE)
                        .addComponent(txtSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(jLabel10)
                    .addComponent(tgl_mulai, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgl_akhir, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cari2)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 413, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_expor, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(back2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        tabbedPane.addTab("Riwayat Opname", jPanel6);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 395, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel14))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton6)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton6)
                    .addComponent(jLabel7)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
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
            .addGroup(jPanel1Layout.createSequentialGroup()
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
        adm_dashboard r = new adm_dashboard();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        dataBarang r = new dataBarang();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton3MouseClicked

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked
        dataKaryawan r = new dataKaryawan();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void btn_simpanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_simpanMouseClicked
        prosesOpname();
    }//GEN-LAST:event_btn_simpanMouseClicked

    private void back1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_back1MouseClicked
        dataBarang r = new dataBarang();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_back1MouseClicked

    private void tbl_opnameMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_opnameMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_tbl_opnameMouseClicked

    private void btn_exporMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_exporMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_exporMouseClicked

    private void back2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_back2MouseClicked
        // TODO add your handling code here:
        dataBarang a = new dataBarang();
        a.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_back2MouseClicked

    private void btn_cari2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari2ActionPerformed
        // TODO add your handling code here:
        cariLaporan();
    }//GEN-LAST:event_btn_cari2ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

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
            java.util.logging.Logger.getLogger(stokOpname.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(stokOpname.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(stokOpname.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(stokOpname.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new stokOpname().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel back1;
    private javax.swing.JLabel back2;
    private javax.swing.JButton btn_cari2;
    private javax.swing.JLabel btn_expor;
    private javax.swing.JLabel btn_simpan;
    private javax.swing.JTextField datechoose;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTable tbl_histori;
    private javax.swing.JTable tbl_opname;
    private com.toedter.calendar.JDateChooser tgl_akhir;
    private com.toedter.calendar.JDateChooser tgl_mulai;
    private javax.swing.JTextField txtSearch;
    private javax.swing.JTextField txtSearch1;
    private javax.swing.JLabel txt_panggilan;
    private javax.swing.JTextField txt_user;
    // End of variables declaration//GEN-END:variables
}
