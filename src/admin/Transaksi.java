package admin;

import barokah_atk.konek;
import barokah_atk.Login;
import fungsi_lain.formatTanggal;
import fungsi_lain.session;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import struk.CetakStrukJual;
import struk.DataStrukPenjualan;
import struk.ItemPenjualan;

/**
 *
 * @author x260
 */
public class Transaksi extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    konek k = new konek();
    private DataStrukPenjualan data;
    CetakStrukJual struk = new CetakStrukJual();
    String namaPanggilan = session.getInstance().getNama();

    public Transaksi() {
        initComponents();
        k.connect();
        setTanggal();
        this.setLocationRelativeTo(null);
        generateIdTRJ();
        SwingUtilities.invokeLater(() -> txt_cari.requestFocusInWindow());
        tabelKeranjang();
        cariBarcode();
        txt_panggilan1.setText(namaPanggilan);
        txt_bayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                try {
                    String totalStr = txt_Total.getText();
                    double total = formatUang.setDefault(totalStr); // Pastikan formatUang.setDefault() mengubah string ke double

                    String bayarStr = txt_bayar.getText();
                    if (!bayarStr.isEmpty()) {
                        double bayar = Double.parseDouble(bayarStr);
                        double kembalian = bayar - total;
                        String kembalianFormat = formatUang.formatRp(kembalian);

                        if (bayar >= total) {
                            txt_kembalian.setForeground(Color.BLACK);
                            txt_kembalian.setText("Rp " + kembalianFormat);
                        } else {
                            txt_kembalian.setForeground(Color.red);
                            txt_kembalian.setText("Rp " + kembalianFormat);
                        }
                    } else {
                        txt_kembalian.setForeground(Color.BLACK);
                        txt_kembalian.setText("Rp 0.00");
                    }
                } catch (NumberFormatException e) {
                    txt_kembalian.setForeground(Color.red);
                    txt_kembalian.setText("error");
                }
            }
        });

    }
    
    private static String formatTglBulanTahun(String tanggalInput) {
        try {
            // Formatter input dengan bulan dalam teks bahasa Indonesia
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            LocalDate localDate = LocalDate.parse(tanggalInput, inputFormatter);
            return localDate.format(outputFormatter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setDataStruk() {
        data = new DataStrukPenjualan();
        String idPenjualan = id_jual.getText();
        String tanggal = formatTglBulanTahun(tgljual.getText());
        String kasir = session.getInstance().getNama();
        double total = formatUang.setDefault(txt_Total.getText());
        double bayar = formatUang.setDefault(txt_bayar.getText());
        double kembalian = formatUang.setDefault(txt_kembalian.getText());

        data.setId(idPenjualan);
        data.setTanggal(tanggal);
        data.setKasir(kasir);
        data.setTotal(total);
        data.setBayar(bayar);
        data.setKembalian(kembalian);
    }

    // kirim data penjualan ke struk
    public static List<ItemPenjualan> getListBarang() {
        konek k = new konek();
        k.connect();
        List<ItemPenjualan> list = new ArrayList<>();
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT nama_barang, harga, jumlah FROM KERANJANG");
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                String namaBarang = rs.getString("nama_barang");
                int harga = rs.getInt("harga");
                int jumlah = rs.getInt("jumlah");
                list.add(new ItemPenjualan(namaBarang, jumlah, harga));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return list;
    }

    private void generateIdTRJ() {
        try {
            // Query untuk mendapatkan ID TRJ terakhir
            String sql = "SELECT id_jual FROM penjualan ORDER BY id_jual DESC LIMIT 1";
            this.stat = k.getCon().prepareStatement(sql);
            this.rs = stat.executeQuery();

            String newId;
            if (rs.next()) {
                String lastId = rs.getString("id_jual");
                // Ekstrak angka dari ID terakhir
                int idNumber = Integer.parseInt(lastId.substring(2));
                // Tambahkan 1 ke ID terakhir dan format menjadi 3 digit
                newId = "TJ" + String.format("%03d", idNumber + 1);
            } else {
                // Jika tidak ada data, mulai dengan TJ001
                newId = "TJ001";
            }
            id_jual.setText(newId);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error generating ID: " + e.getMessage());
        }
    }

    public void tabelKeranjang() {
        model = new DefaultTableModel();
        model.addColumn("ID Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Harga");
        model.addColumn("Jumlah");
        model.addColumn("Subtotal");
        tbl_jual.setModel(model);
        modelTabel.setModel(tbl_jual);

        try {
            this.stat = k.getCon().prepareStatement("select id_barang, nama_barang, harga, sum(jumlah) as jumlah, sum(total) as total"
                    + " from keranjang "
                    + "group by id_barang, nama_barang;");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    "Rp " + formatUang.formatRp(Double.parseDouble(rs.getString("harga"))),
                    rs.getString("jumlah"),
                    "Rp " + formatUang.formatRp(Double.parseDouble(rs.getString("total")))
                };
                model.addRow(data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        try {
            this.stat = k.getCon().prepareStatement("SELECT IFNULL(sum(total), 0) AS total FROM keranjang");
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                total = Math.round(total / 100) * 100;
                txt_Total.setText("Rp " + formatUang.formatRp(total));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }

    // show tanggal otomatis
    private void setTanggal() {
        LocalDate tgl1 = LocalDate.now();
        Locale localeID = Locale.forLanguageTag("id-ID");
        Locale.setDefault(localeID);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd MMMM yyyy", localeID);
        String tanggal = tgl1.format(df);
        tgljual.setText(tanggal);

    }

    //pop up jumlah
    private void cariBarcode() {
        txt_cari.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String uid = txt_cari.getText().trim();
                    txt_cari.setText("");
                    if (!uid.isEmpty()) {
                        showbarang(uid);
                    }
                }
            }
        });
    }

    //show barang ke pop up
    private void showbarang(String uid) {
        try {
            this.stat = k.getCon().prepareStatement("select nama_barang from barang where id_barcode = ?");
            stat.setString(1, uid.trim());
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                String nama = rs.getString("nama_barang");

                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
                spinner.setPreferredSize(new Dimension(60, 25)); // Mengatur ukuran kotak input
                panel.add(new JLabel(nama + " :"));
                panel.add(spinner);

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
                    tambahBarang(uid, jumlahBarang);
                }
            } else {
                JOptionPane.showMessageDialog(null, "Tidak DItemukan");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void tambahBarang(String uid, int jumlahBarang) {
        try {
            String id = "";
            String nama = "";
            int harga = 0;
            this.stat = k.getCon().prepareStatement("select * from barang where id_barcode = ?");
            stat.setString(1, uid);
            this.rs = this.stat.executeQuery();
            if (rs.next()) {
                id = rs.getString("id_barang");
                nama = rs.getString("nama_barang");
                harga = rs.getInt("harga_jual");
            }

            double subtotal = harga * jumlahBarang;
            this.stat = k.getCon().prepareStatement("insert into keranjang values(?, ?, ?, ?, ?)");
            stat.setString(1, id);
            stat.setString(2, nama);
            stat.setInt(3, harga);
            stat.setInt(4, jumlahBarang);
            stat.setDouble(5, subtotal);
            stat.executeUpdate();

            tabelKeranjang();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void hapusKeranjang() {
        try {
            this.stat = k.getCon().prepareStatement("delete from keranjang");
            stat.executeUpdate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }

    private boolean keluar() {
        if (model.getRowCount()== 0) {
            return true;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Keluar dari halaman transaksi?\nDaftar barang yang sudah ditambahkan akan dihapus!", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            hapusKeranjang();
            return true;
        }
        return false;
    }

    private void trjual() {
        try {
            String total = txt_Total.getText();
            double Total = formatUang.setDefault(total);
            String idkaryawan = session.getInstance().getId();
            this.stat = k.getCon().prepareStatement("insert into penjualan values (?, ?, ?, ?)");
            stat.setString(1, id_jual.getText());
            stat.setString(2, formatTanggal.formatTgl(tgljual.getText()));
            stat.setString(3, idkaryawan);
            stat.setString(4, String.valueOf(Total));
            stat.executeUpdate();

            // insert detail pembelian
            try {
                String idbarang = "";
                String jumlah = "";
                this.stat = k.getCon().prepareStatement("select id_barang, sum(jumlah) as jumlah, sum(total) as total "
                        + "from keranjang "
                        + "group by id_barang");
                this.rs = this.stat.executeQuery();

                while (rs.next()) {
                    idbarang = rs.getString("id_barang");
                    jumlah = rs.getString("jumlah");
                    total = rs.getString("total");

                    this.stat = k.getCon().prepareStatement("insert into detail_jual values (?, ?, ?, ?)");
                    stat.setString(1, id_jual.getText());
                    stat.setString(2, idbarang);
                    stat.setString(3, jumlah);
                    stat.setString(4, total);
                    stat.executeUpdate();
                }

                try {
                    this.stat = k.getCon().prepareStatement("delete from keranjang");
                    stat.executeUpdate();
                    tabelKeranjang();

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, e.getMessage());
            }
            JOptionPane.showMessageDialog(null, "pembayaran berhasil!");
            struk.cetak();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        txt_bayar.setText("");
        txt_kembalian.setText("");
        generateIdTRJ();
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
        jPanel3 = new javax.swing.JPanel();
        kasir_profil = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_jual = new javax.swing.JTable();
        btn_tambah = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txt_Total = new javax.swing.JTextField();
        txt_kembalian = new javax.swing.JTextField();
        txt_bayar = new javax.swing.JTextField();
        btn_bayar = new javax.swing.JLabel();
        id_jual = new javax.swing.JTextField();
        tgljual = new javax.swing.JTextField();
        txt_cari = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        btn_dashboard1 = new javax.swing.JButton();
        btn_laporan1 = new javax.swing.JButton();
        btn_dataBarang1 = new javax.swing.JButton();
        btn_dataKaryawan1 = new javax.swing.JButton();
        btn_dataSupplier1 = new javax.swing.JButton();
        logo_user1 = new javax.swing.JLabel();
        txt_panggilan1 = new javax.swing.JLabel();
        admin1 = new javax.swing.JLabel();
        btn_transaksi = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        kasir_profil.setBackground(new java.awt.Color(63, 114, 175));
        kasir_profil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        kasir_profil.setBorder(null);
        kasir_profil.setBorderPainted(false);
        kasir_profil.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        kasir_profil.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                kasir_profilMouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        tbl_jual.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbl_jual);

        btn_tambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/trollynew.png"))); // NOI18N
        btn_tambah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_tambah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_tambahMouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Bayar :");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Total :");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Kembalian :");

        txt_Total.setEditable(false);
        txt_Total.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txt_Total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_TotalActionPerformed(evt);
            }
        });

        txt_kembalian.setEditable(false);
        txt_kembalian.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        txt_kembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_kembalianActionPerformed(evt);
            }
        });

        txt_bayar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        btn_bayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/bayar.png"))); // NOI18N
        btn_bayar.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_bayar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_bayarMouseClicked(evt);
            }
        });

        id_jual.setEditable(false);
        id_jual.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        id_jual.setText("id jual");
        id_jual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                id_jualActionPerformed(evt);
            }
        });

        tgljual.setEditable(false);
        tgljual.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N

        txt_cari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cariActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("TRANSAKSI JUAL");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Cari Barcode:");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_Total, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 561, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jLabel2)
                        .addGap(285, 285, 285)
                        .addComponent(btn_tambah))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(id_jual, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tgljual, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_kembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btn_bayar)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(btn_tambah))
                .addGap(53, 53, 53)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(id_jual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgljual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 291, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txt_Total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(9, 9, 9)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txt_kembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addComponent(btn_bayar)
                .addContainerGap())
        );

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("TOKO BAROKAH ATK");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(204, 204, 204));
        jLabel14.setText("Jl. Raya Besuk, Desa Alaskandang, Kec. Besuk, Kab. Probolinggo");

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
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(kasir_profil))
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
                        .addComponent(kasir_profil)
                        .addGap(90, 90, 90))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addGap(4, 4, 4)
                                .addComponent(jLabel14))
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)))
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(63, 114, 175));
        jPanel5.setPreferredSize(new java.awt.Dimension(200, 560));

        btn_dashboard1.setBackground(new java.awt.Color(63, 114, 175));
        btn_dashboard1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/dashboard.png"))); // NOI18N
        btn_dashboard1.setBorder(null);
        btn_dashboard1.setBorderPainted(false);
        btn_dashboard1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dashboard1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_dashboard1MouseClicked(evt);
            }
        });
        btn_dashboard1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dashboard1ActionPerformed(evt);
            }
        });

        btn_laporan1.setBackground(new java.awt.Color(63, 114, 175));
        btn_laporan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/laporan.png"))); // NOI18N
        btn_laporan1.setBorder(null);
        btn_laporan1.setBorderPainted(false);
        btn_laporan1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_laporan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_laporan1MouseClicked(evt);
            }
        });

        btn_dataBarang1.setBackground(new java.awt.Color(63, 114, 175));
        btn_dataBarang1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        btn_dataBarang1.setBorder(null);
        btn_dataBarang1.setBorderPainted(false);
        btn_dataBarang1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dataBarang1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dataBarang1ActionPerformed(evt);
            }
        });

        btn_dataKaryawan1.setBackground(new java.awt.Color(63, 114, 175));
        btn_dataKaryawan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/karyawan.png"))); // NOI18N
        btn_dataKaryawan1.setBorder(null);
        btn_dataKaryawan1.setBorderPainted(false);
        btn_dataKaryawan1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dataKaryawan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_dataKaryawan1MouseClicked(evt);
            }
        });
        btn_dataKaryawan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dataKaryawan1ActionPerformed(evt);
            }
        });

        btn_dataSupplier1.setBackground(new java.awt.Color(63, 114, 175));
        btn_dataSupplier1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/supplier.png"))); // NOI18N
        btn_dataSupplier1.setBorder(null);
        btn_dataSupplier1.setBorderPainted(false);
        btn_dataSupplier1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dataSupplier1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_dataSupplier1MouseClicked(evt);
            }
        });

        logo_user1.setForeground(new java.awt.Color(0, 0, 0));
        logo_user1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/user biru1 1.png"))); // NOI18N

        txt_panggilan1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        txt_panggilan1.setForeground(new java.awt.Color(204, 204, 204));
        txt_panggilan1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        txt_panggilan1.setText("Nama");
        txt_panggilan1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        txt_panggilan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_panggilan1MouseClicked(evt);
            }
        });

        admin1.setForeground(new java.awt.Color(204, 204, 204));
        admin1.setText("Admin");

        btn_transaksi.setBackground(new java.awt.Color(63, 114, 175));
        btn_transaksi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/transaksi.png"))); // NOI18N
        btn_transaksi.setBorder(null);
        btn_transaksi.setBorderPainted(false);
        btn_transaksi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_transaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_transaksiMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txt_panggilan1, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btn_dashboard1)
                                .addComponent(btn_dataBarang1)
                                .addComponent(btn_dataKaryawan1)
                                .addComponent(btn_laporan1)
                                .addComponent(btn_dataSupplier1)
                                .addComponent(btn_transaksi)))
                        .addGroup(jPanel5Layout.createSequentialGroup()
                            .addGap(82, 82, 82)
                            .addComponent(admin1))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 52, Short.MAX_VALUE)
                .addComponent(logo_user1)
                .addGap(48, 48, 48))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(logo_user1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_panggilan1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(admin1)
                .addGap(35, 35, 35)
                .addComponent(btn_dashboard1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dataBarang1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dataKaryawan1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dataSupplier1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_transaksi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_laporan1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 708, Short.MAX_VALUE)
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

    private void btn_bayarMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_bayarMouseClicked
        // TODO add your handling code here:
        try {
            this.stat = k.getCon().prepareStatement("Select ifnull(count(id_barang), 0) as p from keranjang");
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                int n = rs.getInt("p");
                if (n == 0) {
                    JOptionPane.showMessageDialog(null, "harus ada barang yang dibeli!");
                } else {
                    if (!txt_bayar.getText().matches(".*\\d.*")) {
                        JOptionPane.showMessageDialog(null, "Total bayar harus berupa angka!");
                    } else {
                        String kembaliStr = txt_kembalian.getText(); // "Rp 125.000"
                        double kembalian = formatUang.setDefault(kembaliStr);

                        if (kembalian < 0) {
                            JOptionPane.showMessageDialog(null, "Uang yang dibayar kurang!");
                        } else {
                            setDataStruk();
                            struk.isiStruk(data);
                            trjual();
                        }
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }//GEN-LAST:event_btn_bayarMouseClicked

    private void txt_kembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_kembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_kembalianActionPerformed

    private void txt_TotalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_TotalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_TotalActionPerformed

    private void btn_tambahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_tambahMouseClicked
        KeranjangTransaksi r = new KeranjangTransaksi();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_tambahMouseClicked

    private void kasir_profilMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_kasir_profilMouseClicked
        int jawab = JOptionPane.showConfirmDialog(
                this,
                "Apakah Anda yakin ingin Log Out?",
                "Konfirmasi",
                JOptionPane.YES_NO_OPTION
        );
        if (jawab == JOptionPane.YES_OPTION) {
            hapusKeranjang();
            Login c = new Login();
            c.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_kasir_profilMouseClicked

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void btn_dashboard1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_dashboard1MouseClicked
        adm_dashboard r = new adm_dashboard();
        if (keluar()) {
            r.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_dashboard1MouseClicked

    private void btn_dashboard1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dashboard1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_dashboard1ActionPerformed

    private void btn_laporan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_laporan1MouseClicked
        Laporan r = new Laporan();
        if (keluar()) {
            r.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_laporan1MouseClicked

    private void btn_dataBarang1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dataBarang1ActionPerformed
        DataBarang q = new DataBarang();
        if (keluar()) {
            q.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_dataBarang1ActionPerformed

    private void btn_dataKaryawan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_dataKaryawan1MouseClicked
        DataKaryawan r = new DataKaryawan();
        if (keluar()) {
            r.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_dataKaryawan1MouseClicked

    private void btn_dataKaryawan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dataKaryawan1ActionPerformed

    }//GEN-LAST:event_btn_dataKaryawan1ActionPerformed

    private void btn_dataSupplier1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_dataSupplier1MouseClicked
        DataSupplier r = new DataSupplier();
        if (keluar()) {
            r.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_dataSupplier1MouseClicked

    private void txt_panggilan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilan1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_panggilan1MouseClicked

    private void btn_transaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_transaksiMouseClicked

    }//GEN-LAST:event_btn_transaksiMouseClicked

    private void id_jualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_id_jualActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_id_jualActionPerformed

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
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Transaksi.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Transaksi().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel admin1;
    private javax.swing.JLabel btn_bayar;
    private javax.swing.JButton btn_dashboard1;
    private javax.swing.JButton btn_dataBarang1;
    private javax.swing.JButton btn_dataKaryawan1;
    private javax.swing.JButton btn_dataSupplier1;
    private javax.swing.JButton btn_laporan1;
    private javax.swing.JLabel btn_tambah;
    private javax.swing.JButton btn_transaksi;
    private javax.swing.JTextField id_jual;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton kasir_profil;
    private javax.swing.JLabel logo_user1;
    private javax.swing.JTable tbl_jual;
    private javax.swing.JTextField tgljual;
    private javax.swing.JTextField txt_Total;
    private javax.swing.JTextField txt_bayar;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JTextField txt_kembalian;
    private javax.swing.JLabel txt_panggilan1;
    // End of variables declaration//GEN-END:variables
}
