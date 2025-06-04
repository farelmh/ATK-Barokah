package fungsi_ubah_data;

import admin.dataSupplier;
import barokah_atk.konek;
import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class FormUbahSupplier extends FormUbah {

    private dataSupplier supplier;
    private boolean hapus;
    konek k = new konek();

    public FormUbahSupplier(Frame parent, dataSupplier supplier) {
        super(parent, "Ubah data supplier", "ID Supplier", "Nama Supplier", "No Telp", "Alamat");
        this.supplier = supplier;
        k.connect();
        isiData();
    }

    public boolean hapusData(boolean hapus) {
        hapus = this.hapus;
        return hapus;
    }

    private void isiData() {
        fieldMap.get("ID Supplier").setEnabled(false);
        setFieldValue("ID Supplier", supplier.getId());
        setFieldValue("Nama Supplier", supplier.getNama());
        setFieldValue("No Telp", supplier.getNotelp());
        setFieldValue("Alamat", supplier.getAlamat());
    }

    @Override
    protected boolean validateInput() {
        String id = getFieldValue("ID Supplier");
        String nama = getFieldValue("Nama Supplier");
        String notelp = getFieldValue("No Telp");
        String alamat = getFieldValue("Alamat");

        if (id.isEmpty() || nama.isEmpty() || notelp.isEmpty() || alamat.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        } else if (id.length() != 5) {
            setPesan("id supplier harus 5 karakter");
            return false;
        } else if (nama.length() < 4) {
            setPesan("nama setidaknya terdiri dari 4 huruf / karakter");
            return false;
        } else if (!notelp.matches("\\d+")) {
            setPesan("nomor telepon harus berupa angka");
            return false;
        } else if (notelp.length() < 12 || notelp.length() > 15) {
            setPesan("nomor telepon harus berada diantara 12 - 15 angka / karakter");
            return false;
        } else if (alamat.length() < 10) {
            setPesan("alamat setidaknya l0 karakter");
            return false;
        }

        if (isDuplicate("id_supplier", id, id)) {
            setPesan("id sudah terdaftar! gunakan id lain");
            return false;
        } else if (isDuplicate("nama_supplier", nama, id)) {
            setPesan("nama sudah terdaftar!");
            return false;
        } else if (isDuplicate("no_telp", notelp, id)) {
            setPesan("nomor telepon sudah terdaftar! gunakan nomor lain");
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
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM supplier WHERE " + column + " = ? AND id_supplier != ?");
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
