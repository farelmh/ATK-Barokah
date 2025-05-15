package fungsi_ubah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import admin.dataBarang;
import barokah_atk.konek;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FormUbahBarang extends FormUbah {

    private dataBarang barang;
    private boolean hapus;
    konek k = new konek();

    public FormUbahBarang(Frame parent, dataBarang barang) {
        super(parent, "Ubah Data Barang", "ID Barang", "Nama Barang", "Harga Beli /Pcs", "Harga Jual /Pcs", "Kode Barcode");
        this.barang = barang;
        k.connect();
        isiDataBarang(); //️ langsung isi field berdasarkan data yang dikirim   
    }

    public boolean hapusData(boolean hapus) {
        hapus = this.hapus;
        return hapus;
    }

    private void isiDataBarang() {
        fieldMap.get("ID Barang").setEnabled(false);
        setFieldValue("ID Barang", barang.getId());
        setFieldValue("Nama Barang", barang.getNama());
        setFieldValue("Harga Beli /Pcs", barang.getHbeli());
        setFieldValue("Harga Jual /Pcs", barang.getHjual());
        setFieldValue("Kode Barcode", getBarcode());
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

    @Override
    protected boolean validateInput() {
        // Validasi bisa kamu sesuaikan lagi
        String nama = getFieldValue("Nama Barang");
        String hbeli = getFieldValue("Harga Beli /Pcs");
        String hjual = getFieldValue("Harga Jual /Pcs");
        String idBarang = getFieldValue("ID Barang");

        if (nama.isEmpty() || hbeli.isEmpty() || hjual.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        }

        if (!hbeli.matches("\\d+(\\.\\d+)?") || !hjual.matches("\\d+(\\.\\d+)?")) {
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
