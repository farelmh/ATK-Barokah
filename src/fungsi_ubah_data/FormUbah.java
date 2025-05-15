package fungsi_ubah_data;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public abstract class FormUbah {

    protected JDialog dialog;
    protected Map<String, JTextField> fieldMap = new HashMap<>();
    private boolean isConfirmed = false;

    public FormUbah(Frame parent, String title, String... fields) {
        dialog = new JDialog(parent, title, true);
        dialog.setLayout(new GridBagLayout());
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;

            JLabel label = new JLabel(fields[i] + ":");
            label.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Ubah ke font bold
            label.setForeground(new Color(50, 50, 50));
            dialog.add(label, gbc);

            gbc.gridx = 1;
            JTextField textField = new JTextField(15);
            fieldMap.put(fields[i], textField);
            dialog.add(textField, gbc);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(190, 30));

        // Tombol Hapus
        JButton deleteButton = new JButton("Hapus");
        deleteButton.addActionListener(e -> {
            onDelete();
            dialog.dispose();
        });

        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.white);
        deleteButton.setPreferredSize(new Dimension(80, 30));

        gbc.gridx = 0;
        gbc.gridy = fields.length + 1;
        dialog.add(deleteButton, gbc);

        // Tombol Simpan
        JButton okButton = new JButton("Simpan");
        okButton.addActionListener(e -> {
            if (validateInput()) {
                isConfirmed = true;
                dialog.dispose();
            }
        });
        okButton.setBackground(new Color(17, 45, 78));
        okButton.setForeground(Color.white);
        okButton.setPreferredSize(new Dimension(80, 30));

        panel.add(okButton, BorderLayout.WEST);

        gbc.gridx = 1;
        gbc.gridy = fields.length;

        tambahKomponenTambahan(gbc);

        gbc.gridy = fields.length + 1;
        dialog.add(panel, gbc);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return isConfirmed;
    }

    public void setPesan(String pesan) {
        JOptionPane.showMessageDialog(dialog, pesan);
    }

    public String getFieldValue(String fieldName) {
        return fieldMap.getOrDefault(fieldName, new JTextField()).getText();
    }

    public void setFieldValue(String fieldName, String value) {
        if (fieldMap.containsKey(fieldName)) {
            fieldMap.get(fieldName).setText(value);
        }
    }

    protected void tambahKomponenTambahan(GridBagConstraints gbc) {
        // Untuk override di subclass
    }

    protected abstract boolean validateInput();

    protected abstract void onDelete();
    
    protected abstract boolean isDuplicate(String column, String value, String id);
}
