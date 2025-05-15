package fungsi_ubah_data;

import admin.dataKaryawan;
import barokah_atk.konek;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class FormUbahKaryawan extends FormUbah {

    private dataKaryawan karyawan;
    private boolean hapus;
    private JComboBox<String> comboJabatan;
    konek k = new konek();

    public FormUbahKaryawan(Frame parent, dataKaryawan karyawan) {
        super(parent, "Ubah data karyawan", "ID Karyawan", "Nama Lengkap", "Nama Panggilan", "No Telp", "Email");
        this.karyawan = karyawan;
        k.connect();
        isiData();
    }

    @Override
    protected void tambahKomponenTambahan(GridBagConstraints gbc) {
        JLabel label = new JLabel("Jabatan:");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridx = 0;
        dialog.add(label, gbc);

        String[] pilihan = {"Admin", "Kasir"};
        comboJabatan = new JComboBox<>(pilihan);
        comboJabatan.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboJabatan.setPreferredSize(new Dimension(180, 30));
        gbc.gridx = 1;
        dialog.add(comboJabatan, gbc);
    }

    private void getJabatan() {
        String jabatan = karyawan.getId_level();
        if (jabatan.equalsIgnoreCase("Admin")) {
            comboJabatan.setSelectedItem("Admin");
        } else {
            comboJabatan.setSelectedItem("Kasir");
        }
    }

    public String getId_level() {
        return comboJabatan.getSelectedItem().toString().toLowerCase();
    }

    private void isiData() {
        getJabatan();
        fieldMap.get("ID Karyawan").setEnabled(false);
        setFieldValue("ID Karyawan", karyawan.getId());
        setFieldValue("Nama Lengkap", karyawan.getNamaPanjang());
        setFieldValue("Nama Panggilan", karyawan.getNamaPendek());
        setFieldValue("No Telp", karyawan.getNo_telp());
        setFieldValue("Email", karyawan.getEmail());
        setFieldValue("Jabatan", comboJabatan.getSelectedItem().toString());
    }

    public boolean hapusData(boolean hapus) {
        hapus = this.hapus;
        return hapus;
    }

    @Override
    protected boolean validateInput() {
        String id = getFieldValue("ID Karyawan");
        String namaPanjang = getFieldValue("Nama Lengkap");
        String namaPendek = getFieldValue("Nama Panggilan");
        String notelp = getFieldValue("No Telp");
        String email = getFieldValue("Email");
        String id_level = comboJabatan.getSelectedItem().toString();

        if (id.isEmpty() || namaPanjang.isEmpty() || namaPendek.isEmpty() || notelp.isEmpty() || email.isEmpty() || id_level.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        }
        if (isDuplicate("nama_karyawan", namaPanjang, id)) {
            setPesan("nama sudah terdaftar");
            return false;
        } else if (namaPanjang.length() < 5) {
            setPesan("Nama Lengkap minimal 5 karakter!");
            return false;
        } else if (namaPendek.length() < 3) {
            setPesan("Nama Panggilan minimal 3 karakter!");
            return false;
        } else if (!notelp.matches("\\d+")) {
            setPesan("Nomor Telepon harus berupa angka");
            return false;
        } else if (notelp.length() < 11 || notelp.length() > 13) {
            setPesan("Nomor Telepon harus diantara 11 - 13 karakter");
            return false;
        } else if (email.length() < 15) {
            setPesan("Email minimal 15 karakter");
            return false;
        } else if (!email.contains("@gmail.com")) {
            setPesan("format email harus @gmail.com");
            return false;
        } else if (isDuplicate("no_telp", notelp, id)) {
            setPesan("nomor telepon sudah terdaftar!\ngunakan nomor lain!");
            return false;
        } else if (isDuplicate("email", email, id)) {
            setPesan("email sudah terdaftar!\ngunakan email lain!");
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
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM karyawan WHERE " + column + " = ? AND id_karyawan != ?");
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
