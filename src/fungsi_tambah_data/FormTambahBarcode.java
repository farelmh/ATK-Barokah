package fungsi_tambah_data;

import java.awt.Frame;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import barokah_atk.konek;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class FormTambahBarcode extends FormTambah {

    konek k = new konek();

    public FormTambahBarcode(Frame parent) {
        super(parent, "Tambah Kode Barcode", "Kode Barcode");
        k.connect();
        
        JTextField tfBarcode = fieldMap.get("Kode Barcode");

        SwingUtilities.invokeLater(() -> {
            tfBarcode.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        if (validateInput()) {
                            isConfirmed = true;
                            dialog.dispose(); // tutup dialog kalau valid
                        }
                    }
                }
            });
        });
    }


    @Override
    protected boolean validateInput() {
        String barcode = getFieldValue("Kode Barcode");
        if (barcode.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Harap Scan Barang");
            return false;
        }

        if (isDuplicate("id_barcode", barcode.trim())) {
            JOptionPane.showMessageDialog(dialog, "Kode Barcode Sudah Terdaftar Sebelumnya!");
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
