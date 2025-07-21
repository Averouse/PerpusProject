import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    /**
     * Menghasilkan hash BCrypt dari password teks biasa.
     * @param plainPassword Password yang akan di-hash.
     * @return String hash dari password.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    /**
     * Memeriksa apakah password teks biasa cocok dengan hash yang tersimpan.
     * @param plainPassword Password yang dimasukkan pengguna.
     * @param hashedPassword Hash yang diambil dari database.
     * @return true jika cocok, false jika tidak.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}