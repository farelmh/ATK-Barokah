package admin;

import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import fungsi_tambah_data.FormTambahBarcode;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.SwingUtilities;

public class KeranjangRestock extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private DefaultTableModel modelKeranjang = null;
    private int isiSatuan = 1;
    private final int[] index = {0, 1, 2, 5, 6};
    private final int[] indexUang = {4};
    private final int[] indexAngka = {2};
    private String idBarang, namaBarang = "";
    private double harga, total;
    private JComboBox<String> comboBox;

    konek k = new konek();

    public KeranjangRestock() { //error tambah jumlah, di tabel keranjang ada 2 daftar
        initComponents();
        this.setLocationRelativeTo(null);
        k.connect();
        tabelBarang();
        tabelKeranjang();
        cb_pilih.addItem("Pcs");
        cariBarcode();

        txt_jumlah_keranjang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editJumlahKeranjang(); // panggil fungsi edit jumlah
            }
        });

        tbl_barang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {  // supaya event hanya sekali
                int selectedRow = tbl_barang.getSelectedRow();
                if (selectedRow != -1) {
                    int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
                    String namaBarang = model.getValueAt(modelRow, 1).toString();  // kolom 1 = Nama Barang
                    txt_nama.setText(namaBarang);

                    String satuan = model.getValueAt(modelRow, 5).toString();
                    txt_satuan.setText(satuan);
                }
            }
        });

        SwingUtilities.invokeLater(() -> txt_cari.requestFocusInWindow());

        tbl_keranjang.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tbl_keranjang.getSelectedRow();
                    if (selectedRow != -1) {
                        cb_pilih.setVisible(true);
                        jumlah.setVisible(true);
                        txt_jumlah_keranjang.setVisible(true);
                    } else {
                        cb_pilih.setVisible(false);
                        jumlah.setVisible(false);
                        txt_jumlah_keranjang.setVisible(false);
                    }
                }
            }
        });

        tbl_keranjang.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                isiSatuan();
            }
        });

    }
    
    private void cariBarcode() {
        txt_cari.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String uid = txt_cari.getText().trim();
                // Hanya panggil showbarang jika panjang kode 13 karakter
                if (uid.length() == 13) {
                    txt_cari.setText(""); // reset kolom input
                    showbarang(uid);
                }
            }
        });
    }
    
    private String isiSatuanBeli() {
        try {
            this.stat = k.getCon().prepareStatement(
                    "SELECT satuan_beli.nama_satuan, satuan_beli.isi_per_satuan "
                    + "FROM satuan_beli "
                    + "JOIN barang ON barang.id_satuan = satuan_beli.id_satuan "
                    + "WHERE barang.id_barang = ?"
            );
            stat.setString(1, idBarang);
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                this.isiSatuan = rs.getInt("isi_per_satuan");
                String satuan = (rs.getString("nama_satuan"));
                return satuan;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }
    
    private void showbarang(String uid) {
        try {
            this.stat = k.getCon().prepareStatement("select * from barang where id_barcode = ?");
            stat.setString(1, uid.trim());
            this.rs = this.stat.executeQuery();
            
            if (rs.next()) {
                this.idBarang = rs.getString("id_barang");
                this.namaBarang = rs.getString("nama_barang");
                this.harga = rs.getDouble("harga_beli");
                
                String satuan = isiSatuanBeli();
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                spinner.setPreferredSize(new Dimension(60, 25)); // Mengatur ukuran kotak input
                comboBox = new JComboBox<>();
                comboBox.addItem("Pcs");
                comboBox.addItem(satuan);

                panel.add(new JLabel(namaBarang + " :"));
                panel.add(spinner);
                panel.add(comboBox);

                // Menampilkan pop-up dengan JSpinner
                int option = JOptionPane.showConfirmDialog(
                        null,
                        panel,
                        "Masukkan Jumlah Barang",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (option == JOptionPane.OK_OPTION) {
                    int jumlahBarang = (int) spinner.getValue();
                    String satuanBeli = comboBox.getSelectedItem().toString();
                    tambahBarang(jumlahBarang, satuanBeli);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Tidak DItemukan");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
    
        private void tambahBarang(int jumlahBarang, String satuan) {
        try {
            int satuanBeli = 1;
            if (satuan.equalsIgnoreCase("Pcs")) {
                satuanBeli = 1;
            } else {
                satuanBeli = this.isiSatuan;
            }
            
            int jumlahFinal = jumlahBarang * satuanBeli;
            double subtotal = harga * jumlahFinal;
            
            String sqlCek = "SELECT jumlah FROM keranjang WHERE id_barang = ?";
            PreparedStatement cekStmt = k.getCon().prepareStatement(sqlCek);
            cekStmt.setString(1, idBarang);
            ResultSet rs = cekStmt.executeQuery();
            
            if (rs.next()) {
                // Barang sudah ada, update jumlah dan subtotal
                int jumlahLama = rs.getInt("jumlah");
                int jumlahBaru = jumlahLama + jumlahFinal;
                double subtotalBaru = harga * jumlahBaru;

                String sqlUpdate = "UPDATE keranjang SET jumlah = ?, total = ? WHERE id_barang = ?";
                PreparedStatement updateStmt = k.getCon().prepareStatement(sqlUpdate);
                updateStmt.setInt(1, jumlahBaru);
                updateStmt.setDouble(2, subtotalBaru);
                updateStmt.setString(3, idBarang);
                updateStmt.executeUpdate();
            } else {
                // Barang belum ada, insert baru
                this.stat = k.getCon().prepareStatement("INSERT INTO keranjang VALUES (?, ?, ?, ?, ?)");
                stat.setString(1, idBarang);
                stat.setString(2, namaBarang);
                stat.setDouble(3, harga);
                stat.setInt(4, jumlahFinal);
                stat.setDouble(5, subtotal);
                stat.executeUpdate();
            }

            tabelKeranjang();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void isiSatuan() {
        cb_pilih.removeAllItems(); // bersihkan dulu biar nggak dobel
        cb_pilih.addItem("Pcs");
        try {
            int selectedRow = tbl_keranjang.getSelectedRow();
            if (selectedRow == -1) {
                return;
            } else {
                int modelRow = tbl_keranjang.convertRowIndexToModel(selectedRow);
                String idBarang = modelKeranjang.getValueAt(modelRow, 0).toString();

                this.stat = k.getCon().prepareStatement(
                        "SELECT satuan_beli.nama_satuan, satuan_beli.isi_per_satuan "
                        + "FROM satuan_beli "
                        + "JOIN barang ON barang.id_satuan = satuan_beli.id_satuan "
                        + "WHERE barang.id_barang = ?"
                );
                stat.setString(1, idBarang);
                this.rs = this.stat.executeQuery();

                if (rs.next()) {
                    this.isiSatuan = rs.getInt("isi_per_satuan");
                    cb_pilih.addItem(rs.getString("nama_satuan"));
                }

                txt_jumlah_keranjang.setEnabled(true);
                cb_pilih.setEnabled(true);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    // tabel barang
    public void tabelBarang() {
        model = new DefaultTableModel();
        model.addColumn("ID Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Stok");
        model.addColumn("Harga Beli /Pcs");
        model.addColumn("Kode Barcode");
        model.addColumn("Kode Satuan");
        model.addColumn("Isi Satuan");

        tbl_barang.setModel(model);

        try {
            this.stat = k.getCon().prepareStatement("SELECT b.id_barang, b.nama_barang, b.stok, b.harga_beli, b.id_barcode, sb.nama_satuan, sb.isi_per_satuan "
                    + "FROM barang b JOIN satuan_beli sb ON sb.id_satuan = b.id_satuan");
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("stok"),
                    "Rp " + formatUang.formatRp(rs.getDouble("harga_beli")),
                    rs.getString("id_barcode"),
                    rs.getString("nama_satuan"),
                    rs.getString("isi_per_satuan")
                };
                model.addRow(data);
            }

            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    for (int i = 4; i <= 6; i++) {
                        tbl_barang.getColumnModel().getColumn(i).setMinWidth(0);
                        tbl_barang.getColumnModel().getColumn(i).setMaxWidth(0);
                        tbl_barang.getColumnModel().getColumn(i).setWidth(0);
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Gagal sembunyikan kolom barcode: " + e.getMessage());
                }
            });

            modelTabel.setModel(tbl_barang);
            CariData.TableSorter(tbl_barang, txt_cari, index, indexUang, indexAngka, null);
            modelTabel.setTransparan(tbl_barang, 6);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //tabel keranjang
    public void tabelKeranjang() {
        modelKeranjang = new DefaultTableModel();
        modelKeranjang.addColumn("ID Barang");
        modelKeranjang.addColumn("Nama Barang");
        modelKeranjang.addColumn("Harga");
        modelKeranjang.addColumn("Jumlah");
        modelKeranjang.addColumn("Subtotal");
        tbl_keranjang.setModel(modelKeranjang);
        modelTabel.setModel(tbl_keranjang);

        try {
            this.stat = k.getCon().prepareStatement("select id_barang, nama_barang, harga, sum(jumlah) as jumlah, sum(total) as total"
                    + " from keranjang "
                    + "group by id_barang, nama_barang;");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                double harga = rs.getDouble("harga");
                double total = rs.getDouble("total");
                String hargaFormat = formatUang.formatRp(harga);
                String totalFormat = formatUang.formatRp(total);
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    "Rp " + hargaFormat,
                    rs.getString("jumlah"),
                    "Rp " + totalFormat
                };
                modelKeranjang.addRow(data);
            }
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

    // done
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

        int jumlahInput;
        try {
            jumlahInput = Integer.parseInt(jumlahStr);
            if (jumlahInput <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Jumlah harus berupa angka positif.");
            return;
        }

        int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
        String idBarang = model.getValueAt(modelRow, 0).toString();
        String namaBarang = model.getValueAt(modelRow, 1).toString();
        String hargaStr = model.getValueAt(modelRow, 3).toString();
        double hargaFormat = formatUang.setDefault(hargaStr);
        String isiSatuanBeli = model.getValueAt(modelRow, 6).toString();

        int isisatuan;
        try {
            isisatuan = Integer.parseInt(isiSatuanBeli);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Isi satuan tidak valid. Default 1 digunakan.");
            isisatuan = 1;
        }

        int jumlahFinal = jumlahInput * isisatuan;
        double subtotal = hargaFormat * jumlahFinal;

        try {
            // Cek apakah barang sudah ada di keranjang pembelian
            String sqlCek = "SELECT jumlah FROM keranjang WHERE id_barang = ?";
            PreparedStatement cekStmt = k.getCon().prepareStatement(sqlCek);
            cekStmt.setString(1, idBarang);
            ResultSet rs = cekStmt.executeQuery();

            if (rs.next()) {
                // Barang sudah ada, update jumlah dan subtotal
                int jumlahLama = rs.getInt("jumlah");
                int jumlahBaru = jumlahLama + jumlahFinal;
                double subtotalBaru = hargaFormat * jumlahBaru;

                String sqlUpdate = "UPDATE keranjang SET jumlah = ?, total = ? WHERE id_barang = ?";
                PreparedStatement updateStmt = k.getCon().prepareStatement(sqlUpdate);
                updateStmt.setInt(1, jumlahBaru);
                updateStmt.setDouble(2, subtotalBaru);
                updateStmt.setString(3, idBarang);
                updateStmt.executeUpdate();
            } else {
                // Barang belum ada, insert baru
                this.stat = k.getCon().prepareStatement("INSERT INTO keranjang VALUES (?, ?, ?, ?, ?)");
                stat.setString(1, idBarang);
                stat.setString(2, namaBarang);
                stat.setDouble(3, hargaFormat);
                stat.setInt(4, jumlahFinal);
                stat.setDouble(5, subtotal);
                stat.executeUpdate();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        tabelKeranjang();

// Reset input
        txt_jumlah.setText("");
        txt_nama.setText("");
        txt_satuan.setText("");
        tbl_barang.clearSelection();

    }

    // tombol hapus (done)
    private void hapusDariKeranjang() {
        int selectedRow = tbl_keranjang.getSelectedRow();
        int modelRow = tbl_barang.convertRowIndexToModel(selectedRow);
        String idBarang = modelKeranjang.getValueAt(modelRow, 0).toString();
        try {
            this.stat = k.getCon().prepareStatement("delete from keranjang where id_barang = ?");
            stat.setString(1, idBarang);

            if (selectedRow != -1) {

                int confirm = JOptionPane.showConfirmDialog(this, "Hapus barang dari keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    stat.executeUpdate();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Pilih barang yang ingin dihapus dari keranjang.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        tabelKeranjang();
        txt_jumlah_keranjang.setText("");
    }

    //kosongkan keranjang (done)
    private void resetKeranjang() {
        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin mengosongkan keranjang?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                this.stat = k.getCon().prepareStatement("delete from keranjang");
                stat.executeUpdate();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
        }
        tabelKeranjang();
        txt_jumlah_keranjang.setText("");
    }

    // edit jumlah barang (done)
    private void editJumlahKeranjang() {
        int selectedRow = tbl_keranjang.getSelectedRow();
        int modelRow = tbl_keranjang.convertRowIndexToModel(selectedRow);
        String idBarang = modelKeranjang.getValueAt(modelRow, 0).toString();
        String satuan = cb_pilih.getSelectedItem().toString();

        String jumlahAwalStr = txt_jumlah_keranjang.getText();
        int jumlahAwal = Integer.parseInt(jumlahAwalStr);

        int jumlahFinal = 0;
        if (satuan.equalsIgnoreCase("Pcs")) {
            jumlahFinal = jumlahAwal;
        } else {
            jumlahFinal = jumlahAwal * isiSatuan;
        }
        double subtotal = getSubTotal(idBarang, jumlahFinal);
        try {
            if (jumlahFinal == 0) {
                this.stat = k.getCon().prepareStatement("delete from keranjang where id_barang = ? ");
                stat.setString(1, idBarang);
                stat.executeUpdate();
            } else {
                this.stat = k.getCon().prepareStatement("update keranjang set jumlah = ?, total = ? where id_barang = ?");
                stat.setInt(1, jumlahFinal);
                stat.setDouble(2, subtotal);
                stat.setString(3, idBarang);
                stat.executeUpdate();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        isiSatuan();
        tabelKeranjang();
        txt_jumlah_keranjang.setText("");
    }

    // hitung subtotal untuk update keranjang (done)
    private double getSubTotal(String id, int jumlah) {
        try {
            double hbeli = 0;

            this.stat = k.getCon().prepareStatement("select harga_beli from barang where id_barang = ?");
            stat.setString(1, id);
            this.rs = this.stat.executeQuery();
            if (rs.next()) {
                hbeli = rs.getDouble("harga_beli");
            }
            double hargaFinal = hbeli * jumlah;
            //hargaFinal = Math.round(hargaFinal / 50.0) * 50;
            return hargaFinal;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return 0;
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
                new KeranjangRestock().setVisible(true);

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
        jumlah = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        tambahKerajang2 = new javax.swing.JLabel();
        cb_pilih = new javax.swing.JComboBox<>();
        tambahKerajang3 = new javax.swing.JLabel();
        tambahKerajang4 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tbl_barang = new javax.swing.JTable();
        txt_nama = new javax.swing.JTextField();
        txt_cari = new javax.swing.JTextField();
        txt_jumlah = new javax.swing.JTextField();
        tambahKerajang = new javax.swing.JLabel();
        tambahKerajang1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txt_satuan = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jPanel7.setBackground(new java.awt.Color(218, 212, 181));
        jPanel7.setPreferredSize(new java.awt.Dimension(1050, 557));

        txt_jumlah_keranjang.setEnabled(false);
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

        jumlah.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jumlah.setForeground(new java.awt.Color(0, 0, 0));
        jumlah.setText("Jumlah :");

        jLabel20.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel20.setForeground(new java.awt.Color(0, 0, 0));
        jLabel20.setText("Keranjang");

        tambahKerajang2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/simpanNew.png"))); // NOI18N
        tambahKerajang2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tambahKerajang2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang2MouseClicked(evt);
            }
        });

        cb_pilih.setEnabled(false);
        cb_pilih.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cb_pilihActionPerformed(evt);
            }
        });

        tambahKerajang3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/hapusNew.png"))); // NOI18N
        tambahKerajang3.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tambahKerajang3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang3MouseClicked(evt);
            }
        });

        tambahKerajang4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kosongkan.png"))); // NOI18N
        tambahKerajang4.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tambahKerajang4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang4MouseClicked(evt);
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
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addComponent(jLabel20)
                                .addGap(194, 194, 194))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(jumlah)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(txt_jumlah_keranjang, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(cb_pilih, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel7Layout.createSequentialGroup()
                                        .addComponent(tambahKerajang3, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(5, 5, 5)
                                        .addComponent(tambahKerajang2, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(tambahKerajang4)
                .addGap(14, 14, 14))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel20)
                .addGap(44, 44, 44)
                .addComponent(tambahKerajang4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cb_pilih, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_jumlah_keranjang, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jumlah))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tambahKerajang2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tambahKerajang3, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );

        jLabel16.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("TOKO BAROKAH ATK");

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(204, 204, 204));
        jLabel17.setText("Jl. Raya Besuk, Desa Alaskandang, Kec. Besuk, Kab. Probolinggo");

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

        txt_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cariActionPerformed(evt);
            }
        });

        txt_jumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_jumlahActionPerformed(evt);
            }
        });

        tambahKerajang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/tambah.png"))); // NOI18N
        tambahKerajang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tambahKerajang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajangMouseClicked(evt);
            }
        });

        tambahKerajang1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembaliNew.png"))); // NOI18N
        tambahKerajang1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        tambahKerajang1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tambahKerajang1MouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 0, 0));
        jLabel1.setText("Nama Barang :");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Jumlah :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Cari :");

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel21.setForeground(new java.awt.Color(0, 0, 0));
        jLabel21.setText("Data Barang");

        txt_satuan.setEditable(false);
        txt_satuan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_satuanActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane2)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(tambahKerajang1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tambahKerajang))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel8Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 120, Short.MAX_VALUE)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_satuan, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                    .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(txt_nama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(txt_satuan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(42, 42, 42)
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
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel16)
                            .addComponent(jLabel17))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel16)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel17)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
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

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void txt_jumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_jumlahActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_jumlahActionPerformed

    private void tambahKerajangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajangMouseClicked
        tambahKeKeranjang();
    }//GEN-LAST:event_tambahKerajangMouseClicked

    private void tambahKerajang1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang1MouseClicked
        Restock b = new Restock();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_tambahKerajang1MouseClicked

    private void tambahKerajang2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang2MouseClicked
        if (txt_jumlah_keranjang.getText().isEmpty() || !txt_jumlah_keranjang.getText().matches("\\d+")) {
            JOptionPane.showMessageDialog(null, "harap masukkan jumlah barang dengan benar!");
        } else {
            editJumlahKeranjang();
        }
    }//GEN-LAST:event_tambahKerajang2MouseClicked

    private void txt_satuanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_satuanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_satuanActionPerformed

    private void cb_pilihActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cb_pilihActionPerformed

    }//GEN-LAST:event_cb_pilihActionPerformed

    private void tambahKerajang3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang3MouseClicked
        // TODO add your handling code here:
        hapusDariKeranjang();
    }//GEN-LAST:event_tambahKerajang3MouseClicked

    private void tambahKerajang4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tambahKerajang4MouseClicked
        // TODO add your handling code here:
        resetKeranjang();
    }//GEN-LAST:event_tambahKerajang4MouseClicked

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cb_pilih;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JLabel jumlah;
    private javax.swing.JLabel tambahKerajang;
    private javax.swing.JLabel tambahKerajang1;
    private javax.swing.JLabel tambahKerajang2;
    private javax.swing.JLabel tambahKerajang3;
    private javax.swing.JLabel tambahKerajang4;
    private javax.swing.JTable tbl_barang;
    private javax.swing.JTable tbl_keranjang;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JTextField txt_jumlah;
    private javax.swing.JTextField txt_jumlah_keranjang;
    private javax.swing.JTextField txt_nama;
    private javax.swing.JTextField txt_satuan;
    // End of variables declaration//GEN-END:variables
}
