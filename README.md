# Aplikasi Manajemen Perpustakaan Sederhana

Aplikasi desktop yang dibangun menggunakan Java Swing untuk membantu pustakawan atau pengguna perorangan dalam mengelola koleksi buku pribadi dan melacak aktivitas peminjaman. Aplikasi ini dirancang sebagai sistem multi-pengguna di mana setiap pengguna memiliki koleksi bukunya masing-masing yang terpisah.

## Fitur Utama

  * **Autentikasi Pengguna**: Sistem registrasi dan login yang aman untuk setiap pengguna.
  * **Keamanan Password**: Menggunakan hashing **jBCrypt** untuk melindungi password pengguna.
  * [cite\_start]**Manajemen Buku (CRUD)**: Pengguna dapat menambah, melihat, mengedit, dan menghapus koleksi bukunya sendiri[cite: 13, 112].
  * [cite\_start]**Manajemen Gambar**: Kemampuan untuk mengunggah, menampilkan pratinjau, dan menyimpan gambar sampul buku[cite: 63, 119]. Gambar secara otomatis disalin ke dalam folder proyek agar portabel.
  * [cite\_start]**Paginasi Data**: Menampilkan daftar buku dalam beberapa halaman untuk menjaga performa aplikasi[cite: 48, 50, 51].
  * [cite\_start]**Pencarian & Penyaringan**: Fitur untuk mencari buku berdasarkan berbagai kriteria (judul, pengarang, dll.) serta menyortir data[cite: 19, 20].
  * [cite\_start]**Impor & Ekspor**: Fungsi untuk mengimpor data buku dari file CSV dan mengekspor koleksi ke dalam file CSV[cite: 24].
  * **Sistem Peminjaman**: Mencatat transaksi peminjaman dan pengembalian buku.
  * [cite\_start]**Laporan**: Menampilkan laporan transaksi peminjaman yang diproses oleh pengguna yang sedang login[cite: 30].
  * [cite\_start]**Antarmuka Modern**: Menggunakan library **FlatLaf** untuk tampilan yang bersih dan modern[cite: 1].

## Teknologi yang Digunakan

  * **Bahasa**: Java
  * **Framework GUI**: Java Swing
  * **Database**: MySQL
  * **Konektivitas**: JDBC (Java Database Connectivity)
  * **Library Tambahan**:
      * **FlatLaf**: Untuk tema antarmuka.
      * **jBCrypt**: Untuk hashing password.
      * **MySQL Connector/J**: Driver JDBC untuk MySQL.

-----

## Cara Menjalankan Proyek

### Prasyarat

1.  **JDK** (Java Development Kit) versi 8 atau yang lebih baru.
2.  **Server Database MySQL** (bisa menggunakan XAMPP, Laragon, atau instalasi mandiri).
3.  **IDE** (Integrated Development Environment) seperti Eclipse, IntelliJ IDEA, atau NetBeans.

### Langkah-langkah Instalasi

**1. Pengaturan Database**

  * Buka *tool* manajemen database Anda (misalnya phpMyAdmin).
  * Buat database baru dengan nama `perpustakaan`.
  * Jalankan tiga script SQL di bawah ini untuk membuat tabel `users`, `buku`, dan `peminjaman`.

**2. Pengaturan Proyek**

  * Buka proyek ini menggunakan IDE pilihan Anda.
  * Tambahkan 3 file `.jar` berikut ke dalam *build path* atau *dependencies* proyek Anda:
      * `flatlaf-x.x.jar` (sesuai versi yang Anda unduh)
      * `jbcrypt-0.4.jar`
      * `mysql-connector-j-x.x.x.jar` (driver resmi dari MySQL)

**3. Konfigurasi Koneksi Database**

  * [cite\_start]Buka file `App Java.txt` dan cari kelas `DBUtil` di bagian bawah [cite: 293-295].
  * Pastikan detail koneksi (URL, user, dan password) sudah sesuai dengan pengaturan MySQL di komputer Anda.
    ```java
    class DBUtil {
        public static Connection getConnection() throws Exception {
            String url = "jdbc:mysql://localhost:3306/perpustakaan";
            String user = "root";
            String pass = ""; // Sesuaikan jika password MySQL Anda berbeda
            return DriverManager.getConnection(url, user, pass);
        }
    }
    ```

**4. Jalankan Aplikasi**

  * Cari file `App.java` yang berisi metode `main`.
  * Jalankan file tersebut dari IDE Anda. Jendela login akan muncul sebagai program pertama.

-----

## Struktur Database

Berikut adalah script SQL final untuk membuat semua tabel yang diperlukan.

**Tabel `users`**

```sql
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Tabel `buku`**

```sql
CREATE TABLE buku (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    judul VARCHAR(255) NOT NULL,
    pengarang VARCHAR(255),
    penerbit VARCHAR(255),
    kategori VARCHAR(100),
    tahun_terbit VARCHAR(10),
    gambar VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Tabel `peminjaman`**

```sql
CREATE TABLE peminjaman (
    id INT AUTO_INCREMENT PRIMARY KEY,
    buku_id INT,
    user_id INT,
    nama_peminjam VARCHAR(100) NOT NULL,
    tanggal_pinjam DATE,
    tanggal_kembali DATE,
    status VARCHAR(20),
    FOREIGN KEY (buku_id) REFERENCES buku(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);
```

-----
