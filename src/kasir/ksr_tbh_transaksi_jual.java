/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package kasir;

import admin.dataBarang;
import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import fungsi_tambah_data.FormTambahBarcode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.util.Vector;
import javax.swing.SwingUtilities;

public class ksr_tbh_transaksi_jual extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private DefaultTableModel modelKeranjang;
    private String id, nama, hjual, barcode;
    private boolean lacak = false;
    private final int[] index = {0, 1};
    private final int[] indexUang = {3};
    private final int[] indexAngka = {2};

    konek k = new konek();

    public ksr_tbh_transaksi_jual() {
        initComponents();
        this.setLocationRelativeTo(null);
        k.connect();
        txt_jumlah_keranjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editJumlahKeranjang(); // panggil fungsi edit jumlah
            }
        });
        
        modelKeranjang = new DefaultTableModel();
        modelKeranjang.addColumn("ID Barang");
        modelKeranjang.addColumn("Nama Barang");
        modelKeranjang.addColumn("Harga Jual");
        modelKeranjang.addColumn("Jumlah");
        modelKeranjang.addColumn("Subtotal");

        tbl_keranjang.setModel(modelKeranjang);
        tabelBarang();
        tbl_barang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {  // supaya event hanya sekali
                int selectedRow = tbl_barang.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
                    String namaBarang = model.getValueAt(modelRow, 1).toString();  // kolom 1 = Nama Barang
                    txt_nama.setText(namaBarang);
                }
            }
        });

        // SwingUtilities.invokeLater(() -> stoktipis());
        SwingUtilities.invokeLater(() -> txt_Cari.requestFocusInWindow());
        tbl_keranjang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = tbl_keranjang.getSelectedRow();
                if (row != -1) {
                    String jumlah = modelKeranjang.getValueAt(row, 3).toString(); // kolom 3 = jumlah
                    txt_jumlah_keranjang.setText(jumlah);
                }
            }
        });
    }

    // konstruktor dengan parameter
    public ksr_tbh_transaksi_jual(String id, String nama, String hjual, String barcode) {
        this.id = id;
        this.nama = nama;
        this.hjual = hjual;
        this.barcode = barcode;
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

    // tabel barang
    public void tabelBarang() {
        model = new DefaultTableModel();
        model.addColumn("ID Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Stok");
        model.addColumn("Harga Jual /Pcs");
        model.addColumn("Kode Barcode");
        tbl_barang.setModel(model);
        modelTabel.setModel(tbl_barang);

        try {
            this.stat = k.getCon().prepareStatement("SELECT b.id_barang, b.nama_barang, b.stok, b.harga_jual, b.id_barcode FROM barang b");
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("stok"),
                    "Rp " + formatUang.formatRp(rs.getDouble("harga_jual")),
                    rs.getString("id_barcode")
                };
                model.addRow(data);
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    if (tbl_barang.getColumnCount() > 4) {
                        tbl_barang.getColumnModel().getColumn(4).setMinWidth(0);
                        tbl_barang.getColumnModel().getColumn(4).setMaxWidth(0);
                        tbl_barang.getColumnModel().getColumn(4).setWidth(0);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Gagal sembunyikan kolom barcode: " + e.getMessage());
                }
            });

            //CariData.TableSorter(tbl_barang, txt_Cari, index, indexUang, indexAngka, null);
            modelTabel.setTransparan(tbl_barang, 4);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //scan barcode ( tambah data )
    private void scanBarcode(dataBarang b) {
        FormTambahBarcode form = new FormTambahBarcode(this);
        boolean hasil = form.showDialog();
        if (hasil) {
            b.setBarcode(form.getFieldValue("Kode Barcode"));
        }
    }

    //fungsi ambil data berdasarkan data yg dipilih
    private ksr_tbh_transaksi_jual pilihData() {
        ksr_tbh_transaksi_jual u = new ksr_tbh_transaksi_jual();
        int selectedRow = tbl_barang.getSelectedRow();

        if (selectedRow != -1) {
            // Konversi indeks baris tampilan ke indeks model
            int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);

            u.setId(model.getValueAt(modelRow, 0).toString());
            u.setNama(model.getValueAt(modelRow, 1).toString());
            String hb = model.getValueAt(modelRow, 4).toString();
            String hj = model.getValueAt(modelRow, 5).toString();
            double hrgjual = formatUang.setDefault(hj);

            u.setHjual(String.valueOf(hrgjual));
        }

        return u;
    }

    private void isiNamaDariTabel() {
        int selectedRow = tbl_barang.getSelectedRow();

        if (selectedRow != -1) {
            // Konversi ke indeks model jika sorting aktif
            int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
            String namaBarang = model.getValueAt(modelRow, 1).toString(); // Kolom ke-1 = Nama Barang

            txt_nama.setText(namaBarang);
        }
    }

    private void tambahKeKeranjang() {
        int selectedRow = tbl_barang.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Pilih barang terlebih dahulu.");
            return;
        }

        String jumlahStr = txt_jumlah.getText().trim();
        if (jumlahStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan jumlah terlebih dahulu.");
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
            if (jumlah <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka positif.");
            return;
        }

        int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
        String idBarang = model.getValueAt(modelRow, 0).toString();
        String namaBarang = model.getValueAt(modelRow, 1).toString();
        String hargaStr = model.getValueAt(modelRow, 3).toString().replace("Rp", "").replace(".", "").replace(",", "").trim();

        double hargaJual = Double.parseDouble(hargaStr);
        double subtotal = hargaJual * jumlah;

        Object[] data = {idBarang, namaBarang, hargaJual, jumlah, subtotal};
        modelKeranjang.addRow(data);
        modelTabel.setModel(tbl_keranjang);

        // Optional: reset input
        txt_jumlah.setText("");
        txt_nama.setText("");
        tbl_barang.clearSelection();
    }

    private void hapusDariKeranjang() {
        int selectedRow = tbl_keranjang.getSelectedRow();

        if (selectedRow != -1) {
            int confirm = JOptionPane.showConfirmDialog(this, "Hapus barang dari keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                modelKeranjang.removeRow(selectedRow);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih barang yang ingin dihapus dari keranjang.");
        }
        txt_jumlah_keranjang.setText("");
    }

    private void resetKeranjang() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin mengosongkan keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            modelKeranjang.setRowCount(0);  // Hapus semua baris
        }
        txt_jumlah_keranjang.setText("");
    }

    private void editJumlahKeranjang() {
        int row = tbl_keranjang.getSelectedRow();
        if (row != -1) {
            try {
                int jumlahBaru = Integer.parseInt(txt_jumlah_keranjang.getText());

                if (jumlahBaru <= 0) {
                    JOptionPane.showMessageDialog(this, "Jumlah harus lebih dari 0!");
                    return;
                }

                // Ambil harga dari kolom ke-2
                String hargaStr = modelKeranjang.getValueAt(row, 2).toString();
                hargaStr = hargaStr.replace("Rp", "").replace(".0", "").replace(",", "").trim();
                double hargaJual = Double.parseDouble(hargaStr);

                double subtotal = hargaJual * jumlahBaru;

                modelKeranjang.setValueAt(jumlahBaru, row, 3); // kolom jumlah
                modelKeranjang.setValueAt(" " + subtotal, row, 4); // kolom subtotal

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Masukkan angka valid!");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris di keranjang terlebih dahulu.");
        }
    }

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
                new ksr_tbh_transaksi_jual().setVisible(true);

            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        txt_jumlah_keranjang = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbl_keranjang = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        tambahKerajang2 = new javax.swing.JLabel();
        btn_cari = new javax.swing.JButton();
        btn_cari1 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbl_barang = new javax.swing.JTable();
        txt_nama = new javax.swing.JTextField();
        txt_Cari = new javax.swing.JTextField();
        txt_jumlah = new javax.swing.JTextField();
        tambahKerajang = new javax.swing.JLabel();
        tambahKerajang1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/20250517_160534 1.png"))); // NOI18N

        jPanel7.setBackground(new java.awt.Color(218, 212, 181));
        jPanel7.setPreferredSize(new java.awt.Dimension(1050, 557));

        txt_jumlah_keranjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_jumlah_keranjangActionPerformed(evt);
            }
        });

        tbl_keranjang.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane4.setViewportView(tbl_keranjang);

        jLabel5.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Jumlah :");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(0, 0, 0));
        jLabel20.setText("Keranjang");

        tambahKerajang2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/simpan.png"))); // NOI18N
        tambahKerajang2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang2MouseClicked(evt);
            }
        });

        btn_cari.setText("reset");
        btn_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cariActionPerformed(evt);
            }
        });

        btn_cari1.setText("hapus");
        btn_cari1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_cari1MouseClicked(evt);
            }
        });
        btn_cari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 610, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                        .addComponent(jLabel5)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txt_jumlah_keranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(tambahKerajang2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addGap(246, 246, 246))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(btn_cari)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(btn_cari1)
                                .addContainerGap())))))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addGap(52, 52, 52)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_cari)
                    .addComponent(btn_cari1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_jumlah_keranjang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addGap(39, 39, 39)
                .addComponent(tambahKerajang2)
                .addContainerGap())
        );

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("TOKO BAROKAH");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(204, 204, 204));
        jLabel17.setText("Alamat toko");

        jPanel8.setBackground(new java.awt.Color(218, 212, 181));

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
        jScrollPane2.setViewportView(tbl_barang);

        txt_nama.setEditable(false);
        txt_nama.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_namaActionPerformed(evt);
            }
        });

        txt_Cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_CariActionPerformed(evt);
            }
        });

        txt_jumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_jumlahActionPerformed(evt);
            }
        });

        tambahKerajang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/tambah.png"))); // NOI18N
        tambahKerajang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajangMouseClicked(evt);
            }
        });

        tambahKerajang1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        tambahKerajang1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang1MouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Nama Barang :");

        jLabel3.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Jumlah :");

        jLabel4.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Cari :");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(0, 0, 0));
        jLabel21.setText("Data Barang");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_Cari, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(tambahKerajang1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tambahKerajang))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(247, 247, 247)
                        .addComponent(jLabel21)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel21)
                .addGap(53, 53, 53)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_Cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(38, 38, 38)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tambahKerajang)
                    .addComponent(tambahKerajang1))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel17)
                            .addComponent(jLabel16))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel17)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 560, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1280, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
        );

        setBounds(0, 0, 1296, 728);
    }// </editor-fold>//GEN-END:initComponents

    private void txt_jumlah_keranjangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_jumlah_keranjangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_jumlah_keranjangActionPerformed

    private void txt_namaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_namaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_namaActionPerformed

    private void txt_CariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_CariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_CariActionPerformed

    private void txt_jumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_jumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_jumlahActionPerformed

    private void btn_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cariActionPerformed
        resetKeranjang();
    }//GEN-LAST:event_btn_cariActionPerformed

    private void btn_cari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari1ActionPerformed
        hapusDariKeranjang();
    }//GEN-LAST:event_btn_cari1ActionPerformed

    private void tambahKerajangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajangMouseClicked
        tambahKeKeranjang();
    }//GEN-LAST:event_tambahKerajangMouseClicked

    private void btn_cari1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_cari1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_cari1MouseClicked

    private void tambahKerajang1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang1MouseClicked
        ksr_transaksi_jual b = new ksr_transaksi_jual();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_tambahKerajang1MouseClicked

    private void tambahKerajang2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang2MouseClicked
        ksr_transaksi_jual transaksiJual = new ksr_transaksi_jual(); // buat instance form transaksi
        DefaultTableModel keranjangModel = (DefaultTableModel) tbl_keranjang.getModel();

        for (int i = 0; i < keranjangModel.getRowCount(); i++) {
            Vector row = new Vector();
            row.add(keranjangModel.getValueAt(i, 0)); // ID Barang
            row.add(keranjangModel.getValueAt(i, 1)); // Nama Barang
            row.add(keranjangModel.getValueAt(i, 2)); // Harga
            row.add(keranjangModel.getValueAt(i, 3)); // Jumlah
            row.add(keranjangModel.getValueAt(i, 4)); // Subtotal

            String id = keranjangModel.getValueAt(i, 0).toString(); // ID Barang
            String nama = keranjangModel.getValueAt(i, 1).toString(); // Nama Barang
            String harga = keranjangModel.getValueAt(i, 2).toString().replace("Rp", "").replace(".", "").replace(",", ".").trim(); // Harga Jual (bersihkan format)
            String jumlah = keranjangModel.getValueAt(i, 3).toString(); // Jumlah
            String subtotal = keranjangModel.getValueAt(i, 4).toString().replace("Rp", "").replace(".", "").replace(",", ".").trim(); // Subtotal

            transaksiJual.tambahDataKeTabelJual(row);
            transaksiJual.hitungTotal();

            try {
                PreparedStatement stat = k.getCon().prepareStatement(
                        "INSERT INTO keranjang (id_barang, nama_barang, harga, jumlah, total) VALUES (?, ?, ?, ?, ?)"
                );
                stat.setString(1, id);
                stat.setString(2, nama);
                stat.setDouble(3, Double.parseDouble(harga));
                stat.setInt(4, Integer.parseInt(jumlah));
                stat.setDouble(5, Double.parseDouble(subtotal));
                stat.executeUpdate();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Gagal menambahkan ke keranjang: " + e.getMessage());
            }
        }

        transaksiJual.setVisible(true);
        dispose();
    }//GEN-LAST:event_tambahKerajang2MouseClicked

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_cari;
    private javax.swing.JButton btn_cari1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel tambahKerajang;
    private javax.swing.JLabel tambahKerajang1;
    private javax.swing.JLabel tambahKerajang2;
    private javax.swing.JTable tbl_barang;
    private javax.swing.JTable tbl_keranjang;
    private javax.swing.JTextField txt_Cari;
    private javax.swing.JTextField txt_jumlah;
    private javax.swing.JTextField txt_jumlah_keranjang;
    private javax.swing.JTextField txt_nama;
    // End of variables declaration//GEN-END:variables
}
