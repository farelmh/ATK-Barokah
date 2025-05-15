package fungsi_lain;

import barokah_atk.konek;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;

public class session {

    private static session instance;
    private PreparedStatement stat;
    private ResultSet rs;
    private String nama_karyawan = "";
    private String nama_lengkap = "";
    private String id_karyawan = "";
    konek k = new konek();

    private session() {
        k.connect();
    }

    public static session getInstance() {
        if (instance == null) {
            instance = new session();
        }
        return instance;
    }

    public void setNama(String idKaryawan) {
        try {
            this.stat = k.getCon().prepareStatement("Select * from karyawan where id_karyawan = ?");
            stat.setString(1, idKaryawan);
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                String nama_kry = rs.getString("nama_panggilan");
                String nama_lengkap = rs.getString("nama_karyawan");
                String id_karyawan = rs.getString("id_karyawan");
                this.nama_karyawan = nama_kry;
                this.nama_lengkap = nama_lengkap;
                this.id_karyawan = id_karyawan;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.out.println("error disini");
        }
    }

    public String getNama() {
        return nama_karyawan;
    }
    
    public String getNamaLengkap() {
        return nama_lengkap;
    }
    
    public String getId() {
        return id_karyawan;
    }
}
