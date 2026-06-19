import java.io.*;
import java.util.*;


public class IQPuzzlerSolver {
    private int rows, cols, pieceCount;
    private char[][] board;
    private char[][] maskBoard; // For Masking the board
    private List<char[][]> pieces = new ArrayList<>();
    // Transformasi (rotasi/mirror) tiap piece dihitung sekali saja, bukan di setiap node rekursi
    private List<List<char[][]>> pieceTransformations = new ArrayList<>();
    private char[] pieceSymbols; // Huruf simbol tiap piece
    private long iterations = 0;
    // true = brute force murni (sesuai spek tugas); false = backtracking dengan heuristik (lebih cepat).
    // Dapat diubah ke false saat runtime dengan argumen --fast
    private boolean bruteForce = true;

    public static void main(String[] args) {
        IQPuzzlerSolver solver = new IQPuzzlerSolver();
        for (String arg : args) {
            if (arg.equals("--fast")) solver.bruteForce = false;
        }
        solver.run();
    }

    private void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println(bruteForce ? "Mode: brute force" : "Mode: cepat (heuristik)");
        System.out.print("Masukkan path test case(.txt): ");
        String filePath = scanner.nextLine();
        System.out.println("");
        if (!readInputFile(filePath)) {
            scanner.close();
            return;
        }

        long startTime = System.currentTimeMillis();
        precompute();
        boolean solved;
        if (bruteForce) {
            // Brute force murni: biarkan pencarian sendiri yang menemukan ada/tidaknya solusi
            solved = solveBrute(0);
        } else {
            // solveFast mengandalkan jaminan isFeasible (total sel piece == sel papan) untuk base case-nya
            solved = isFeasible() && solveFast(new boolean[pieces.size()]);
        }
        long endTime = System.currentTimeMillis();

        if (solved) {
            printBoard();
        } else {
            System.out.println("Tidak ditemukan solusi.\n");
        }

        System.out.println("Waktu pencarian: " + (endTime - startTime) + " ms\n");
        System.out.println("Banyak kasus yang ditinjau: " + iterations +"\n");

