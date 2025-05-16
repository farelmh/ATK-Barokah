package admin;

import barokah_atk.Login;
import barokah_atk.konek;
import fungsi_tambah_data.FormTambahBarang;
import fungsi_tambah_data.FormTambahBarcode;
import fungsi_ubah_data.FormUbahBarang;
import fungsi_lain.modelTabel;
import fungsi_lain.CariData;
import fungsi_lain.formatUang;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class dataBarang extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private String id, nama, hbeli, hjual, barcode, idKategori, idSatuan = "";
    private boolean lacak = false;
    private final int[] index = {0, 1, 2, 6, 7};
    private final int[] indexUang = {4, 5};
    private final int[] indexAngka = {3};

    konek k = new konek();

    public dataBarang() {
        initComponents();
        this.setLocationRelativeTo(null);
        k.connect();
        tabelBarang();
        // SwingUtilities.invokeLater(() -> stoktipis());
        SwingUtilities.invokeLater(() -> txt_cari.requestFocusInWindow());
        aktifkanTombolUbah();
        onHover();
    }

    // konstruktor dengan parameter
    public dataBarang(String id, String nama, String hbeli, String hjual, String barcode, String idKategori, String idSatuan) {
        this.id = id;
        this.nama = nama;
        this.hbeli = hbeli;
        this.hjual = hjual;
        this.barcode = barcode;
        this.idKategori = idKategori;
        this.idSatuan = idSatuan;
    }

    // Getter dan Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getHbeli() {
        return hbeli;
    }

    public void setHbeli(String hbeli) {
        this.hbeli = hbeli;
    }

    public String getHjual() {
        return hjual;
    }

    public void setHjual(String hjual) {
        this.hjual = hjual;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getIdKategori() {
        return idKategori;
    }

    public void setIdKategori(String idKategori) {
        this.idKategori = idKategori;
    }

    public String getIdSatuan() {
        return idSatuan;
    }

    public void setIdSatuan(String idSatuan) {
        this.idSatuan = idSatuan;
    }

    //fungsi lacak stok tipis
    public void stoktipis() {
        if (lacak == false) {
            try {
                this.stat = k.getCon().prepareStatement("CALL tampilkan_stok_tipis()");
                this.rs = this.stat.executeQuery();
                while (rs.next()) {
                    String p = rs.getString("nama_barang");
                    String u = rs.getString("stok");
                    JOptionPane.showMessageDialog(null, "Stok barang menipis! \n"
                            + p + ", sisa " + u);
                    lacak = true;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
    }

    // tabel barang
    public void tabelBarang() {
        model = new DefaultTableModel();
        model.addColumn("ID Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Kategori");
        model.addColumn("Stok");
        model.addColumn("Harga Beli /Pcs");
        model.addColumn("Harga Jual /Pcs");
        model.addColumn("Satuan Kulakan");
        model.addColumn("Kode Barcode");
        tbl_barang.setModel(model);
        modelTabel.setModel(tbl_barang);
        CariData.TableSorter(tbl_barang, txt_cari, index, indexUang, indexAngka);

        try {
            this.stat = k.getCon().prepareStatement("SELECT b.id_barang, b.nama_barang, k.nama_kategori, b.stok, b.harga_beli, b.harga_jual, sb.nama_satuan, b.id_barcode\n"
                    + "FROM barang b\n"
                    + "JOIN satuan_beli sb ON sb.id_satuan = b.id_satuan\n"
                    + "JOIN kategori k ON k.id_kategori = b.id_kategori");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("nama_kategori"),
                    rs.getString("stok"),
                    "Rp " + formatUang.formatRp(rs.getDouble("harga_beli")),
                    "Rp " + formatUang.formatRp(rs.getDouble("harga_jual")),
                    rs.getString("nama_satuan"),
                    rs.getString("id_barcode")
                };
                model.addRow(data);
                modelTabel.setTransparan(tbl_barang, 7);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //aktifkan tombol ubah setelah pilih data dari tabel
    private void aktifkanTombolUbah() {
        tbl_barang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tbl_barang.getSelectedRow();
                btn_ubah.setEnabled(row != -1); // aktifin tombol kalau baris kepilih
            }
        });
    }

    private void onHover() {
        if (!btn_ubah.isEnabled()) {
            btn_ubah.setToolTipText("Pilih Data Barang untuk mengakses menu");
        }
    }

    //panggil fungsi pop up form tambah
    private void tambahData() {
        FormTambahBarang form = new FormTambahBarang(this);
        dataBarang b = new dataBarang();

        if (form.showDialog()) {
            b.setId(form.getFieldValue("ID Barang").toUpperCase());
            b.setNama(form.getFieldValue("Nama Barang"));
            b.setHbeli(form.getHargaBeliPcs());
            b.setHjual(form.getHargaJual());
            b.setIdKategori(form.getIdKategori());
            b.setIdSatuan(form.getIdSatuan());
            scanBarcode(b);
        }
    }

    //scan barcode ( tambah data )
    private void scanBarcode(dataBarang b) {
        FormTambahBarcode form = new FormTambahBarcode(this);
        boolean hasil = form.showDialog();
        if (hasil) {
            b.setBarcode(form.getFieldValue("Kode Barcode"));
            tambahKeDB(b);
        }
    }

    //insert database
    private void tambahKeDB(dataBarang b) {
        try {
            this.stat = k.getCon().prepareStatement("insert into barang (id_barang, nama_barang, stok, "
                    + "harga_beli, harga_jual, id_barcode, id_kategori, id_satuan) values(?, ?, ?, ?, ?, ?, ?, ?)");
            stat.setString(1, b.getId().toUpperCase());
            stat.setString(2, b.getNama());
            stat.setInt(3, 0);
            stat.setString(4, b.getHbeli());
            stat.setString(5, b.getHjual());
            stat.setString(6, b.getBarcode().trim());
            stat.setString(7, b.getIdKategori());
            stat.setString(8, b.getIdSatuan());
            stat.executeUpdate();

            JOptionPane.showMessageDialog(null, "Data Barang Berhasil Ditambahkan");
            tabelBarang();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    //fungsi ambil data berdasarkan data yg dipilih
    private dataBarang pilihData() {
        dataBarang u = new dataBarang();
        int selectedRow = tbl_barang.getSelectedRow();

        if (selectedRow != -1) {
            // Konversi indeks baris tampilan ke indeks model
            int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);

            u.setId(model.getValueAt(modelRow, 0).toString());
            u.setNama(model.getValueAt(modelRow, 1).toString());
            String hb = model.getValueAt(modelRow, 4).toString();
            String hj = model.getValueAt(modelRow, 5).toString();
            double hrgbeli = formatUang.setDefault(hb);
            double hrgjual = formatUang.setDefault(hj);

            u.setHbeli(String.valueOf(hrgbeli));
            u.setHjual(String.valueOf(hrgjual));
        }

        return u;
    }

    // panggil pop up form ubah data
    private void ubahData() {
        dataBarang u = pilihData();
        FormUbahBarang form = new FormUbahBarang(this, u);

        boolean simpan = form.showDialog();
        boolean hapus = form.hapusData(false);

        if (hapus) {
            int jawab = JOptionPane.showConfirmDialog(
                    this,
                    "Apakah Anda yakin ingin menghapus data ini?",
                    "Konfirmasi",
                    JOptionPane.YES_NO_OPTION
            );

            if (jawab == JOptionPane.YES_OPTION) {
                hapusData(u);
            }

        } else if (simpan) {
            // hanya set field dan update kalau user klik Simpan
            u.setId(form.getFieldValue("ID Barang"));
            u.setNama(form.getFieldValue("Nama Barang"));
            u.setHbeli(form.getFieldValue("Harga Beli /Pcs"));
            u.setHjual(form.getFieldValue("Harga Jual /Pcs"));
            u.setBarcode(form.getFieldValue("Kode Barcode"));
            u.setIdSatuan(form.getIdSatuan());
            
            updateData(u);
        }
    }

    //update database
    private void updateData(dataBarang u) {
        try {
            this.stat = k.getCon().prepareStatement("update barang set nama_barang = ?, "
                    + "harga_beli = ?, harga_jual = ?, id_barcode = ?, id_satuan = ? "
                    + "where id_barang = ?");
            stat.setString(1, u.getNama());
            stat.setString(2, u.getHbeli());
            stat.setString(3, u.getHjual());
            stat.setString(4, u.getBarcode());
            stat.setString(5, u.getIdSatuan());
            stat.setString(6, u.getId().toUpperCase());
            stat.executeUpdate();

            JOptionPane.showMessageDialog(null, "Data Barang Berhasil Diperbarui");
            tabelBarang();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    //hapus data di database
    private void hapusData(dataBarang u) {
        try {
            this.stat = k.getCon().prepareStatement("delete from barang where id_barang = ?");
            stat.setString(1, u.getId());
            stat.executeUpdate();

            JOptionPane.showMessageDialog(null, "Data Barang Berhasil Dihapus");
            tabelBarang();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        dashboard = new javax.swing.JButton();
        laporan = new javax.swing.JButton();
        barang = new javax.swing.JButton();
        karyawan = new javax.swing.JButton();
        supplier = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        logout = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btn_tambah = new javax.swing.JLabel();
        btn_ubah = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_barang = new javax.swing.JTable();
        txt_cari = new javax.swing.JTextField();
        btn_cari = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 560));

        dashboard.setBackground(new java.awt.Color(63, 114, 175));
        dashboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/dashboard.png"))); // NOI18N
        dashboard.setBorder(null);
        dashboard.setBorderPainted(false);
        dashboard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        dashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dashboardMouseClicked(evt);
            }
        });
        dashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dashboardActionPerformed(evt);
            }
        });

        laporan.setBackground(new java.awt.Color(63, 114, 175));
        laporan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/laporan.png"))); // NOI18N
        laporan.setBorder(null);
        laporan.setBorderPainted(false);
        laporan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        laporan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                laporanMouseClicked(evt);
            }
        });
        laporan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                laporanActionPerformed(evt);
            }
        });

        barang.setBackground(new java.awt.Color(63, 114, 175));
        barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        barang.setBorder(null);
        barang.setBorderPainted(false);
        barang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        karyawan.setBackground(new java.awt.Color(63, 114, 175));
        karyawan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/karyawan.png"))); // NOI18N
        karyawan.setBorder(null);
        karyawan.setBorderPainted(false);
        karyawan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        karyawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                karyawanMouseClicked(evt);
            }
        });

        supplier.setBackground(new java.awt.Color(63, 114, 175));
        supplier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/supplier.png"))); // NOI18N
        supplier.setBorder(null);
        supplier.setBorderPainted(false);
        supplier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        supplier.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                supplierMouseClicked(evt);
            }
        });

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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(barang)
                            .addComponent(karyawan)
                            .addComponent(laporan)
                            .addComponent(supplier)
                            .addComponent(dashboard)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel13)))
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
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_panggilan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addGap(35, 35, 35)
                .addComponent(dashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(barang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(karyawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supplier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(laporan)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        logout.setBackground(new java.awt.Color(63, 114, 175));
        logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        logout.setBorder(null);
        logout.setBorderPainted(false);
        logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                logoutMouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        btn_tambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/tambahdataterbaru.png"))); // NOI18N
        btn_tambah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_tambah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_tambahMouseClicked(evt);
            }
        });

        btn_ubah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/ubaheditbiru.png"))); // NOI18N
        btn_ubah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_ubah.setEnabled(false);
        btn_ubah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_ubahMouseClicked(evt);
            }
        });

        tbl_barang.setModel(new javax.swing.table.DefaultTableModel(
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
        tbl_barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbl_barangMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tbl_barang);

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

        btn_cari.setText("reset");
        btn_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cariActionPerformed(evt);
            }
        });

        jLabel5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stokopname.png"))); // NOI18N
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel5MouseClicked(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("DATA BARANG");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(btn_tambah, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_ubah, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 319, Short.MAX_VALUE)
                        .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_cari)))
                .addContainerGap())
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(427, 427, 427)
                .addComponent(jLabel2)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_tambah, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btn_ubah, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_cari)
                        .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 444, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("TOKO BAROKAH");

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
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(logout))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel14)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel14))
                    .addComponent(jLabel6)
                    .addComponent(logout))
                .addGap(16, 16, 16)
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE))
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

    private void dashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_dashboardActionPerformed

    private void btn_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cariActionPerformed
        // TODO add your handling code here:
        //  prosescari(null);
        tabelBarang();
        txt_cari.setText("");
        CariData.resetTableSorting(tbl_barang);
    }//GEN-LAST:event_btn_cariActionPerformed

    private void txt_cariMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_cariMouseClicked
        // TODO add your handling code here:
        //txt_cari.setText("");
    }//GEN-LAST:event_txt_cariMouseClicked

    private void btn_tambahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_tambahMouseClicked
        tambahData();
    }//GEN-LAST:event_btn_tambahMouseClicked

    private void btn_ubahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_ubahMouseClicked
        // TODO add your handling code here:
        if (btn_ubah.isEnabled()) {
            ubahData();
        }
    }//GEN-LAST:event_btn_ubahMouseClicked

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void logoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoutMouseClicked
        int jawab = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin Log Out?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );
        if (jawab == JOptionPane.YES_OPTION) {
            Login b = new Login();
            b.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_logoutMouseClicked

    private void dashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardMouseClicked
        adm_dashboard r = new adm_dashboard();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_dashboardMouseClicked

    private void karyawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_karyawanMouseClicked
        dataKaryawan r = new dataKaryawan();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_karyawanMouseClicked

    private void laporanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_laporanMouseClicked
        laporan r = new laporan();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_laporanMouseClicked

    private void jLabel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel5MouseClicked
        stokOpname r = new stokOpname();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jLabel5MouseClicked

    private void laporanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_laporanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_laporanActionPerformed

    private void supplierMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_supplierMouseClicked
        dataSupplier r = new dataSupplier();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_supplierMouseClicked

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void tbl_barangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbl_barangMouseClicked
        // TODO add your handling code here:
        pilihData();
    }//GEN-LAST:event_tbl_barangMouseClicked
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
            java.util.logging.Logger.getLogger(dataBarang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(dataBarang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(dataBarang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(dataBarang.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new dataBarang().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton barang;
    private javax.swing.JButton btn_cari;
    private javax.swing.JLabel btn_tambah;
    private javax.swing.JLabel btn_ubah;
    private javax.swing.JButton dashboard;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton karyawan;
    private javax.swing.JButton laporan;
    private javax.swing.JButton logout;
    private javax.swing.JButton supplier;
    private javax.swing.JTable tbl_barang;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JLabel txt_panggilan;
    // End of variables declaration//GEN-END:variables
}
