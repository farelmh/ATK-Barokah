package admin;

import barokah_atk.Login;
import barokah_atk.konek;
import fungsi_lain.CariData;
import fungsi_lain.formatTanggal;
import fungsi_lain.formatUang;
import fungsi_lain.modelTabel;
import fungsi_lain.session;
import fungsi_tambah_data.FormTambahOperasional;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
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

public class biayaOperasional extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private DefaultTableModel model = null;
    private final int[] index = {2, 3};
    private final int[] indexUang = {4};
    private final int[] indexTanggal = {1};
    private final int[] indexAngka = {0};
    private String tanggal, keterangan, jumlah;

    konek k = new konek();

    public biayaOperasional() {
        initComponents();
        setTanggal();
        this.setLocationRelativeTo(null);
        k.connect();
        tabelOperasional();
        
    }

    public biayaOperasional(String tanggal, String keterangan, String jumlah) {
        this.tanggal = tanggal;
        this.keterangan = keterangan;
        this.jumlah = jumlah;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
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

    //tabel
    public void tabelOperasional() {
        model = new DefaultTableModel();
        model.addColumn("ID Operasional");
        model.addColumn("Tanggal");
        model.addColumn("Nama Pencatat");
        model.addColumn("Keterangan");
        model.addColumn("Total");
        tbl_operasional.setModel(model);
        modelTabel.setModel(tbl_operasional);
        CariData.TableSorter(tbl_operasional, txt_cari, index, indexUang, indexAngka, indexTanggal);

        try {
            this.stat = k.getCon().prepareStatement("SELECT b.id_operasional, date_format(b.tanggal, '%d-%m-%Y') AS tanggal, b.keterangan, b.jumlah, k.nama_karyawan\n"
                    + "FROM biaya_operasional b\n"
                    + "JOIN karyawan k on b.id_karyawan = k.id_karyawan");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                String jumlah = formatUang.formatRp(rs.getDouble("jumlah"));

                Object[] data = {
                    rs.getString("id_operasional"),
                    rs.getString("tanggal"),
                    rs.getString("nama_karyawan"),
                    rs.getString("keterangan"),
                    "Rp  " + jumlah

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
            this.stat = k.getCon().prepareStatement("SELECT b.id_operasional, date_format(b.tanggal, '%d-%m-%Y') AS tanggal, b.keterangan, b.jumlah, k.nama_karyawan\n"
                    + "FROM biaya_operasional b\n"
                    + "JOIN karyawan k on b.id_karyawan = k.id_karyawan\n"
                    + "WHERE tanggal BETWEEN ? AND ?");
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();

            double totalBiaya = 0;
            while (rs.next()) {
                double jumlah = rs.getDouble("jumlah");
                String jumlahFormat = formatUang.formatRp(jumlah);
                totalBiaya += jumlah;

                Object[] data = {
                    rs.getString("id_operasional"),
                    rs.getString("tanggal"),
                    rs.getString("nama_karyawan"),
                    rs.getString("keterangan"),
                    "Rp  " + jumlahFormat

                };
                model.addRow(data);
                String laba1 = formatUang.formatRp(totalBiaya);
                total_operasional.setText("Rp  " + laba1);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());

        }
    }

    private void resetPencarian() {
        tgl_mulai.setDate(null);
        tgl_akhir.setDate(null);
        tabelOperasional();
        txt_cari.setText("");
        CariData.resetTableSorting(tbl_operasional);
    }

    private void tambahData() {
        FormTambahOperasional form = new FormTambahOperasional(this);
        biayaOperasional b = new biayaOperasional();
        if (form.showDialog()) {
            String tglFormat = formatTanggal.formatTgl(form.getFieldValue("Tanggal"));
            b.setTanggal(tglFormat);
            b.setKeterangan(form.getFieldValue("Keterangan"));
            b.setJumlah(form.getFieldValue("Total Biaya"));
            tambahDB(b);
        }
    }

    private void tambahDB(biayaOperasional b) {
        try {
            this.stat = k.getCon().prepareStatement("insert into biaya_operasional (tanggal, keterangan, jumlah, id_karyawan) values(?, ?, ?, ?)");
            stat.setString(1, b.getTanggal());
            stat.setString(2, b.getKeterangan());
            stat.setString(3, b.getJumlah());
            stat.setString(4, session.getInstance().getId());
            stat.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data berhadil ditambahkan");
            tabelOperasional();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
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

        fc.setDefaultFileName("Laporan Biaya Operasional.xlsx");

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

            Sheet sheet1 = workbook.createSheet("Laporan Biaya Operasional");
            createTitle(sheet1, "Laporan Biaya Operasional", titleStyle, 0, 4);
            String[] header1 = {"ID Operasional", "Tanggal", "Pencatat", "Keterangan", "Total Biaya"};
            createHeader(sheet1, header1, headerStyle, 3);

            int rowIndex = 4;
            this.stat = k.getCon().prepareStatement("SELECT b.id_operasional, date_format(b.tanggal, '%d-%m-%Y') AS tanggal, b.keterangan, b.jumlah, k.nama_karyawan\n"
                    + "FROM biaya_operasional b\n"
                    + "JOIN karyawan k on b.id_karyawan = k.id_karyawan\n"
                    + "WHERE tanggal BETWEEN ? AND ?");
            stat.setString(1, getTanggalMulai());
            stat.setString(2, getTanggalAkhir());
            this.rs = this.stat.executeQuery();

            while (rs.next()) {
                Row row = sheet1.createRow(rowIndex++);
                createCell(row, 0, rs.getString("id_operasional"), cellStyle);
                createCell(row, 1, rs.getString("tanggal"), cellStyle);
                createCell(row, 2, rs.getString("nama_karyawan"), cellStyle);
                createCell(row, 3, rs.getString("keterangan"), cellStyle);
                createCell(row, 4, rs.getDouble("jumlah"), rupiahStyle);
            }
            Row totalRow = sheet1.createRow(rowIndex);
            Cell cellLabel = totalRow.createCell(3); // kolom "Kasir"
            cellLabel.setCellValue("Total Biaya Operasional :");
            cellLabel.setCellStyle(headerStyle); // atau style tebal

            Cell cellTotal = totalRow.createCell(4); // kolom "Total"
            String kolomTotal = "E"; // misal kolom D untuk total

            String opr = total_operasional.getText();
            opr = opr.replaceAll("[^\\d,]", "");
            cellTotal.setCellFormula(String.format(opr, kolomTotal, kolomTotal, rowIndex));
            cellTotal.setCellStyle(rupiahStyle);
            
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
        btn_dashboard = new javax.swing.JButton();
        btn_laporan = new javax.swing.JButton();
        btn_barang = new javax.swing.JButton();
        btn_karyawan = new javax.swing.JButton();
        btn_supplier = new javax.swing.JButton();
        user = new javax.swing.JLabel();
        txt_panggilan = new javax.swing.JLabel();
        admin = new javax.swing.JLabel();
        btn_laporan1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        btn_logout = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        btn_kembali = new javax.swing.JLabel();
        total_operasional = new javax.swing.JTextField();
        total = new javax.swing.JLabel();
        judul = new javax.swing.JLabel();
        btn_ekspor = new javax.swing.JLabel();
        filterPanel = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        tgl_mulai = new com.toedter.calendar.JDateChooser();
        jLabel3 = new javax.swing.JLabel();
        tgl_akhir = new com.toedter.calendar.JDateChooser();
        btn_cari = new javax.swing.JButton();
        txt_cari = new javax.swing.JTextField();
        btn_cari3 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tbl_operasional = new javax.swing.JTable();
        btn_tambah = new javax.swing.JLabel();
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
        btn_dashboard.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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
        btn_laporan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_laporan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_laporanMouseClicked(evt);
            }
        });

        btn_barang.setBackground(new java.awt.Color(63, 114, 175));
        btn_barang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/barang.png"))); // NOI18N
        btn_barang.setBorder(null);
        btn_barang.setBorderPainted(false);
        btn_barang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_barang.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_barangMouseClicked(evt);
            }
        });

        btn_karyawan.setBackground(new java.awt.Color(63, 114, 175));
        btn_karyawan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/karyawan.png"))); // NOI18N
        btn_karyawan.setBorder(null);
        btn_karyawan.setBorderPainted(false);
        btn_karyawan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_karyawan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_karyawanMouseClicked(evt);
            }
        });

        btn_supplier.setBackground(new java.awt.Color(63, 114, 175));
        btn_supplier.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/supplier.png"))); // NOI18N
        btn_supplier.setBorder(null);
        btn_supplier.setBorderPainted(false);
        btn_supplier.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
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

        btn_laporan1.setBackground(new java.awt.Color(63, 114, 175));
        btn_laporan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/transaksi.png"))); // NOI18N
        btn_laporan1.setBorder(null);
        btn_laporan1.setBorderPainted(false);
        btn_laporan1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_laporan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_laporan1MouseClicked(evt);
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
                            .addComponent(btn_laporan1)))
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
                .addComponent(btn_laporan1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_laporan)
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

        btn_kembali.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/kembaliNew.png"))); // NOI18N
        btn_kembali.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_kembali.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_kembaliMouseClicked(evt);
            }
        });

        total_operasional.setEditable(false);

        total.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        total.setForeground(new java.awt.Color(51, 51, 51));
        total.setText("Total Biaya Operasional :");

        judul.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        judul.setForeground(new java.awt.Color(0, 0, 0));
        judul.setText("Biaya Operasional");

        btn_ekspor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/exporexcelNew.png"))); // NOI18N
        btn_ekspor.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_ekspor.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_eksporMouseClicked(evt);
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

        btn_cari3.setText("reset");
        btn_cari3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_cari3ActionPerformed(evt);
            }
        });

        tbl_operasional.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(tbl_operasional);

        javax.swing.GroupLayout filterPanelLayout = new javax.swing.GroupLayout(filterPanel);
        filterPanel.setLayout(filterPanelLayout);
        filterPanelLayout.setHorizontalGroup(
            filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(filterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPanelLayout.createSequentialGroup()
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 277, Short.MAX_VALUE)
                        .addComponent(txt_cari, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1))
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
                        .addComponent(btn_cari3)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 334, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        btn_tambah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/tambah.png"))); // NOI18N
        btn_tambah.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_tambah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_tambahMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(btn_kembali, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btn_ekspor))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(total)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(total_operasional, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                                .addComponent(judul)
                                .addGap(278, 278, 278)
                                .addComponent(btn_tambah, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(judul)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(btn_tambah)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addComponent(filterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(total_operasional, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(total))
                .addGap(30, 30, 30)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_kembali)
                    .addComponent(btn_ekspor))
                .addContainerGap())
        );

        logo_pensil.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/stationery 1.png"))); // NOI18N

        toko_barokah.setFont(new java.awt.Font("Segoe UI", 1, 36)); // NOI18N
        toko_barokah.setForeground(new java.awt.Color(255, 255, 255));
        toko_barokah.setText("TOKO BAROKAH ATK");

        alamat_toko.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        alamat_toko.setForeground(new java.awt.Color(204, 204, 204));
        alamat_toko.setText("Jl. Raya Besuk, Desa Alaskandang, Kec. Besuk, Kab. Probolinggo");

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
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 424, Short.MAX_VALUE)
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

    private void btn_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cariActionPerformed
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
    }//GEN-LAST:event_btn_cariActionPerformed

    private void txt_cariMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_cariMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariMouseClicked

    private void txt_cariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_cariActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_cariActionPerformed

    private void btn_cari3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_cari3ActionPerformed
        // TODO add your handling code here:
        resetPencarian();
    }//GEN-LAST:event_btn_cari3ActionPerformed

    private void btn_tambahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_tambahMouseClicked
        // TODO add your handling code here:
        tambahData();
    }//GEN-LAST:event_btn_tambahMouseClicked

    private void btn_laporan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_laporan1MouseClicked
        // TODO add your handling code here:
        Transaksi p = new Transaksi();
        p.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_btn_laporan1MouseClicked

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
            java.util.logging.Logger.getLogger(biayaOperasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(biayaOperasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(biayaOperasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(biayaOperasional.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new biayaOperasional().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel admin;
    private javax.swing.JLabel alamat_toko;
    private javax.swing.JButton btn_barang;
    private javax.swing.JButton btn_cari;
    private javax.swing.JButton btn_cari3;
    private javax.swing.JButton btn_dashboard;
    private javax.swing.JLabel btn_ekspor;
    private javax.swing.JButton btn_karyawan;
    private javax.swing.JLabel btn_kembali;
    private javax.swing.JButton btn_laporan;
    private javax.swing.JButton btn_laporan1;
    private javax.swing.JButton btn_logout;
    private javax.swing.JButton btn_supplier;
    private javax.swing.JLabel btn_tambah;
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
    private javax.swing.JTable tbl_operasional;
    private com.toedter.calendar.JDateChooser tgl_akhir;
    private com.toedter.calendar.JDateChooser tgl_mulai;
    private javax.swing.JLabel toko_barokah;
    private javax.swing.JLabel total;
    private javax.swing.JTextField total_operasional;
    private javax.swing.JTextField txt_cari;
    private javax.swing.JLabel txt_panggilan;
    private javax.swing.JLabel user;
    // End of variables declaration//GEN-END:variables
}
