package fungsi_tambah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import barokah_atk.konek;
import fungsi_lain.formatUang;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class FormTambahBarang extends FormTambah {

    konek k = new konek();
    private JComboBox<String> comboKategori;
    private JComboBox<String> comboSatuan;
    private String hargaSatuan;
    private String idSatuan;
    private JTextField fieldHargaBeliOtomatis;
    private JTextField hjual;

    public FormTambahBarang(Frame parent) {
        super(parent, "Tambah Data Barang", "ID Barang", "Nama Barang", "Harga Kulakan");
        k.connect();
        fieldMap.get("ID Barang").setEnabled(false);
        isiId();
        isiSatuan();

        JTextField hargaTotalField = fieldMap.get("Harga Kulakan");
        hargaTotalField.addActionListener((e) -> {
            if (comboSatuan.getSelectedItem() != null) {
                double hargaTotal = Double.parseDouble(hargaTotalField.getText());
                konversiHarga(hargaTotal);
                fieldHargaBeliOtomatis.setText(hargaSatuan);
            }
        });
    }

    private void isiKategori() {
        try {
            konek k = new konek();
            k.connect();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT nama_kategori from kategori order by nama_kategori ASC");
            ResultSet rs = stat.executeQuery();
            comboKategori.removeAllItems();
            while (rs.next()) {
                comboKategori.addItem(rs.getString("nama_kategori"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void isiSatuan() {
        try {
            konek k = new konek();
            k.connect();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT nama_satuan from satuan_beli order by nama_satuan ASC");
            ResultSet rs = stat.executeQuery();
            comboSatuan.removeAllItems();
            while (rs.next()) {
                comboSatuan.addItem(rs.getString("nama_satuan"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void isiId() {
        try {
            konek k = new konek();
            k.connect();
            String nama = comboKategori.getSelectedItem().toString();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT kode_kategori from kategori where nama_kategori = ?");
            stat.setString(1, nama);
            ResultSet rs = stat.executeQuery();

            if (rs.next()) {
                String id = rs.getString("kode_kategori");
                makeId(id);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    private void makeId(String id) {
        try {
            konek k = new konek();
            k.connect();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT MAX(id_barang) as id from barang\n"
                    + "where id_barang like ?");
            stat.setString(1, id + "%");
            ResultSet rs = stat.executeQuery();
            String newId;
            if (rs.next()) {
                String lastId = rs.getString("id");
                newId = (lastId != null)
                        ? id + String.format("%03d", Integer.parseInt(lastId.substring(3)) + 1)
                        : id + "001";
                fieldMap.get("ID Barang").setText(newId);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public String getIdKategori() {
        try {
            konek k = new konek();
            k.connect();
            String idk = comboKategori.getSelectedItem().toString();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT id_kategori from kategori\n"
                    + "where nama_kategori = ?");
            stat.setString(1, idk);
            ResultSet rs = stat.executeQuery();

            if (rs.next()) {
                String idkategori = rs.getString("id_kategori");
                return idkategori;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private void konversiHarga(double hargaTotal) {
        try {
            konek k = new konek();
            k.connect();
            String nama = comboSatuan.getSelectedItem().toString();
            PreparedStatement stat = k.getCon().prepareStatement("SELECT id_satuan, isi_per_satuan from satuan_beli where nama_satuan = ?");
            stat.setString(1, nama);
            ResultSet rs = stat.executeQuery();

            int isi = 0;
            String idsatuan = "";
            if (rs.next()) {
                idsatuan = rs.getString("id_satuan");
                isi = rs.getInt("isi_per_satuan");
            }
            this.idSatuan = idsatuan;
            double hsatuan = hargaTotal / isi;
            // Pembulatan ke kelipatan 10 terdekat
            double hargaBulat = Math.round(hsatuan / 10.0) * 10;

            String rp = formatUang.formatRp(hargaBulat);
            this.hargaSatuan = "Rp " + rp;

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public String getHargaBeliPcs() {
        double df = formatUang.setDefault(hargaSatuan);
        return String.valueOf(df);
    }

    public String getHargaJual() {
        return hjual.getText();
    }
    
    public String getIdSatuan() {
        return idSatuan;
    }

    @Override
    protected void tambahKomponenTambahan(GridBagConstraints gbc) {
        fieldHargaBeliOtomatis = new JTextField(14);
        hjual = new JTextField(14);

        JLabel satuan = new JLabel("Satuan Kulakan:");
        satuan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        satuan.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 4;
        dialog.add(satuan, gbc);

        comboSatuan = new JComboBox<>();

        comboSatuan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboSatuan.setPreferredSize(new Dimension(190, 30));
        gbc.gridx = 1;
        gbc.gridy = 4;
        dialog.add(comboSatuan, gbc);
        isiSatuan();
        comboSatuan.addActionListener((e) -> {
            String totalStr = getFieldValue("Harga Kulakan");
            if (!totalStr.isEmpty()) {
                try {
                    double hargaTotal = Double.parseDouble(totalStr);
                    konversiHarga(hargaTotal);
                    fieldHargaBeliOtomatis.setText(hargaSatuan); // Set otomatis di field
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Harga total tidak valid!");
                }
            }
        });

        JLabel beli = new JLabel("Harga Beli / Pcs:");
        beli.setFont(new Font("Segoe UI", Font.BOLD, 14));
        beli.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 5;
        dialog.add(beli, gbc);

        fieldHargaBeliOtomatis.setFont(new Font("Segoe UI", Font.BOLD, 13));
        fieldHargaBeliOtomatis.setEnabled(false);
        gbc.gridx = 1;
        gbc.gridy = 5;
        dialog.add(fieldHargaBeliOtomatis, gbc);
        fieldHargaBeliOtomatis.addActionListener((e) -> {
            fieldHargaBeliOtomatis.setText(hargaSatuan);
        });

        JLabel jual = new JLabel("Harga Jual / Pcs:");
        jual.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jual.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 6;
        dialog.add(jual, gbc);

        hjual.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 6;
        dialog.add(hjual, gbc);

        JLabel label = new JLabel("Kategori:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 7;
        dialog.add(label, gbc);

        comboKategori = new JComboBox<>();

        comboKategori.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboKategori.setPreferredSize(new Dimension(190, 30));
        gbc.gridx = 1;
        gbc.gridy = 7;
        dialog.add(comboKategori, gbc);
        isiKategori();
        comboKategori.addActionListener(e -> isiId());
    }

    @Override
    protected boolean validateInput() {
        // hitung ulang harga per pcs
        double hargaTotal = Double.parseDouble(fieldMap.get("Harga Kulakan").getText());
        konversiHarga(hargaTotal);
        fieldHargaBeliOtomatis.setText(hargaSatuan);

        String id = getFieldValue("ID Barang");
        String nama = getFieldValue("Nama Barang");
        String hbeli = getFieldValue("Harga Kulakan");
        String hargaJual = hjual.getText();
        String hargaBeli = fieldHargaBeliOtomatis.getText();
        double hbeli1 = formatUang.setDefault(hargaBeli);
        String hargaBeliFormat = String.valueOf(hbeli1);

        if (id.isEmpty() || nama.isEmpty() || hbeli.isEmpty() || hargaJual.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        } else if (nama.length() < 10) {
            setPesan("Nama Barang minimal 10 karakter!");
            return false;
        } else if (!hargaBeliFormat.matches("\\d+(\\.\\d+)") || !hargaJual.matches("\\d+")) {
            setPesan("Harga jual harus angka!");
            return false;
        }

        // Cek apakah ID atau Nama Barang sudah ada di database
        if (isDuplicate("id_barang", id) || isDuplicate("nama_barang", nama)) {
            setPesan("ID atau Nama Barang sudah terdaftar!");
            return false;
        }

        double jual = Double.parseDouble(hargaJual);
        double beli = Double.parseDouble(hargaBeliFormat);

        if ((jual - beli) < 250) {
            setPesan("Selisih harga minimal Rp 250");
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDuplicate(String column, String value) {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM barang WHERE " + column + " = ?");
            stat.setString(1, value);
            ResultSet rs = stat.executeQuery();
            return rs.next(); // Jika ada hasil, berarti duplikat
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Kesalahan database: " + e.getMessage());
            return false;
        }
    }
}
