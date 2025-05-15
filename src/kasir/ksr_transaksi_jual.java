package kasir;

import barokah_atk.konek;
import com.sun.source.tree.TryTree;
import fungsi_lain.session;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 *
 * @author x260
 */
public class ksr_transaksi_jual extends javax.swing.JFrame {

    private tr_jual p;
    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    konek k = new konek();

    public ksr_transaksi_jual() {
        initComponents();
        k.connect();
        refreshTable();
        carirfid();
        this.setLocationRelativeTo(null);
        generateIdTRJ();
        gettanggal();
        SwingUtilities.invokeLater(() -> txt_cari.requestFocusInWindow());
    }

    class tr_jual extends ksr_transaksi_jual {

        String bayar, date, id_jual, id_barang, nama_barang, jumlah;
        double harga;

        public tr_jual() {
            this.bayar = txt_bayar.getText();

        }

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

    public void refreshTable() {
        model = new DefaultTableModel();
        model.addColumn("id_barang");
        model.addColumn("nama barang");
        model.addColumn("harga");
        model.addColumn("jumlah");
        model.addColumn("total");
        tbl_jual.setModel(model);

        try {
            this.stat = k.getCon().prepareStatement("select id_barang, nama_barang, harga, sum(jumlah) as jumlah, sum(total) as total"
                    + " from keranjang "
                    + "group by id_barang, nama_barang;");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    rs.getString("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getString("harga"),
                    rs.getString("jumlah"),
                    rs.getString("total")
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

                txt_total.setText(rs.getString("total"));
            }
        } catch (Exception e) {
        }

    }

    public void gettanggal() {
        LocalDate tgll = LocalDate.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("YYYY-MM-dd");
        String tanggal = tgll.format(df);
        tgljual.setText(tanggal);
    }

