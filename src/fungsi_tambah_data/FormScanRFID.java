/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi_tambah_data;

import barokah_atk.konek;
import java.awt.Frame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author farel manalif h
 */
public class FormScanRFID extends FormTambah {
    private String idrfid = "";
    konek k = new konek();

    public FormScanRFID(Frame parent) {
        super(parent, "Scan Kartu RFID", "Kartu RFID");
        k.connect();

        JTextField tfBarcode = fieldMap.get("Kartu RFID");

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

    public String getId() {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT * FROM karyawan WHERE id_rfid = ?");
            stat.setString(1, idrfid);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String namaPanggilan = rs.getString("id_karyawan");
                return namaPanggilan;
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    @Override
    protected boolean validateInput() {
        String rfid = getFieldValue("Kartu RFID").trim();
        if (rfid.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Harap Scan Kartu!");
            return false;
        }

        if (!isDuplicate("id_rfid", rfid.trim())) {
            JOptionPane.showMessageDialog(dialog, "Kartu RFID tidak terdaftar atau bukan seorang admin!");
            return false;
        } else if (isDuplicate("id_rfid", rfid.trim())) {
            idrfid = getFieldValue("Kartu RFID");
            return true;
        }
        return false;
    }

    @Override
    protected boolean isDuplicate(String column, String value) {
        try {
            PreparedStatement stat = k.getCon().prepareStatement("SELECT 1 FROM karyawan WHERE " + column + " = ? AND id_level = ?");
            stat.setString(1, value);
            stat.setString(2, "admin");
            ResultSet rs = stat.executeQuery();
            return rs.next(); // Jika ada hasil, berarti duplikat
        } catch (Exception e) {
            JOptionPane.showMessageDialog(dialog, "Kesalahan database: " + e.getMessage());
            return false;
        }
    }
}
