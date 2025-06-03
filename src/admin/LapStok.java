package admin;

import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatTanggal;
import fungsi_lain.modelTabel;
import fungsi_lain.session;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.table.DefaultTableModel;
import javax.swing.JOptionPane;
import jnafilechooser.api.JnaFileChooser;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class LapStok extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private final int[] index = {0, 5};
    private final int[] indexAngka = {2, 3, 4};
    private final int[] indexTanggal = {1};

    konek k = new konek();

    public LapStok() {
        initComponents();
        this.setLocationRelativeTo(null);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Filter Pencarian"));
        k.connect();
        setTanggal();
        tabelLapStok();
        txt_panggilan.setText(session.getInstance().getNama());
    }

    // set tanggal ke bahasa indo
    private void setTanggal() {
        formatTanggal.setTanggalIndo(tgl_mulai);
        formatTanggal.setTanggalIndo(tgl_akhir);
    }

    //ambil data tanggal mulai
    private String getTanggalMulai() {
        try {
            Date tgl = tgl_mulai.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    //ambil data tanggal akhir
    private String getTanggalAkhir() {
        try {
            Date tgl = tgl_akhir.getDate();
            return formatTanggal.formatTgl(tgl);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    public void tabelLapStok() {
        model = new DefaultTableModel();
        model.addColumn("Tanggal");
        model.addColumn("Nama Barang");
        model.addColumn("Masuk");
        model.addColumn("Keluar");
        model.addColumn("Sisa");
        model.addColumn("Keterangan");
        tbl_stok.setModel(model);
        modelTabel.setModel(tbl_stok);
        CariData.TableSorter(tbl_stok, txt_cari, index, null, indexAngka, indexTanggal);

        try {
            this.stat = k.getCon().prepareStatement("SELECT * FROM v_lap_stok");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    // rs.getString("id_kartu"),
                    rs.getString("tgl_update"),
                    rs.getString("nama_barang"),
                    rs.getString("jml_masuk"),
                    rs.getString("jml_keluar"),
                    rs.getString("sisa"),
                    rs.getString("keterangan")};
                model.addRow(data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void resetPencarian() {
        tgl_mulai.setDate(null);
        tgl_akhir.setDate(null);
        tabelLapStok();
        txt_cari.setText("");
        CariData.resetTableSorting(tbl_stok);
    }

    // model excel
    private XSSFCellStyle createStyle(Workbook wb, boolean bold, int fontSize, IndexedColors bgColor) {
        XSSFFont font = ((XSSFWorkbook) wb).createFont();
        font.setBold(bold);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) fontSize);

        XSSFCellStyle style = ((XSSFWorkbook) wb).createCellStyle();
        style.setFont(font);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        if (bgColor != null) {
            style.setFillForegroundColor(bgColor.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return style;
    }

    private CellStyle createCurrencyStyle(Workbook wb) {
        CellStyle style = createStyle(wb, false, 11, null);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("\"Rp\" #,##0"));
        return style;
    }

    private void createTitle(Sheet sheet, String title, CellStyle style, int rowIdx, int colSpan) {
        Row row = sheet.createRow(rowIdx);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);

        style.setAlignment(HorizontalAlignment.CENTER);              // <-- Tambahkan ini
        style.setVerticalAlignment(VerticalAlignment.CENTER);        // <-- Opsional
        cell.setCellStyle(style);

        sheet.addMergedRegion(new CellRangeAddress(rowIdx, rowIdx, 0, colSpan));
    }

    private void createHeader(Sheet sheet, String[] headers, CellStyle style, int rowIdx) {
        Row row = sheet.createRow(rowIdx);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void createCell(Row row, int colIdx, String value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int colIdx, double value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createCell(Row row, int colIdx, int value, CellStyle style) {
        Cell cell = row.createCell(colIdx);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet, int numCols) {
        for (int i = 0; i < numCols; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private File getUniqueFile(File file) {
        String name = file.getName();
        String parent = file.getParent();
        String baseName;
        String extension;

        int dotIndex = name.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = name.substring(0, dotIndex);
            extension = name.substring(dotIndex);
        } else {
            baseName = name;
            extension = "";
        }

        File uniqueFile = new File(parent, baseName + extension);
        int count = 1;
        while (uniqueFile.exists()) {
            uniqueFile = new File(parent, baseName + "(" + count + ")" + extension);
            count++;
        }

        return uniqueFile;
    }

    // fungsi ekspor ke excel
    private void exportExcel() {
        JnaFileChooser fc = new JnaFileChooser();
        fc.setTitle("Simpan File");
        fc.setMode(JnaFileChooser.Mode.Files);
        fc.addFilter("Excel Files", "xlsx");

        fc.setDefaultFileName("Laporan Stok.xlsx");

        if (!fc.showSaveDialog(this)) {
            return;
        }

        File fileToSave = fc.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".xlsx")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
        }
        fileToSave = getUniqueFile(fileToSave);  // <-- Cek dan rename otomatis
        String filePath = fileToSave.getAbsolutePath();

        try (Workbook workbook = new XSSFWorkbook()) {
            XSSFCellStyle titleStyle = createStyle(workbook, true, 24, IndexedColors.WHITE);
            XSSFCellStyle headerStyle = createStyle(workbook, true, 12, IndexedColors.LIGHT_GREEN);
            XSSFCellStyle cellStyle = createStyle(workbook, false, 11, null);
            CellStyle rupiahStyle = createCurrencyStyle(workbook);

            Sheet sheet1 = workbook.createSheet("Laporan Stok");
            createTitle(sheet1, "Laporan Stok", titleStyle, 0, 5);
            String[] header1 = {"Tanggal", "Nama Barang", "Masuk", "Keluar", "Sisa", "Keterangan"};
            createHeader(sheet1, header1, headerStyle, 3);

            int rowIndex = 4;
            this.stat = k.getCon().prepareStatement("Select barang.nama_barang, kartu_stok.tgl_update, kartu_stok.jml_masuk, kartu_stok.jml_keluar, kartu_stok.sisa, kartu_stok.keterangan\n"
                    + "from kartu_stok\n"
                    + "JOIN barang ON barang.id_barang = kartu_stok.id_barang where kartu_stok.tgl_update between ? and ? "
                    + "order by kartu_stok.tgl_update asc");
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Row row = sheet1.createRow(rowIndex++);
                createCell(row, 0, rs.getString("tgl_update"), cellStyle);
                createCell(row, 1, rs.getString("nama_barang"), cellStyle);
                createCell(row, 2, rs.getString("jml_masuk"), cellStyle);
                createCell(row, 3, rs.getString("jml_keluar"), cellStyle);
                createCell(row, 4, rs.getString("sisa"), cellStyle);
                createCell(row, 5, rs.getString("keterangan"), cellStyle);
            }

            autoSizeColumns(sheet1, header1.length);

            // === Simpan file Excel
            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
                JOptionPane.showMessageDialog(null, "Data berhasil disimpan!");
            }
            workbook.close();

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
        dashboard = new javax.swing.JButton();
        laporan = new javax.swing.JButton();
        barang = new javax.swing.JButton();
        karyawan = new javax.swing.JButton();
        supplier = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        btn_transaksi = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_stok = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        tgl_mulai = new com.toedter.calendar.JDateChooser();
        jLabel9 = new javax.swing.JLabel();
        tgl_akhir = new com.toedter.calendar.JDateChooser();
        btn_cari1 = new javax.swing.JButton();
        txt_cari = new javax.swing.JTextField();
        btn_cari4 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
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

        barang.setBackground(new java.awt.Color(63, 114, 175));
        barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        barang.setBorder(null);
        barang.setBorderPainted(false);
        barang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                barangMouseClicked(evt);
            }
        });

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
                            .addComponent(dashboard)
                            .addComponent(barang)
                            .addComponent(karyawan)
                            .addComponent(supplier)
                            .addComponent(btn_transaksi)
                            .addComponent(laporan)))
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
                .addComponent(dashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(barang)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(karyawan)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(supplier)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_transaksi)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(laporan)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(63, 114, 175));

        jButton6.setBackground(new java.awt.Color(63, 114, 175));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/logout2.png"))); // NOI18N
        jButton6.setBorder(null);
        jButton6.setBorderPainted(false);
        jButton6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButton6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButton6MouseClicked(evt);
            }
        });

        jPanel4.setBackground(new java.awt.Color(218, 212, 181));

        tbl_stok.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbl_stok);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembaliNew.png"))); // NOI18N
        jLabel2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel2MouseClicked(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(0, 0, 0));
        jLabel6.setText("Laporan Stok");

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/exporexcelNew.png"))); // NOI18N
        jLabel8.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel8.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel8MouseClicked(evt);
            }
        });

        filterPanel.setBackground(new java.awt.Color(218, 212, 181));

        jLabel7.setForeground(new java.awt.Color(0, 0, 0));
        jLabel7.setText("Dari");

        tgl_mulai.setBackground(new java.awt.Color(218, 212, 181));

        jLabel9.setForeground(new java.awt.Color(0, 0, 0));
        jLabel9.setText("Sampai");

        tgl_akhir.setBackground(new java.awt.Color(218, 212, 181));

        btn_cari1.setText("Cari");
        btn_cari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari1ActionPerformed(evt);
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

        btn_cari4.setText("Reset");
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
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_mulai, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tgl_akhir, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_cari1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_cari4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
                .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        filterPanelLayout.setVerticalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_mulai, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(tgl_akhir, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_cari1)
                        .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btn_cari4)))
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
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8))
                    .addComponent(jScrollPane1)
                    .addComponent(filterPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 81, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabel8))
                .addContainerGap())
        );

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("TOKO BAROKAH ATK");

        jLabel14.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(204, 204, 204));
        jLabel14.setText("Jl. Raya Besuk, Desa Alaskandang, Kec. Besuk, Kab. Probolinggo");

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
                        .addComponent(jLabel1)
                        .addGap(30, 30, 30)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 424, Short.MAX_VALUE)
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
                        .addGap(87, 87, 87))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel14))
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

    private void dashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dashboardActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dashboardActionPerformed

    private void jButton6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton6MouseClicked

    }//GEN-LAST:event_jButton6MouseClicked

    private void dashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dashboardMouseClicked
        adm_dashboard r = new adm_dashboard();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_dashboardMouseClicked

    private void barangMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_barangMouseClicked
        DataBarang r = new DataBarang();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_barangMouseClicked

    private void karyawanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_karyawanMouseClicked
        DataKaryawan r = new DataKaryawan();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_karyawanMouseClicked
    private void cari() {
        model.setRowCount(0);
        try {
            this.stat = k.getCon().prepareStatement("Select barang.nama_barang, kartu_stok.tgl_update, kartu_stok.jml_masuk, kartu_stok.jml_keluar, kartu_stok.sisa, kartu_stok.keterangan\n"
                    + "from kartu_stok\n"
                    + "JOIN barang ON barang.id_barang = kartu_stok.id_barang where kartu_stok.tgl_update between ? and ? "
                    + "order by kartu_stok.tgl_update asc");
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Object[] data = {
                    //  rs.getString("id_kartu"),
                    rs.getString("tgl_update"),
                    rs.getString("nama_barang"),
                    rs.getString("jml_masuk"),
                    rs.getString("jml_keluar"),
                    rs.getString("sisa"),
                    rs.getString("keterangan")

                };
                model.addRow(data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());

        }
    }
    private void jLabel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel2MouseClicked
        Laporan p = new Laporan();
        p.setVisible(true);
        this.setVisible(false);
    }//GEN-LAST:event_jLabel2MouseClicked

    private void txt_panggilanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_panggilanMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_txt_panggilanMouseClicked

    private void jLabel8MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel8MouseClicked
        // TODO add your handling code here:
        // TODO add your handling code here:
        Date start = tgl_mulai.getDate();
        Date end = tgl_akhir.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(null, "tanggal tidak boleh kosong");
        } else if (start.after(end)) {
            JOptionPane.showMessageDialog(null, "tanggal awal tidak boleh lebih dari tanggal akhir");
        } else {
            cari();
            exportExcel();
        }
    }//GEN-LAST:event_jLabel8MouseClicked

    private void btn_transaksiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_transaksiMouseClicked
        // TODO add your handling code here:
        Transaksi a = new Transaksi();
        a.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_transaksiMouseClicked

    private void btn_cari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari1ActionPerformed
        // TODO add your handling code here:
        Date start = tgl_mulai.getDate();
        Date end = tgl_akhir.getDate();

        if (start == null || end == null) {
            JOptionPane.showMessageDialog(null, "tanggal tidak boleh kosong");
        } else if (start.after(end)) {
            JOptionPane.showMessageDialog(null, "tanggal awal tidak boleh lebih dari tanggal akhir");
        } else {
            cari();
        }
    }//GEN-LAST:event_btn_cari1ActionPerformed

    private void txt_cariMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_cariMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariMouseClicked

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void btn_cari4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari4ActionPerformed
        // TODO add your handling code here:
        resetPencarian();
    }//GEN-LAST:event_btn_cari4ActionPerformed

    private void laporanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_laporanMouseClicked
        // TODO add your handling code here:
        Laporan r = new Laporan();
        r.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_laporanMouseClicked

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
            java.util.logging.Logger.getLogger(LapStok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LapStok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LapStok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LapStok.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LapStok().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton barang;
    private javax.swing.JButton btn_cari1;
    private javax.swing.JButton btn_cari4;
    private javax.swing.JButton btn_transaksi;
    private javax.swing.JButton dashboard;
    private javax.swing.JPanel filterPanel;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton karyawan;
    private javax.swing.JButton laporan;
    private javax.swing.JButton supplier;
    private javax.swing.JTable tbl_stok;
    private com.toedter.calendar.JDateChooser tgl_akhir;
    private com.toedter.calendar.JDateChooser tgl_mulai;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JLabel txt_panggilan;
    // End of variables declaration//GEN-END:variables
}
