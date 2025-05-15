package barokah_atk;

import javax.swing.JOptionPane;
import java.sql.Connection;
import java.sql.DriverManager;

public class konek {

    private String url = "jdbc:mysql://localhost:3306/elkasir";
    private String username = "root";
    private String password = "";
    private Connection con;

    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public Connection getCon() {
        return con;
    }
}
