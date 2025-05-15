package fungsi_tambah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import barokah_atk.konek;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class FormTambahKaryawan extends FormTambah {

    private JComboBox<String> comboJabatan;
    konek k = new konek();

    public FormTambahKaryawan(Frame parent) {
        super(parent, "Tambah Data Karyawan", "ID Karyawan", "Nama Lengkap", "Nama Panggilan", "Nomor Telepon", "Email");
        k.connect();
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
        comboJabatan.setPreferredSize(new Dimension(190, 30));
        gbc.gridx = 1;
        dialog.add(comboJabatan, gbc);
    }

    @Override
    protected boolean validateInput() {
        String id = getFieldValue("ID Karyawan");
        String namaLengkap = getFieldValue("Nama Lengkap");
        String namaPanggilan = getFieldValue("Nama Panggilan");
        String nomorTelp = getFieldValue("Nomor Telepon");
        String email = getFieldValue("Email");

        if (id.isEmpty() || namaLengkap.isEmpty() || namaPanggilan.isEmpty() || nomorTelp.isEmpty() || email.isEmpty()) {
            setPesan("Semua kolom harus diisi");
            return false;
        } else if (id.length() != 5) {
            setPesan("ID Karyawan harus 5 karakter!");
            return false;
        } else if (namaLengkap.length() < 5) {
            setPesan("Nama Lengkap minimal 5 karakter!");
            return false;
        } else if (namaPanggilan.length() < 3) {
            setPesan("Nama Panggilan minimal 3 karakter!");
            return false;
        } else if (!nomorTelp.matches("\\d+")) {
            setPesan("Nomor Telepon harus berupa angka");
            return false;
        } else if (nomorTelp.length() < 11 || nomorTelp.length() > 13) {
            setPesan("Nomor Telepon harus diantara 11 - 13 karakter");
            return false;
        } else if (email.length() < 15) {
            setPesan("Email minimal 15 karakter");
            return false;
        } else if (!email.contains("@gmail.com")) {
            setPesan("format email harus @gmail.com");
            return false;
        }

        if (isDuplicate("id_karyawan", id)) {
            setPesan("ID Karyawan sudah terdaftar! gunakan ID lain");
            return false;
        } else if (isDuplicate("nama_karyawan", namaLengkap)) {
            setPesan("Nama ini sudah terdaftar!");
            return false;
        } else if (isDuplicate("no_telp", nomorTelp)) {
            setPesan("Nomor Telepon sudah terdaftar!");
            return false;
        } else if (isDuplicate("email", email)) {
            setPesan("email sudah terdaftar!");
            return false;
        }
        return true;
    }

    @Override
    protected boolean isDuplicate(String column, String value) {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM karyawan WHERE " + column + " = ?");
            stat.setString(1, value);
            ResultSet rs = stat.executeQuery();
            return rs.next(); // Jika ada hasil, berarti duplikat
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Kesalahan database: " + e.getMessage());
            return false;
        }
    }

    public String getJabatanTerpilih() {
        return comboJabatan.getSelectedItem().toString().toLowerCase();
    }

}