        if (solved) {
            System.out.print("Apakah anda ingin menyimpan solusi sebagai txt? (ya/tidak): ");
            if (scanner.nextLine().equalsIgnoreCase("ya")) {
                saveSolution(filePath + "_solution.txt", filePath);
            }
            System.out.print("Apakah anda ingin menyimpan solusi sebagai gambar? (ya/tidak): ");
            if (scanner.nextLine().equalsIgnoreCase("ya")) {
                System.out.print("Masukkan nama file: ");
                String filename = scanner.nextLine();
                SaveAsImage.save(board, rows, cols, filePath, filename);
            }
        }
        scanner.close();
    }

    private boolean readInputFile(String filePath) {
        Set<Character> usedLetters = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String dimLine = br.readLine();
            if (dimLine == null) throw new IllegalArgumentException("File kosong.");
            String[] dimensions = dimLine.trim().split("\\s+");
            if (dimensions.length < 3) throw new IllegalArgumentException("Baris pertama harus berisi N M P.");
            rows = Integer.parseInt(dimensions[0]);
            cols = Integer.parseInt(dimensions[1]);
            pieceCount = Integer.parseInt(dimensions[2]);
            if (rows <= 0 || cols <= 0 || pieceCount <= 0) {
                throw new IllegalArgumentException("N, M, dan P harus bilangan bulat positif.");
            }
            maskBoard = new char[rows][cols];
            board = new char[rows][cols];

            String boardType = br.readLine();
            if (boardType != null) boardType = boardType.trim();
            if ("DEFAULT".equals(boardType)) {
                for (char[] row : maskBoard) Arrays.fill(row, 'X');
            } else if ("CUSTOM".equals(boardType)) {
                for (int i = 0; i < rows; i++) {
                    String boardRow = br.readLine();
                    if (boardRow == null) {
                        throw new IllegalArgumentException("Konfigurasi papan CUSTOM kurang dari " + rows + " baris.");
                    }
                    Arrays.fill(maskBoard[i], '.'); // Padding agar baris pendek tidak menyebabkan crash
                    for (int j = 0; j < boardRow.length(); j++) {
                        char ch = boardRow.charAt(j);
                        if (j >= cols) {
                            if (ch != ' ') throw new IllegalArgumentException("Baris papan CUSTOM ke-" + (i + 1) + " lebih panjang dari " + cols + " kolom.");
                            continue;
                        }
                        if (ch != 'X' && ch != '.' && ch != ' ') {
                            throw new IllegalArgumentException("Papan CUSTOM hanya boleh berisi 'X', '.', atau spasi.");
                        }
                        maskBoard[i][j] = (ch == 'X') ? 'X' : '.';
                    }
                }
            } else {
                throw new IllegalArgumentException("Tipe papan harus 'DEFAULT' atau 'CUSTOM', bukan '" + boardType + "'.");
            }
            for (char[] row : board) Arrays.fill(row, '.');

            int piecesRead = 0;
            String currentLetter = "";
            List<String> shapeLines = new ArrayList<>();
            String line;

            while ((line = br.readLine()) != null) {
                line = line.stripTrailing(); // Menghapus space tambahan
                if (line.isEmpty()) continue; // Melewati barisan kosong

                char firstChar = line.trim().charAt(0); // Memeriksa huruf
                if (currentLetter.isEmpty() || firstChar == currentLetter.charAt(0)) {
                    shapeLines.add(line);
                } else {
                    // Simpan piece sebelum, tambah piece lain
                    addPiece(shapeLines, currentLetter.charAt(0), usedLetters);
                    piecesRead++;
                    shapeLines.clear();
                    shapeLines.add(line);
                }
                currentLetter = String.valueOf(firstChar);
            }

            // Piece terakhir
            if (!shapeLines.isEmpty()) {
                addPiece(shapeLines, currentLetter.charAt(0), usedLetters);
                piecesRead++;
            }

            if (piecesRead != pieceCount) {
                throw new IllegalArgumentException("Jumlah piece (" + piecesRead + ") tidak sama dengan P (" + pieceCount + ").");
            }
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage() + "\n");
            return false;
        } catch (Exception e) {
            System.out.println("Gagal membaca file test case atau file tidak ada.\n");
            return false;
        }
    }

    // Memvalidasi lalu menambahkan sebuah piece ke daftar pieces
    private void addPiece(List<String> shapeLines, char letter, Set<Character> usedLetters) {
        if (letter < 'A' || letter > 'Z') {
            throw new IllegalArgumentException("Piece harus diberi label huruf kapital A-Z, bukan '" + letter + "'.");
        }
        if (!usedLetters.add(letter)) {
            throw new IllegalArgumentException("Label piece '" + letter + "' digunakan lebih dari sekali.");
        }
        pieces.add(convertToMatrix(shapeLines, letter));
    }

    // Mengecek kelayakan sebelum solve: total sel piece harus sama dengan sel papan yang harus diisi,
    // dan tidak ada piece yang lebih besar dari papan. Mengembalikan true jika masih mungkin ada solusi.
    private boolean isFeasible() {
        int fillableCells = 0;
        for (char[] row : maskBoard) {
            for (char cell : row) {
                if (cell == 'X') fillableCells++;
            }
        }
        int totalPieceCells = 0;
        for (char[][] piece : pieces) {
            for (char[] row : piece) {
                for (char cell : row) {
                    if (cell != ' ') totalPieceCells++;
                }
            }
            if (piece.length > rows || piece[0].length > cols) {
                return false; // Piece tidak muat di papan
            }
        }
        return totalPieceCells == fillableCells;
    }

    private char[][] convertToMatrix(List<String> shapeLines, char letter) {
        int h = shapeLines.size();
        int w = shapeLines.stream().mapToInt(String::length).max().orElse(0);

        // Membuat matrix kosong
        char[][] shape = new char[h][w];
        for (int i = 0; i < h; i++) {
            Arrays.fill(shape[i], ' ');
            String line = shapeLines.get(i);
            for (int j = 0; j < line.length(); j++) {
                if (line.charAt(j) == letter) {
                    shape[i][j] = letter; // Matrix diisi dengan huruf
                }
            }
        }
        return shape;
    }

    // Menghitung transformasi dan simbol tiap piece sekali sebelum proses solve
    private void precompute() {
        pieceSymbols = new char[pieces.size()];
        for (int p = 0; p < pieces.size(); p++) {
            pieceTransformations.add(generateTransformations(pieces.get(p)));
            pieceSymbols[p] = symbolOf(pieces.get(p));
        }
    }

    private char symbolOf(char[][] piece) {
        for (char[] row : piece) {
            for (char ch : row) {
                if (ch != ' ') return ch;
            }
        }
        return '?'; // Tidak akan terjadi: setiap piece punya minimal satu sel
    }

    // Brute force murni: tiap piece (urut), coba semua transformasi di semua posisi papan.
    // Mengeksplorasi seluruh ruang solusi secara ekshaustif tanpa heuristik.
    private boolean solveBrute(int pieceIndex) {
        if (pieceIndex >= pieces.size()) {
            return firstEmptyCell() < 0; // Semua sel yang harus diisi sudah terisi
        }
        for (char[][] t : pieceTransformations.get(pieceIndex)) {
            for (int r = 0; r <= rows - t.length; r++) {
                for (int c = 0; c <= cols - t[0].length; c++) {
                    if (canPlace(t, r, c)) {
                        placePiece(t, r, c, pieceSymbols[pieceIndex]);
                        iterations++;
                        if (solveBrute(pieceIndex + 1)) return true;
                        removePiece(t, r, c);
                    }
                }
            }
        }
        return false;
    }

    // Backtracking dengan heuristik: hanya mencoba penempatan yang menutupi sel kosong pertama.
    private boolean solveFast(boolean[] used) {
        // Cari sel kosong pertama yang harus diisi. Jika tidak ada, papan selesai.
        // Karena total sel piece == sel yang harus diisi (lihat isFeasible), papan penuh berarti semua piece terpakai.
        int target = firstEmptyCell();
        if (target < 0) return true;
        int tr = target / cols, tc = target % cols;

        for (int p = 0; p < pieces.size(); p++) {
            if (used[p]) continue;
            for (char[][] t : pieceTransformations.get(p)) {
                // Coba semua penempatan di mana sebuah sel piece menutupi sel target
                for (int i = 0; i < t.length; i++) {
                    for (int j = 0; j < t[i].length; j++) {
                        if (t[i][j] == ' ') continue;
                        int r = tr - i, c = tc - j;
                        if (r < 0 || c < 0 || !canPlace(t, r, c)) continue;
                        placePiece(t, r, c, pieceSymbols[p]);
                        used[p] = true;
                        iterations++;
                        if (solveFast(used)) return true;
                        used[p] = false;
                        removePiece(t, r, c);
                    }
                }
            }
        }
        return false;
    }

    private int firstEmptyCell() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (maskBoard[i][j] == 'X' && board[i][j] == '.') {
                    return i * cols + j;
                }
            }
        }
        return -1;
    }

    private List<char[][]> generateTransformations(char[][] piece) {
        Set<String> uniqueTransformations = new HashSet<>();
        List<char[][]> transformations = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            piece = rotate90(piece);
            if (uniqueTransformations.add(Arrays.deepToString(piece))) {
                transformations.add(copyMatrix(piece));
            }
            char[][] mirrored = mirror(piece);
            if (uniqueTransformations.add(Arrays.deepToString(mirrored))) {
                transformations.add(copyMatrix(mirrored));
            }
        }
        return transformations;
    }

    private char[][] copyMatrix(char[][] matrix) {
        char[][] copy = new char[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }
    // Modify pieces
    private char[][] rotate90(char[][] piece) {
        int h = piece.length, w = piece[0].length;
        char[][] rotated = new char[w][h];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                rotated[j][h - 1 - i] = piece[i][j];
            }
        }
        return rotated;
    }

    private char[][] mirror(char[][] piece) {
        int h = piece.length, w = piece[0].length;
        char[][] mirrored = new char[h][w];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                mirrored[i][w - 1 - j] = piece[i][j];
            }
        }
        return mirrored;
    }

    private boolean canPlace(char[][] piece, int r, int c) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != ' ') {
                    if (r + i >= rows || c + j >= cols || board[r + i][c + j] != '.' || maskBoard[r + i][c + j] != 'X') {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void placePiece(char[][] piece, int r, int c, char symbol) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != ' ') {
                    board[r + i][c + j] = symbol;
                }
            }
        }
    }

    private void removePiece(char[][] piece, int r, int c) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] != ' ') {
                    board[r + i][c + j] = '.';
                }
            }
        }
    }

    private void printBoard() { // Print board
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char cell = board[i][j];

                if (cell == '.') {
                    System.out.print((maskBoard[i][j] == 'X' ? "." : " ") + " "); // Menyembunyikan . dan/atau X pada board
                } else {
                    System.out.print(Palette.ansiFor(cell) + cell + Palette.RESET + " ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private void saveSolution(String outputPath, String testCasePath) { // File .txt
        File testCaseDir = new File(testCasePath).getParentFile();
        File solutionsDir = new File(testCaseDir, "solutions");

        if (!solutionsDir.exists()) {
            solutionsDir.mkdir(); // Create folder if it doesn't exist
        }

        File outputFile = new File(solutionsDir, new File(outputPath).getName());

        try (PrintWriter writer = new PrintWriter(outputFile)) {
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    char cell = board[i][j];
                    writer.print(cell == '.' ? " " : cell);
                    writer.print(" ");
                }
                writer.println();
            }
            System.out.println("File berhasil disimpan di " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Gagal menyimpan solusi.");
        }
    }
}
