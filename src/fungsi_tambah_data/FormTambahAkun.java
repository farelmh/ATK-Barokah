package fungsi_tambah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import barokah_atk.konek;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FormTambahAkun extends FormTambah {

    konek k = new konek();

    public FormTambahAkun(Frame parent) {
        super(parent, "Tambah Akun", "Username", "Password");
        k.connect();
    }

    @Override
    protected boolean validateInput() {
        String username = getFieldValue("Username");
        String password = getFieldValue("Password");

        if (username.isEmpty() || password.isEmpty()) {
            setPesan("Semua kolom harus diisi!");
            return false;
        } else if (username.length() < 5 || username.length() > 15) {
            setPesan("Username harus diantara 5 - 15 karakter");
            return false;
        }
        if (isDuplicate("username", username)) {
            setPesan("Username sudah terdaftar!");
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            setPesan("Password harus diantara 8 - 20 karakter");
            return false;
        } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*\\-_]).{8,20}$")) {
            setPesan("Password harus mengandung kombinasi huruf kapital, huruf kecil, angka, dan simbol khusus,\n"
                    + "simbol khusus yang bisa digunakan: ! @ # $ % & * - _ ");
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
            return rs.next();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Kesalahan database: " + e.getMessage());
            return false;
        }
    }

}
