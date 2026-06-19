import java.awt.Color;

// Sumber tunggal pemetaan warna piece, dipakai baik untuk output konsol (ANSI)
// maupun output gambar (AWT). Tiap entri memasangkan kode ANSI dan warna AWT
// sehingga keduanya tidak perlu lagi disinkronkan secara manual antar-file.
public final class Palette {
    public static final String RESET = "\u001B[0m";

    private static final class Entry {
        final String ansi;
        final Color awt;
        Entry(String ansi, Color awt) {
            this.ansi = ansi;
            this.awt = awt;
        }
    }

    private static final Entry[] ENTRIES = {
        new Entry("\u001B[31m", Color.RED),
        new Entry("\u001B[32m", Color.GREEN),
        new Entry("\u001B[33m", Color.YELLOW),
        new Entry("\u001B[34m", Color.BLUE),
        new Entry("\u001B[35m", Color.MAGENTA),
        new Entry("\u001B[36m", Color.CYAN),
        new Entry("\u001B[38;5;208m", Color.ORANGE),
        new Entry("\u001B[38;5;214m", new Color(255, 178, 102)), // Light Orange
        new Entry("\u001B[38;5;165m", new Color(76, 0, 153)),    // Purple
        new Entry("\u001B[38;5;200m", Color.PINK),
        new Entry("\u001B[38;5;118m", new Color(102, 255, 102)), // Bright Lime Green
        new Entry("\u001B[38;5;75m",  new Color(153, 204, 255)), // Sky Blue
        new Entry("\u001B[38;5;220m", new Color(255, 215, 0)),   // Gold
        new Entry("\u001B[38;5;130m", new Color(102, 51, 0)),    // Brown
        new Entry("\u001B[38;5;255m", Color.LIGHT_GRAY),
        new Entry("\u001B[38;5;21m",  new Color(0, 0, 153)),     // Deep Blue
        new Entry("\u001B[91m", new Color(255, 102, 102)),       // Bright Red
        new Entry("\u001B[92m", new Color(153, 255, 153)),       // Bright Green
        new Entry("\u001B[93m", new Color(255, 255, 153)),       // Bright Yellow
        new Entry("\u001B[94m", new Color(204, 229, 255)),       // Bright Blue
        new Entry("\u001B[95m", new Color(229, 204, 255)),       // Bright Magenta
        new Entry("\u001B[96m", new Color(204, 255, 255)),       // Bright Cyan
        new Entry("\u001B[90m", Color.DARK_GRAY),
        new Entry("\u001B[97m", Color.WHITE),
        new Entry("\u001B[38;5;196m", new Color(153, 0, 0)),     // Dark Red
        new Entry("\u001B[38;5;46m",  new Color(0, 102, 0)),     // Dark Green
    };

    private Palette() {}

    // Pemetaan deterministik huruf piece (A-Z) ke sebuah entri warna
    private static int indexFor(char letter) {
        int i = (letter - 'A') % ENTRIES.length;
        return i < 0 ? i + ENTRIES.length : i;
    }

    public static String ansiFor(char letter) {
        return ENTRIES[indexFor(letter)].ansi;
    }

    public static Color awtFor(char letter) {
        return ENTRIES[indexFor(letter)].awt;
    }
}
