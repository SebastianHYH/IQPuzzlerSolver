import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SaveAsImage {

    public static void save(char[][] board, int rows, int cols, String testCasePath, String filename) {
        int tileSize = 50;
        int width = cols * tileSize;
        int height = rows * tileSize;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char tile = board[r][c];
                if (tile != '.') {
                    graphics.setColor(Palette.awtFor(tile));
                    graphics.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);

                    graphics.setColor(Color.BLACK);
                    graphics.drawRect(c * tileSize, r * tileSize, tileSize, tileSize);
                    graphics.setColor(Color.WHITE);
                    graphics.drawString(String.valueOf(tile), c * tileSize + 20, r * tileSize + 30);
                }
            }
        }

        graphics.dispose();

        try {
            File testCaseDir = new File(testCasePath).getParentFile();
            File imagesDir = new File(testCaseDir, "images");
            if (!imagesDir.exists()) {
                imagesDir.mkdir();
            }

            File outputFile = new File(imagesDir, filename + ".png");
            ImageIO.write(image, "png", outputFile);
            System.out.println("Gambar berhasil disimpan di " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("Gagal menyimpan gambar");
        }
    }

}