    //pop up jumlah
    public void carirfid() {
        txt_cari.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String uid = txt_cari.getText().trim();
                    txt_cari.setText("");
                    showbarang(uid);

                }
            }
        });
    }

    //show barang ke pop up
    private void showbarang(String uid) {
        tr_jual s = new tr_jual();
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
                    System.out.println("jumlah : " + jumlahBarang);
                    dataBarang(uid, jumlahBarang);

                }
            } else {
                JOptionPane.showMessageDialog(null, "Tidak DItemukan");
            }

            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //tambah ke keranjang 
    private void tambah(tr_jual t, int jumlahBarang) {
        double total = jumlahBarang * t.harga;
        String totall = Double.toString(total);
        String hargajual = Double.toString(t.harga);

        System.out.println("barang: " + t.id_barang + " " + t.nama_barang + " " + t.harga);

        try {
            this.stat = k.getCon().prepareStatement("insert into keranjang values (?, ?, ?, ?, ?)");
            stat.setString(1, t.id_barang);
            stat.setString(2, t.nama_barang);
            stat.setString(3, hargajual);
            stat.setInt(4, jumlahBarang);
            stat.setString(5, totall);
            stat.executeUpdate();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //ambil data barang
    private void dataBarang(String uid, int jumlahBarang) {
        tr_jual db = new tr_jual();
        try {
            this.stat = k.getCon().prepareStatement("select * from barang where id_barcode = ?");
            stat.setString(1, uid.trim());
            System.out.println("hai bardode : " + uid);
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                System.out.println("Barang ditemukan: " + rs.getString("nama_barang"));
                db.id_barang = rs.getString("id_barang");
                db.nama_barang = rs.getString("nama_barang");
                db.harga = rs.getDouble("harga_jual");

                System.out.println("jumlah" + jumlahBarang);
                System.out.println("harga " + db.harga);
                tambah(db, jumlahBarang);

            }

        } catch (Exception e) {
            e.printStackTrace();
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
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_jual = new javax.swing.JTable();
        btn_tambah = new javax.swing.JLabel();
        btn_keranjang = new javax.swing.JLabel();
        btn_back = new javax.swing.JLabel();
        btn_hitung = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        txt_total = new javax.swing.JTextField();
        txt_kembalian = new javax.swing.JTextField();
        txt_bayar = new javax.swing.JTextField();
        btn_bayar = new javax.swing.JLabel();
        id_jual = new javax.swing.JTextField();
        tgljual = new javax.swing.JTextField();
        txt_cari = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
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
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/transaksi.png"))); // NOI18N
        jButton4.setBorder(null);
        jButton4.setBorderPainted(false);
        jButton4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton4MouseClicked(evt);
            }
        });
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/userhitam.png"))); // NOI18N

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
        jLabel16.setText("Kasir");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton1)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(52, 52, 52)
                        .addComponent(jLabel13)))
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel16)
                .addGap(86, 86, 86))
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

        btn_tambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/tambah.png"))); // NOI18N
        btn_tambah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_tambahMouseClicked(evt);
            }
        });

        btn_keranjang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/keranjang.png"))); // NOI18N
        btn_keranjang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_keranjangMouseClicked(evt);
            }
        });

        btn_back.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        btn_back.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_backMouseClicked(evt);
            }
        });

        btn_hitung.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/hitung.png"))); // NOI18N
        btn_hitung.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_hitungMouseClicked(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Bayar :");

        jLabel8.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 0));
        jLabel8.setText("Total :");

        jLabel9.setFont(new java.awt.Font("Times New Roman", 0, 18)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Kembalian :");

        txt_total.setEditable(false);
        txt_total.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_totalActionPerformed(evt);
            }
        });

        txt_kembalian.setEditable(false);
        txt_kembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_kembalianActionPerformed(evt);
            }
        });

        btn_bayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/bayar.png"))); // NOI18N
        btn_bayar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_bayarMouseClicked(evt);
            }
        });

        id_jual.setEditable(false);
        id_jual.setText("id jual");

        tgljual.setEditable(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("TRANSAKSI JUAL");

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
                        .addComponent(txt_total, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 551, Short.MAX_VALUE)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(btn_tambah)
                        .addGap(265, 265, 265)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_keranjang))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_back, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(id_jual, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tgljual, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txt_kembalian, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(btn_hitung)
                                .addGap(18, 18, 18)
                                .addComponent(btn_bayar))
                            .addComponent(txt_cari, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_keranjang)
                    .addComponent(btn_tambah)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING))
                .addGap(56, 56, 56)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(id_jual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tgljual, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(txt_total, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(9, 9, 9)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(txt_kembalian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_back)
                    .addComponent(btn_hitung)
                    .addComponent(btn_bayar))
                .addContainerGap())
        );

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("TOKO BAROKAH");

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
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jButton6))
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
                        .addComponent(jButton6)
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

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed

    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton4MouseClicked

    }//GEN-LAST:event_jButton4MouseClicked

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked
        ksr_profil r = new ksr_profil();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton6MouseClicked

    private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
        ksr_dashboard r = new ksr_dashboard();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton1MouseClicked

    private void jButton3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton3MouseClicked
        ksr_dt_barang r = new ksr_dt_barang();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jButton3MouseClicked

    private void txt_totalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_totalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_totalActionPerformed

    private void txt_kembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_kembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_kembalianActionPerformed

    private void btn_tambahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_tambahMouseClicked
        ksr_tbh_transaksi_jual r = new ksr_tbh_transaksi_jual();
        r.setVisible(true);
        this.setVisible(false);


    }//GEN-LAST:event_btn_tambahMouseClicked

    private void btn_keranjangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_keranjangMouseClicked
        // TODO add your handling code here:
        ksr_transaksi_keranjang_jual p = new ksr_transaksi_keranjang_jual();
        p.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_btn_keranjangMouseClicked

    private void btn_hitungMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_hitungMouseClicked
        // TODO add your handling code here:
        tr_jual h = new tr_jual();
        double total = Double.parseDouble(txt_total.getText());
        double bayar = Double.parseDouble(txt_bayar.getText());
        double kembalian = bayar - total;
        String kembaliann = Double.toString(kembalian);
        if (bayar >= total) {
            txt_kembalian.setText(kembaliann);

        } else {
            JOptionPane.showMessageDialog(null, "uang tidak cukup");
        }

    }//GEN-LAST:event_btn_hitungMouseClicked
    private void trjual() {
        if (p == null) {
            p = new tr_jual();
        }
        tr_jual w = new tr_jual();

        // insert tabel penjualan
        try {
             String idkaryawan = session.getInstance().getId();
                this.stat = k.getCon().prepareStatement("insert into penjualan values (?, ?, ?, ?)");
                stat.setString(1, id_jual.getText());
                stat.setString(2, tgljual.getText());
                stat.setString(3, "KY003");
                stat.setString(4, txt_total.getText());
                stat.executeUpdate();

                // insert detail penjualan
                try {
                    String idbarang = "";
                    String jumlah = "";
                    String total = "";

                    this.stat = k.getCon().prepareStatement("select id_barang, nama_barang, harga, sum(jumlah) as jumlah, sum(total) as total "
                            + "from keranjang "
                            + "group by id_barang, nama_barang");
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
                        refreshTable();

                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(null, e.getMessage());
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, e.getMessage());
                }
                JOptionPane.showMessageDialog(null, "pembayaran berhasil!");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        txt_bayar.setText("");
        txt_kembalian.setText("");
        generateIdTRJ();
    }
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
                    trjual();
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }//GEN-LAST:event_btn_bayarMouseClicked

    private void btn_backMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_backMouseClicked
        ksr_transaksi r = new ksr_transaksi();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_btn_backMouseClicked

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:
        user_profile r = new user_profile();
        r.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_txt_panggilanMouseClicked

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
            java.util.logging.Logger.getLogger(ksr_transaksi_jual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ksr_transaksi_jual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ksr_transaksi_jual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ksr_transaksi_jual.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
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
                new ksr_transaksi_jual().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btn_back;
    private javax.swing.JLabel btn_bayar;
    private javax.swing.JLabel btn_hitung;
    private javax.swing.JLabel btn_keranjang;
    private javax.swing.JLabel btn_tambah;
    private javax.swing.JTextField id_jual;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tbl_jual;
    private javax.swing.JTextField tgljual;
    private javax.swing.JTextField txt_bayar;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JTextField txt_kembalian;
    private javax.swing.JLabel txt_panggilan;
    private javax.swing.JTextField txt_total;
    // End of variables declaration//GEN-END:variables
}
