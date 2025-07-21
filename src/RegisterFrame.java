import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    public RegisterFrame() {
        setTitle("Registrasi Pengguna Baru");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Konfirmasi Password:"));
        confirmPasswordField = new JPasswordField();
        panel.add(confirmPasswordField);
        
        panel.add(new JLabel()); // Sel kosong

        JButton registerButton = new JButton("Daftar");
        panel.add(registerButton);

        add(panel, BorderLayout.CENTER);

        registerButton.addActionListener(e -> registerUser());
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password tidak boleh kosong.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 8) {
            JOptionPane.showMessageDialog(this, "Password harus memiliki minimal 8 karakter.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Password dan konfirmasi password tidak cocok.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            // 1. Cek apakah username sudah ada
            String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Username sudah digunakan. Silakan pilih yang lain.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // 2. Hash password dan simpan pengguna baru
            String hashedPassword = PasswordUtil.hashPassword(password);
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            PreparedStatement insertPs = conn.prepareStatement(insertSql);
            insertPs.setString(1, username);
            insertPs.setString(2, hashedPassword);

            int rowsAffected = insertPs.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.");
                dispose(); // Tutup jendela registrasi setelah berhasil
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}