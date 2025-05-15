package kasir;

import barokah_atk.Login;
import barokah_atk.konek;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.PreparedStatement;
import javax.swing.JOptionPane;
import javax.swing.*;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import fungsi_lain.session;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

public class ksr_dashboard extends javax.swing.JFrame {
    private PreparedStatement stat;
    private ResultSet rs;
    private boolean tekan = false;
    konek k = new konek();
    String namaPanggilan = session.getInstance().getNama();

    public ksr_dashboard() {
        initComponents();
        k.connect();
        this.setLocationRelativeTo(null);
        totalBarang();
        SwingUtilities.invokeLater(() -> showPieChart());
        showBarChart();
        txt_panggilan.setText(namaPanggilan);
        btn_jual.setVisible(false);
        btn_beli.setVisible(false);
    }

    private void totalBarang() {
        try {
            this.stat = k.getCon().prepareStatement("Select count(id_barang) as banyak from barang");
            this.rs = this.stat.executeQuery();
            if (rs.next()) {
                String banyak = rs.getString("banyak");
                brg_tipis.setText(banyak);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    //grafik barang terlaris
    private void showPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            this.stat = k.getCon().prepareStatement("CALL barangTerlaris()");
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                String namaBarang = rs.getString("nama_barang");
                double totalTerjual = rs.getDouble("banyak");
                dataset.setValue(namaBarang, totalTerjual);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal mengambil data dari database!", "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Buat pie chart
        JFreeChart pieChart = ChartFactory.createPieChart("5 Barang Paling Laris", dataset, true, true, false);
        PiePlot piePlot = (PiePlot) pieChart.getPlot();
        piePlot.setBackgroundPaint(Color.white);

        // Daftar warna dasar
        List<Color> colors = Arrays.asList(
                Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW,
                Color.ORANGE, Color.MAGENTA, Color.CYAN, Color.PINK,
                Color.GRAY, new Color(128, 0, 128) // Purple (karena Java tidak punya Purple)
        );

        // Set warna untuk tiap bagian pie chart
        int index = 0;
        for (Object key : dataset.getKeys()) {
            piePlot.setSectionPaint((Comparable) key, colors.get(index % colors.size())); // Pakai warna secara berulang
            index++;
        }

        // Tampilkan chart di panel
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setPreferredSize(new Dimension(200, 100));

        panelBarChart.removeAll();
        panelBarChart.setLayout(new BorderLayout());
        panelBarChart.add(chartPanel, BorderLayout.CENTER);
        panelBarChart.revalidate();
        panelBarChart.repaint();
    }

    //ambil data keuntungan
    private DefaultCategoryDataset getKeuntunganDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            String query = "SELECT karyawan.nama_panggilan, COUNT(penjualan.id_jual) AS total\n"
                    + "FROM penjualan\n"
                    + "JOIN karyawan ON karyawan.id_karyawan = penjualan.id_karyawan\n"
                    + "GROUP BY karyawan.id_karyawan\n"
                    + "ORDER by total DESC";
            this.stat = k.getCon().prepareStatement(query);
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                String nama = rs.getString("nama_panggilan");
                int total = rs.getInt("total");
                dataset.setValue(total, "total penjualan", nama);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataset;
    }

    //tampilkan top contributor
    private void showBarChart() {
        DefaultCategoryDataset dataset = getKeuntunganDataset();

        JFreeChart barChart = ChartFactory.createBarChart(
                "Top Kontribusi", // Judul
                "Nama", // Sumbu X
                "Total", // Sumbu Y
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false);

        // Modifikasi tampilan plot
        CategoryPlot plot = barChart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.BLUE);
        plot.setBackgroundPaint(Color.WHITE);

        // Gunakan BarRenderer polos
        BarRenderer renderer = new BarRenderer();
        renderer.setDrawBarOutline(false); // tidak ada border batang
        renderer.setBarPainter(new StandardBarPainter()); // ini menghilangkan efek 3D/kilau
        renderer.setSeriesPaint(0, new Color(204, 0, 51)); // warna batang polos

        plot.setRenderer(renderer);

        // Tampilkan chart ke panel
        ChartPanel chartPanel = new ChartPanel(barChart);
        panelLineChart.removeAll(); // ganti panel sesuai kebutuhan
        panelLineChart.add(chartPanel, BorderLayout.CENTER);
        panelLineChart.validate();
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
        btn_dataBarang = new javax.swing.JButton();
        btn_dataKaryawan = new javax.swing.JButton();
        logo_user = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        admin = new javax.swing.JLabel();
        btn_jual = new javax.swing.JButton();
        btn_beli = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btn_logout = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jmljual = new javax.swing.JLabel();
        pemasukan = new javax.swing.JLabel();
        sc_pemasukan = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jmlkaryawan = new javax.swing.JLabel();
        karyawan = new javax.swing.JLabel();
        sc_karyawan = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        brg_tipis = new javax.swing.JLabel();
        stoktipis = new javax.swing.JLabel();
        sc_barang = new javax.swing.JLabel();
        panelLineChart = new javax.swing.JPanel();
        panelBarChart = new javax.swing.JPanel();
        toko_barokah = new javax.swing.JLabel();
        alamat_toko = new javax.swing.JLabel();
        logo_atk = new javax.swing.JLabel();

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
        btn_dashboard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dashboardActionPerformed(evt);
            }
        });

        btn_dataBarang.setBackground(new java.awt.Color(63, 114, 175));
        btn_dataBarang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        btn_dataBarang.setBorder(null);
        btn_dataBarang.setBorderPainted(false);
        btn_dataBarang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dataBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dataBarangActionPerformed(evt);
            }
        });

        btn_dataKaryawan.setBackground(new java.awt.Color(63, 114, 175));
        btn_dataKaryawan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/transaksi.png"))); // NOI18N
        btn_dataKaryawan.setBorder(null);
        btn_dataKaryawan.setBorderPainted(false);
        btn_dataKaryawan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_dataKaryawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_dataKaryawanMouseClicked(evt);
            }
        });
        btn_dataKaryawan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_dataKaryawanActionPerformed(evt);
            }
        });

        logo_user.setForeground(new java.awt.Color(0, 0, 0));
        logo_user.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/user biru1 1.png"))); // NOI18N

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

        btn_jual.setText("Jual");
        btn_jual.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_jualMouseClicked(evt);
            }
        });
        btn_jual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_jualActionPerformed(evt);
            }
        });

        btn_beli.setText("Beli / Restock");
        btn_beli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_beliActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(logo_user)
                .addGap(48, 48, 48))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btn_beli, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_panggilan, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(82, 82, 82)
                            .addComponent(admin))
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btn_dashboard)
                                .addComponent(btn_dataBarang)
                                .addComponent(btn_dataKaryawan)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addContainerGap()
                            .addComponent(btn_jual, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addComponent(logo_user)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_panggilan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(admin)
                .addGap(35, 35, 35)
                .addComponent(btn_dashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dataBarang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_dataKaryawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_jual, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_beli, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        btn_logout.setBackground(new java.awt.Color(63, 114, 175));
        btn_logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        btn_logout.setBorder(null);
        btn_logout.setBorderPainted(false);
        btn_logout.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_logout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_logoutMouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        jPanel7.setBackground(new java.awt.Color(0, 51, 102));
        jPanel7.setPreferredSize(new java.awt.Dimension(150, 150));

        jmljual.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jmljual.setForeground(new java.awt.Color(255, 255, 255));
        jmljual.setText("Jual");

        pemasukan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        pemasukan.setForeground(new java.awt.Color(204, 204, 204));
        pemasukan.setText("Mulai Transaksi Jual");

        sc_pemasukan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/panahbirukiri.png"))); // NOI18N
        sc_pemasukan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sc_pemasukan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sc_pemasukanMouseClicked(evt);
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
                        .addComponent(jmljual)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(pemasukan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                        .addComponent(sc_pemasukan)))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(sc_pemasukan))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addComponent(jmljual)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(pemasukan)))
                .addContainerGap())
        );

        jPanel9.setBackground(new java.awt.Color(0, 102, 102));
        jPanel9.setPreferredSize(new java.awt.Dimension(150, 150));

        jmlkaryawan.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jmlkaryawan.setForeground(new java.awt.Color(255, 255, 255));
        jmlkaryawan.setText("Beli / Restock");

        karyawan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        karyawan.setForeground(new java.awt.Color(204, 204, 204));
        karyawan.setText("Mulai Transaksi Beli");

        sc_karyawan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/panahijo.png"))); // NOI18N
        sc_karyawan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sc_karyawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sc_karyawanMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(karyawan)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                        .addComponent(sc_karyawan))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jmlkaryawan)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jmlkaryawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sc_karyawan)
                    .addComponent(karyawan))
                .addContainerGap())
        );

        jPanel10.setBackground(new java.awt.Color(17, 45, 78));
        jPanel10.setPreferredSize(new java.awt.Dimension(150, 150));

        brg_tipis.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        brg_tipis.setForeground(new java.awt.Color(255, 255, 255));
        brg_tipis.setText("brg");

        stoktipis.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        stoktipis.setForeground(new java.awt.Color(204, 204, 204));
        stoktipis.setText("Total Barang");

        sc_barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/panahkanan.png"))); // NOI18N
        sc_barang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        sc_barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sc_barangMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(stoktipis)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 183, Short.MAX_VALUE)
                        .addComponent(sc_barang))
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addComponent(brg_tipis)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(brg_tipis)
                .addGap(3, 3, 3)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sc_barang)
                    .addComponent(stoktipis))
                .addContainerGap(8, Short.MAX_VALUE))
        );

        panelLineChart.setBackground(new java.awt.Color(255, 255, 255));
        panelLineChart.setLayout(new java.awt.BorderLayout());

        panelBarChart.setBackground(new java.awt.Color(255, 255, 255));
        panelBarChart.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(panelLineChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(15, 15, 15)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel10, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(panelBarChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelLineChart, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
                    .addComponent(panelBarChart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        toko_barokah.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        toko_barokah.setForeground(new java.awt.Color(255, 255, 255));
        toko_barokah.setText("TOKO BAROKAH");

        alamat_toko.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        alamat_toko.setForeground(new java.awt.Color(204, 204, 204));
        alamat_toko.setText("Alamat toko");

        logo_atk.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

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
                        .addComponent(logo_atk)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(alamat_toko)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(toko_barokah)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btn_logout)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(btn_logout)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap(15, Short.MAX_VALUE)
                        .addComponent(toko_barokah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(alamat_toko)
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(logo_atk)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

    private void sc_barangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sc_barangMouseClicked
        ksr_dataBarang q = new ksr_dataBarang();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_sc_barangMouseClicked

    private void sc_pemasukanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sc_pemasukanMouseClicked
        ksr_transaksi_jual q = new ksr_transaksi_jual();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_sc_pemasukanMouseClicked

    private void sc_karyawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sc_karyawanMouseClicked
        ksr_transaksi_beli q = new ksr_transaksi_beli();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_sc_karyawanMouseClicked

    private void btn_dataKaryawanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dataKaryawanActionPerformed

    }//GEN-LAST:event_btn_dataKaryawanActionPerformed

    private void btn_dataKaryawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_dataKaryawanMouseClicked
        if (!tekan) {
            btn_jual.setVisible(true);
            btn_beli.setVisible(true);
            tekan = true;
        } else if (tekan) {
            btn_jual.setVisible(false);
            btn_beli.setVisible(false);
            tekan = false;
        }
    }//GEN-LAST:event_btn_dataKaryawanMouseClicked

    private void btn_dataBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dataBarangActionPerformed
        ksr_dataBarang q = new ksr_dataBarang();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_dataBarangActionPerformed

    private void btn_dashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_dashboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_dashboardActionPerformed

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void btn_jualMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_jualMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_jualMouseClicked

    private void btn_jualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_jualActionPerformed
        // TODO add your handling code here:
        ksr_transaksi_jual q = new ksr_transaksi_jual();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_jualActionPerformed

    private void btn_beliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_beliActionPerformed
        // TODO add your handling code here:
        ksr_transaksi_beli q = new ksr_transaksi_beli();
        q.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_beliActionPerformed

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
            java.util.logging.Logger.getLogger(ksr_dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ksr_dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ksr_dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ksr_dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                new ksr_dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel admin;
    private javax.swing.JLabel alamat_toko;
    private javax.swing.JLabel brg_tipis;
    private javax.swing.JButton btn_beli;
    private javax.swing.JButton btn_dashboard;
    private javax.swing.JButton btn_dataBarang;
    private javax.swing.JButton btn_dataKaryawan;
    private javax.swing.JButton btn_jual;
    private javax.swing.JButton btn_logout;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel jmljual;
    private javax.swing.JLabel jmlkaryawan;
    private javax.swing.JLabel karyawan;
    private javax.swing.JLabel logo_atk;
    private javax.swing.JLabel logo_user;
    private javax.swing.JPanel panelBarChart;
    private javax.swing.JPanel panelLineChart;
    private javax.swing.JLabel pemasukan;
    private javax.swing.JLabel sc_barang;
    private javax.swing.JLabel sc_karyawan;
    private javax.swing.JLabel sc_pemasukan;
    private javax.swing.JLabel stoktipis;
    private javax.swing.JLabel toko_barokah;
    private javax.swing.JLabel txt_panggilan;
    // End of variables declaration//GEN-END:variables
}
