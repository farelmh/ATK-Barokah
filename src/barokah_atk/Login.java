package barokah_atk;

import admin.adm_dashboard;
import javax.swing.JOptionPane;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import fungsi_lain.session;
import kasir.ksr_dashboard;

public class Login extends javax.swing.JFrame {

    private PreparedStatement stat;
    private ResultSet rs;
    private int loginAttempts = 0;
    private final int MAX_ATTEMPTS = 3;

    konek i = new konek();

    public Login() {
        i.connect();
        initComponents();
        loginrfid();
        this.setLocationRelativeTo(null);
    }

    class user {

        String username, password, id_level, rfid;

        public user() {
            this.username = txt_username.getText();
            this.password = txt_password.getText();
            this.id_level = "";
            this.rfid = txt_username.getText();
        }
    }

    private void setTimer() {
        btn_submit.setEnabled(false);
        btn_lupaSandi.setEnabled(false);

        // Timer untuk jeda 10 detik
        int DELAY_TIME = 10000;
        javax.swing.Timer timer = new javax.swing.Timer(DELAY_TIME, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Anda bisa mencoba login kembali.");
                btn_submit.setEnabled(true);
                btn_lupaSandi.setEnabled(true);
                loginAttempts = 0;
            }
        });
        timer.setRepeats(false); // Timer hanya dijalankan sekali
        timer.start(); // Mulai timer
    }

    // fungsi login
    private void logincek() {
        user u = new user();
        String idkaryawan = "";
        try {
            this.stat = i.getCon().prepareStatement("CALL pilih_akun(?, ?);");
            stat.setString(1, u.username);
            stat.setString(2, u.password);
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                u.id_level = rs.getString("id_level");
                idkaryawan = rs.getString("id_karyawan");
            }

            if (u.id_level == "") {
                loginAttempts++;
                int remainingAttempts = MAX_ATTEMPTS - loginAttempts;

                if (remainingAttempts > 0) {
                    JOptionPane.showMessageDialog(null, "Login gagal! Anda memiliki " + remainingAttempts + " percobaan lagi.");
                } else {
                    JOptionPane.showMessageDialog(null, "Login gagal! Anda telah mencapai batas percobaan.\nHarap tunggu 10 detik sebelum mencoba lagi.");
                    setTimer();
                }
            } else {
                session.getInstance().setNama(idkaryawan);
                loginAttempts = 0;
                ceklevel(u.id_level);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

    }

    //fungsi login rfid
    private void loginrfid() {
        txt_username.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String uid = txt_username.getText().trim();
                    if (uid != null && !uid.isEmpty()) {
                        proseslogin(uid);
                        txt_username.setText("");
                    }
                }
            }
        });
    }

    //cek admin or kasir
    private void ceklevel(String id) {
        switch (id) {
            case "admin":
                adm_dashboard db = new adm_dashboard();
                db.setVisible(true);
                this.dispose();
                break;
            case "kasir":
                ksr_dashboard bd = new ksr_dashboard();
                bd.setVisible(true);
                this.dispose();
                break;
        }

    }

    //cari data dengan rfid
    public void proseslogin(String uid) {
        String usn = "";
        String pwd = "";
        String idLevel = "";
        try {
            this.stat = i.getCon().prepareStatement("CALL pilihRFID(?)");
            stat.setString(1, txt_username.getText());
            this.rs = this.stat.executeQuery();

            if (rs.next()) {
                usn = rs.getString("username");
                pwd = rs.getString("password");
                idLevel = rs.getString("id_level");

                txt_username.setText("");
                int result = JOptionPane.showConfirmDialog(
                        null,
                        "username : " + usn + "\npassword : " + "*".repeat(pwd.length()),
                        "konfirmasi",
                        JOptionPane.YES_NO_OPTION
                );

                if (result == JOptionPane.YES_OPTION) {
                    ceklevel(idLevel);
                }
            } else if (txt_username.getText().trim() != "") {
                txt_username.setText("");
                JOptionPane.showMessageDialog(this, "Tidak Terdaftar!", "Akses Ditolak", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        labelj = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        txt_username = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txt_password = new javax.swing.JPasswordField();
        showpasswd = new javax.swing.JCheckBox();
        btn_submit = new javax.swing.JLabel();
        btn_lupaSandi = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBackground(new java.awt.Color(17, 45, 78));
        jPanel2.setForeground(new java.awt.Color(54, 116, 181));
        jPanel2.setPreferredSize(new java.awt.Dimension(550, 700));

        jPanel1.setBackground(new java.awt.Color(63, 114, 175));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(17, 45, 78));
        jLabel2.setText("Tempelkan kartu atau isi username dan password");

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/Vector (1).png"))); // NOI18N

        labelj.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        labelj.setForeground(new java.awt.Color(204, 204, 204));
        labelj.setText("BAROKAH ATK");

        jPanel3.setBackground(new java.awt.Color(219, 226, 239));
        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 102, 255)));

        txt_username.setBackground(new java.awt.Color(242, 232, 198));
        txt_username.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_usernameActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(17, 45, 78));
        jLabel6.setText("Username");

        jLabel7.setBackground(new java.awt.Color(51, 51, 51));
        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(17, 45, 78));
        jLabel7.setText("Password");

        txt_password.setBackground(new java.awt.Color(242, 232, 198));
        txt_password.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                txt_passwordMouseClicked(evt);
            }
        });

        showpasswd.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        showpasswd.setForeground(new java.awt.Color(17, 45, 78));
        showpasswd.setText("Show Password");
        showpasswd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showpasswdActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(showpasswd)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel6)
                        .addComponent(jLabel7)
                        .addComponent(txt_username, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                        .addComponent(txt_password)))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_username, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txt_password, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showpasswd)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        btn_submit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gambar/Group 2_2.png"))); // NOI18N
        btn_submit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_submit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_submitMouseClicked(evt);
            }
        });

        btn_lupaSandi.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btn_lupaSandi.setForeground(new java.awt.Color(255, 255, 255));
        btn_lupaSandi.setText("Lupa Sandi?");
        btn_lupaSandi.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btn_lupaSandi.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btn_lupaSandiMouseClicked(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btn_lupaSandiMouseReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(21, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(163, 163, 163))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(labelj, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(119, 119, 119))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btn_submit)
                        .addGap(153, 153, 153))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(btn_lupaSandi)
                        .addGap(177, 177, 177))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelj)
                .addGap(50, 50, 50)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btn_submit)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_lupaSandi)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txt_usernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_usernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_usernameActionPerformed

    private void btn_submitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_submitMouseClicked

        user u = new user();

        if (btn_submit.isEnabled()) {
            if (u.username.isEmpty() || u.password.isEmpty()) {
                JOptionPane.showMessageDialog(null, "username atau password tidak boleh kosong");
            } else if (u.username.length() < 5 || u.username.length() > 15) {
                JOptionPane.showMessageDialog(null, "username harus berisi antara 5 sampai 15 karakter");
            } else if (u.password.length() < 8 || u.password.length() > 20) {
                JOptionPane.showMessageDialog(null, "password harus berisi antara 8 sampai 20 karakter");
            } else {
                logincek();
            }
        }

    }//GEN-LAST:event_btn_submitMouseClicked

    private void btn_lupaSandiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_lupaSandiMouseClicked
        // TODO add your handling code here:
        if (btn_lupaSandi.isEnabled()) {
            LupaSandi l = new LupaSandi();
            l.setVisible(true);
            this.dispose();
        }
    }//GEN-LAST:event_btn_lupaSandiMouseClicked

    private void txt_passwordMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_txt_passwordMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_passwordMouseClicked

    private void showpasswdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showpasswdActionPerformed
        // TODO add your handling code here:
        if (showpasswd.isSelected()) {
            txt_password.setEchoChar((char) 0);

        } else {
            txt_password.setEchoChar('*');
        }
    }//GEN-LAST:event_showpasswdActionPerformed

    private void btn_lupaSandiMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btn_lupaSandiMouseReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_lupaSandiMouseReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
//        UIManager.put("Panel.background", new Color(128, 0, 0));
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Login.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel btn_lupaSandi;
    private javax.swing.JLabel btn_submit;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel labelj;
    private javax.swing.JCheckBox showpasswd;
    private javax.swing.JPasswordField txt_password;
    private javax.swing.JTextField txt_username;
    // End of variables declaration//GEN-END:variables
}
