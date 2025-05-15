package fungsi_tambah_data;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class FormTambah {

    protected JDialog dialog;
    protected Map<String, JTextField> fieldMap = new HashMap<>();
    protected boolean isConfirmed = false;
    protected JPanel panel;

    public FormTambah(Frame parent, String title, String... fields) {
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
            label.setFont(new Font("Segoe UI", Font.BOLD, 14));
            label.setForeground(new Color(50, 50, 50));
            dialog.add(label, gbc);

            gbc.gridx = 1;
            JTextField textField = new JTextField(15);
            textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            fieldMap.put(fields[i], textField);
            dialog.add(textField, gbc);
        }

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(190, 30));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            if (validateInput()) {
                isConfirmed = true;
                dialog.dispose();
            }
        });

        okButton.setBackground(new Color(17, 45, 78));
        okButton.setForeground(Color.white);
        okButton.setPreferredSize(new Dimension(70, 30));

        panel.add(okButton, BorderLayout.WEST);

        gbc.gridx = 1;
        gbc.gridy = fields.length;
        
        tambahKomponenTambahan(gbc);
        
        gbc.gridy = fields.length + 5;
        dialog.add(panel, gbc);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
    }

    public boolean showDialog() {
        dialog.setVisible(true);
        return isConfirmed;
    }

    public String getFieldValue(String fieldName) {
        return fieldMap.getOrDefault(fieldName, new JTextField()).getText();
    }

    public void setPesan(String pesan) {
        JOptionPane.showMessageDialog(dialog, pesan);
    }

    protected void tambahKomponenTambahan(GridBagConstraints gbc) {
        // Untuk override di subclass
    }

    protected abstract boolean validateInput();

    protected abstract boolean isDuplicate(String column, String value);
}
