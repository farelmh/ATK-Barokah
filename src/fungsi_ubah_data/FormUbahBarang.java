package fungsi_ubah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import admin.dataBarang;
import barokah_atk.konek;
import fungsi_lain.formatUang;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class FormUbahBarang extends FormUbah {

    private dataBarang barang;
    private boolean hapus;
    private String hargaSatuan;
    private String idSatuan;
    private JComboBox<String> comboSatuan;
    konek k = new konek();

    public FormUbahBarang(Frame parent, dataBarang barang) {
        super(parent, "Ubah Data Barang", "ID Barang", "Nama Barang", "Kode Barcode", "Harga Beli /Pcs", "Harga Jual /Pcs", "Harga Kulakan");
        this.barang = barang;
        k.connect();
        fieldMap.get("ID Barang").setEnabled(false);
        fieldMap.get("Harga Beli /Pcs").setEnabled(false);
        isiSatuan();
        isiDataBarang(); //️ langsung isi field berdasarkan data yang dikirim   

        JTextField hargaTotalField = fieldMap.get("Harga Kulakan");
        hargaTotalField.addActionListener((e) -> {
            if (comboSatuan.getSelectedItem() != null) {
                double hargaTotal = Double.parseDouble(hargaTotalField.getText());
                konversiHarga(hargaTotal);
                fieldMap.get("Harga Beli /Pcs").setText(hargaSatuan);
            }
        });
    }

    public boolean hapusData(boolean hapus) {
        hapus = this.hapus;
        return hapus;
    }

    private void isiDataBarang() {
        double hjual = Double.parseDouble(barang.getHjual());
        int hjualformat = (int) hjual;
        
        String hbeli = barang.getHbeli();
        double hbeliformat = Double.parseDouble(hbeli);
        String format = formatUang.formatRp(hbeliformat);
        
        setFieldValue("ID Barang", barang.getId());
        setFieldValue("Nama Barang", barang.getNama());
        setFieldValue("Harga Beli /Pcs", "Rp " + format);
        setFieldValue("Harga Jual /Pcs", String.valueOf(hjualformat));
        setFieldValue("Kode Barcode", getBarcode());
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

    private String getBarcode() {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT id_barcode FROM barang WHERE id_barang = ?");
            stat.setString(1, barang.getId());
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String barcode = rs.getString("id_barcode");
                return barcode;
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

    public String getIdSatuan() {
        return idSatuan;
    }
    
    
    @Override
    protected void tambahKomponenTambahan(GridBagConstraints gbc) {
        comboSatuan = new JComboBox<>();

        JLabel satuan = new JLabel("Satuan Kulakan:");
        satuan.setFont(new Font("Segoe UI", Font.BOLD, 14));
        satuan.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 6;
        dialog.add(satuan, gbc);

        comboSatuan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboSatuan.setPreferredSize(new Dimension(190, 30));
        gbc.gridx = 1;
        gbc.gridy = 6;
        dialog.add(comboSatuan, gbc);
        isiSatuan();
        comboSatuan.addActionListener((e) -> {
            String totalStr = getFieldValue("Harga Kulakan");
            if (!totalStr.isEmpty()) {
                try {
                    double hargaTotal = Double.parseDouble(totalStr);
                    konversiHarga(hargaTotal);
                    fieldMap.get("Harga Beli /Pcs").setText(hargaSatuan); // Set otomatis di field
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(dialog, "Harga total tidak valid!");
                }
            }
        });
    }

    @Override
    protected boolean validateInput() {
        if (fieldMap.get("Harga Kulakan").getText().isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        }
        
        double hargaTotal = Double.parseDouble(fieldMap.get("Harga Kulakan").getText());
        konversiHarga(hargaTotal);
        fieldMap.get("Harga Beli /Pcs").setText(hargaSatuan);

        String nama = getFieldValue("Nama Barang");
        String hbeli = getFieldValue("Harga Beli /Pcs");
        String hjual = getFieldValue("Harga Jual /Pcs");
        String idBarang = getFieldValue("ID Barang");
        double hbeli1 = formatUang.setDefault(hbeli);
        String hargaBeliFormat = String.valueOf(hbeli1);
        

        if (nama.isEmpty() || hbeli.isEmpty() || hjual.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        }

        if (!hargaBeliFormat.matches("\\d+(\\.\\d+)?") || !hjual.matches("\\d+")) {
            setPesan("Harga harus berupa angka!");
            return false;
        }
        if (isDuplicate("nama_barang", nama, idBarang)) {
            setPesan("Nama barang sudah terdaftar!");
            return false;
        }
        return true;
    }

    @Override
    protected void onDelete() {
        this.hapus = true;
    }

    @Override
    protected boolean isDuplicate(String column, String value, String id) {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM barang WHERE " + column + " = ? AND id_barang != ?");
            stat.setString(1, value);
            stat.setString(2, id);
            ResultSet rs = stat.executeQuery();
            return rs.next(); // Jika ada hasil, berarti duplikat
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Kesalahan database: " + e.getMessage());
            return false;
        }
    }
}
