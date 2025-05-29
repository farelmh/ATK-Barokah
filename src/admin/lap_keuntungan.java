package admin;

import barokah_atk.Login;
import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatTanggal;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import java.awt.Color;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;

public class lap_keuntungan extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private final int[] index = {1, 2, 3};
    private final int[] indexUang = {1, 2, 3};
    private final int[] indexTanggal = {0};
    
    konek k = new konek();

    public lap_keuntungan() {
        initComponents();
        setTanggal();
        this.setLocationRelativeTo(null);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Pencarian"));
        k.connect();
        tabelKeuntungan();
        CariData.TableSorter(tbl_untung, txt_cari1, index, indexUang, null, indexTanggal);
    }

    // set tanggal ke bahasa indo
    private void setTanggal() {
        formatTanggal.setTanggalIndo(tgl_mulai1);
        formatTanggal.setTanggalIndo(tgl_akhir1);
    }

    //ambil data tanggal mulai
    private String getTanggalMulai() {
        try {
            Date tgl = tgl_mulai1.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    //ambil data tanggal akhir
    private String getTanggalAkhir() {
        try {
            Date tgl = tgl_akhir1.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    //tabel
    public void tabelKeuntungan() {
        model = new DefaultTableModel();
        model.addColumn("Tanggal");
        model.addColumn("Total Penjualan");
        model.addColumn("Total Modal");
        model.addColumn("Keuntungan");
        tbl_untung.setModel(model);
        modelTabel.setModel(tbl_untung);

        try {
            this.stat = k.getCon().prepareStatement("SELECT * FROM v_lap_keuntungan");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                String totalJual = formatUang.formatRp(rs.getDouble("total_penjualan"));
                String totalModal = formatUang.formatRp(rs.getDouble("total_modal"));
                String keuntungan = formatUang.formatRp(rs.getDouble("keuntungan"));

                Object[] data = {
                    rs.getString("tanggal_penjualan"),
                    "Rp  " + totalJual,
                    "Rp  " + totalModal,
                    "Rp  " + keuntungan
                };
                model.addRow(data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void cari() {
        model.setRowCount(0);
        try {
            String query = "CALL getKeuntunganPerTanggal(?, ?)";
            this.stat = k.getCon().prepareStatement(query);
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();

            double laba = 0;
            while (rs.next()) {
                String totalJual = formatUang.formatRp(rs.getDouble("total_penjualan"));
                String totalModal = formatUang.formatRp(rs.getDouble("total_modal"));
                String keuntungann = formatUang.formatRp(rs.getDouble("keuntungan"));

                String untung = rs.getString("keuntungan");
                double keuntungan = Double.parseDouble(untung);
                laba += keuntungan;
                Object[] data = {
                    rs.getString("tanggal_penjualan"),
                    "Rp  " + totalJual,
                    "Rp  " + totalModal,
                    "Rp  " + keuntungann

                };
                model.addRow(data);
                String laba1 = formatUang.formatRp(laba);
                txt_labaKotor.setText("Rp  " + laba1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        getBiayaOperasional();
        getLabaBersih();
    }

    private void getBiayaOperasional() {
        try {
            this.stat = k.getCon().prepareStatement("select sum(jumlah) as total from biaya_operasional where tanggal between ? and ?");
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();
            if (rs.next()) {
                double total = rs.getDouble("total");
                String format = formatUang.formatRp(total);
                txt_operasional.setText("Rp " + format);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
    
    private void getLabaBersih() {
        double labaKotor = formatUang.setDefault(txt_labaKotor.getText());
        double operasional = formatUang.setDefault(txt_operasional.getText());
        
        double labaBersih = labaKotor - operasional;
        String format = formatUang.formatRp(labaBersih);
        
        if (labaBersih < 0) {
            txt_labaBersih.setForeground(Color.red);
        } else {
            txt_labaBersih.setForeground(new Color(0, 150, 0));
        }
        txt_labaBersih.setText("Rp " + format);
    }
    
    public void resetPencarian() {
        tgl_mulai1.setDate(null);
        tgl_akhir1.setDate(null);
        txt_cari1.setText("");
        txt_labaKotor.setText("");
        txt_operasional.setText("");
        txt_labaBersih.setText("");
        tabelKeuntungan(); 
        CariData.resetTableSorting(tbl_untung);
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
        btn_dashboard = new javax.swing.JButton();
        btn_laporan = new javax.swing.JButton();
        btn_barang = new javax.swing.JButton();
        btn_karyawan = new javax.swing.JButton();
        btn_supplier = new javax.swing.JButton();
        user = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        admin = new javax.swing.JLabel();
        btn_transaksi = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btn_logout = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_untung = new javax.swing.JTable();
        btn_kembali = new javax.swing.JLabel();
        txt_labaBersih = new javax.swing.JTextField();
        total = new javax.swing.JLabel();
        judul = new javax.swing.JLabel();
        btn_ekspor = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tgl_mulai1 = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        tgl_akhir1 = new com.toedter.calendar.JDateChooser();
        btn_cari1 = new javax.swing.JButton();
        txt_cari1 = new javax.swing.JTextField();
        btn_cari4 = new javax.swing.JButton();
        total1 = new javax.swing.JLabel();
        txt_operasional = new javax.swing.JTextField();
        total2 = new javax.swing.JLabel();
        txt_labaKotor = new javax.swing.JTextField();
        logo_pensil = new javax.swing.JLabel();
        toko_barokah = new javax.swing.JLabel();
        alamat_toko = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(17, 45, 78));
        jPanel1.setPreferredSize(new java.awt.Dimension(990, 584));

        jPanel2.setBackground(new java.awt.Color(63, 114, 175));
        jPanel2.setPreferredSize(new java.awt.Dimension(200, 560));

        btn_dashboard.setBackground(new java.awt.Color(63, 114, 175));
        btn_dashboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/dashboard.png"))); // NOI18N
        btn_dashboard.setBorder(null);
        btn_dashboard.setBorderPainted(false);
        btn_dashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_dashboardMouseClicked(evt);
            }
        });
        btn_dashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dashboardActionPerformed(evt);
            }
        });

        btn_laporan.setBackground(new java.awt.Color(63, 114, 175));
        btn_laporan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/laporan.png"))); // NOI18N
        btn_laporan.setBorder(null);
        btn_laporan.setBorderPainted(false);
        btn_laporan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_laporanMouseClicked(evt);
            }
        });

        btn_barang.setBackground(new java.awt.Color(63, 114, 175));
        btn_barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        btn_barang.setBorder(null);
        btn_barang.setBorderPainted(false);
        btn_barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_barangMouseClicked(evt);
            }
        });

        btn_karyawan.setBackground(new java.awt.Color(63, 114, 175));
        btn_karyawan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/karyawan.png"))); // NOI18N
        btn_karyawan.setBorder(null);
        btn_karyawan.setBorderPainted(false);
        btn_karyawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_karyawanMouseClicked(evt);
            }
        });

        btn_supplier.setBackground(new java.awt.Color(63, 114, 175));
        btn_supplier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/supplier.png"))); // NOI18N
        btn_supplier.setBorder(null);
        btn_supplier.setBorderPainted(false);
        btn_supplier.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_supplierMouseClicked(evt);
            }
        });

        user.setForeground(new java.awt.Color(0, 0, 0));
        user.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/user biru1 1.png"))); // NOI18N

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

        admin.setForeground(new java.awt.Color(204, 204, 204));
        admin.setText("Admin");

        btn_transaksi.setBackground(new java.awt.Color(63, 114, 175));
        btn_transaksi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/transaksi.png"))); // NOI18N
        btn_transaksi.setBorder(null);
        btn_transaksi.setBorderPainted(false);
        btn_transaksi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_transaksiMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(user)
                .addGap(48, 48, 48))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_dashboard)
                            .addComponent(btn_barang)
                            .addComponent(btn_karyawan)
                            .addComponent(btn_laporan)
                            .addComponent(btn_supplier)
                            .addComponent(btn_transaksi)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(82, 82, 82)
                        .addComponent(admin)))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(user)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_panggilan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(admin)
                .addGap(35, 35, 35)
                .addComponent(btn_dashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_barang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_karyawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_supplier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_transaksi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_laporan)
                .addContainerGap(160, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        btn_logout.setBackground(new java.awt.Color(63, 114, 175));
        btn_logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        btn_logout.setBorder(null);
        btn_logout.setBorderPainted(false);
        btn_logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_logoutMouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        tbl_untung.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbl_untung);

        btn_kembali.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembali.png"))); // NOI18N
        btn_kembali.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_kembaliMouseClicked(evt);
            }
        });

        txt_labaBersih.setEditable(false);

        total.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        total.setForeground(new java.awt.Color(51, 51, 51));
        total.setText("Total Laba Bersih");

        judul.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        judul.setForeground(new java.awt.Color(0, 0, 0));
        judul.setText("Laporan Keuntungan");

        btn_ekspor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/exporexcel.png"))); // NOI18N
        btn_ekspor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_eksporMouseClicked(evt);
            }
        });

        filterPanel.setBackground(new java.awt.Color(218, 212, 181));

        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Dari");

        tgl_mulai1.setBackground(new java.awt.Color(218, 212, 181));

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Sampai");

        tgl_akhir1.setBackground(new java.awt.Color(218, 212, 181));

        btn_cari1.setText("cari");
        btn_cari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari1ActionPerformed(evt);
            }
        });

        txt_cari1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_cari1MouseClicked(evt);
            }
        });
        txt_cari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_cari1ActionPerformed(evt);
            }
        });

        btn_cari4.setText("reset");
        btn_cari4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari4ActionPerformed(evt);
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
                .addComponent(tgl_mulai1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_akhir1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_cari1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_cari4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
                .addComponent(txt_cari1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_mulai1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_akhir1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_cari1)
                        .addComponent(txt_cari1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cari4)))
                .addGap(3, 3, 3))
        );

        total1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        total1.setForeground(new java.awt.Color(51, 51, 51));
        total1.setText("Biaya Operasional");

        txt_operasional.setEditable(false);

        total2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        total2.setForeground(new java.awt.Color(51, 51, 51));
        total2.setText("Total Laba Kotor");

        txt_labaKotor.setEditable(false);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(judul)
                .addGap(429, 429, 429))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btn_kembali, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_ekspor, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(filterPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(total2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txt_labaKotor, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(total)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txt_labaBersih, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(total1)
                                .addGap(18, 18, 18)
                                .addComponent(txt_operasional, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(judul)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(total1)
                    .addComponent(total2)
                    .addComponent(txt_labaKotor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_operasional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_labaBersih, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(total))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_kembali)
                    .addComponent(btn_ekspor))
                .addContainerGap())
        );

        logo_pensil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        toko_barokah.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        toko_barokah.setForeground(new java.awt.Color(255, 255, 255));
        toko_barokah.setText("TOKO BAROKAH");

        alamat_toko.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        alamat_toko.setForeground(new java.awt.Color(204, 204, 204));
        alamat_toko.setText("Alamat toko");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(logo_pensil)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(toko_barokah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_logout))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(alamat_toko)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btn_logout)
                        .addGap(87, 87, 87))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(toko_barokah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(alamat_toko))
                            .addComponent(logo_pensil))
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

    private void btn_dashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dashboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_dashboardActionPerformed

    private void btn_logoutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_logoutMouseClicked
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
    }//GEN-LAST:event_btn_logoutMouseClicked

    private void btn_dashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_dashboardMouseClicked
        adm_dashboard a = new adm_dashboard();
        a.setVisible(true);
        this.dispose();

    }//GEN-LAST:event_btn_dashboardMouseClicked

    private void btn_barangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_barangMouseClicked
        dataBarang b = new dataBarang();
        b.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_barangMouseClicked

    private void btn_karyawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_karyawanMouseClicked
        dataKaryawan k = new dataKaryawan();
        k.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_karyawanMouseClicked

    private void btn_kembaliMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_kembaliMouseClicked
        laporan l = new laporan();
        l.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_kembaliMouseClicked

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void btn_eksporMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_eksporMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_eksporMouseClicked

    private void btn_supplierMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_supplierMouseClicked
        // TODO add your handling code here:
        dataSupplier s = new dataSupplier();
        s.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_supplierMouseClicked

    private void btn_laporanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_laporanMouseClicked
        // TODO add your handling code here:
        laporan l = new laporan();
        l.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_laporanMouseClicked

    private void btn_cari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari1ActionPerformed
        // TODO add your handling code here:
        Date start = tgl_mulai1.getDate();
        Date end = tgl_akhir1.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(null, "tanggal tidak boleh kosong");
        } else if (start.after(end)) {
            JOptionPane.showMessageDialog(null, "tanggal awal tidak boleh lebih dari tanggal akhir");
        } else {
            cari();
        }
    }//GEN-LAST:event_btn_cari1ActionPerformed

    private void txt_cari1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_cari1MouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cari1MouseClicked

    private void txt_cari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cari1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cari1ActionPerformed

    private void btn_cari4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari4ActionPerformed
        // TODO add your handling code here:
        resetPencarian();
    }//GEN-LAST:event_btn_cari4ActionPerformed

    private void btn_transaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_transaksiMouseClicked
        // TODO add your handling code here:
        Transaksi a = new Transaksi();
        a.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_transaksiMouseClicked

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
            java.util.logging.Logger.getLogger(lap_keuntungan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(lap_keuntungan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(lap_keuntungan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(lap_keuntungan.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new lap_keuntungan().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel admin;
    private javax.swing.JLabel alamat_toko;
    private javax.swing.JButton btn_barang;
    private javax.swing.JButton btn_cari1;
    private javax.swing.JButton btn_cari4;
    private javax.swing.JButton btn_dashboard;
    private javax.swing.JLabel btn_ekspor;
    private javax.swing.JButton btn_karyawan;
    private javax.swing.JLabel btn_kembali;
    private javax.swing.JButton btn_laporan;
    private javax.swing.JButton btn_logout;
    private javax.swing.JButton btn_supplier;
    private javax.swing.JButton btn_transaksi;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel judul;
    private javax.swing.JLabel logo_pensil;
    private javax.swing.JTable tbl_untung;
    private com.toedter.calendar.JDateChooser tgl_akhir1;
    private com.toedter.calendar.JDateChooser tgl_mulai1;
    private javax.swing.JLabel toko_barokah;
    private javax.swing.JLabel total;
    private javax.swing.JLabel total1;
    private javax.swing.JLabel total2;
    private javax.swing.JTextField txt_cari1;
    private javax.swing.JTextField txt_labaBersih;
    private javax.swing.JTextField txt_labaKotor;
    private javax.swing.JTextField txt_operasional;
    private javax.swing.JLabel txt_panggilan;
    private javax.swing.JLabel user;
    // End of variables declaration//GEN-END:variables
}
