import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        setTitle("Login - Aplikasi Perpustakaan");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        panel.add(usernameField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        JButton loginButton = new JButton("Login");
        panel.add(loginButton);

        JButton registerButton = new JButton("Registrasi");
        panel.add(registerButton);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> openRegisterFrame());
        
        // Menjadikan tombol Login sebagai default saat menekan Enter
        this.getRootPane().setDefaultButton(loginButton);
    }

    private void loginUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username dan password harus diisi.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT id, password_hash FROM users WHERE username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int userId = rs.getInt("id"); // Ambil ID pengguna
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.checkPassword(password, storedHash)) {
                    // Login berhasil
                    dispose(); // Tutup jendela login
                    // Buka aplikasi utama
                    SwingUtilities.invokeLater(() -> new App(userId).frame.setVisible(true));
                } else {
                    // Password salah
                    JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Username tidak ditemukan
                JOptionPane.showMessageDialog(this, "Username atau password salah.", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal terhubung ke database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRegisterFrame() {
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}