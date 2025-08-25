package pro.watchkit.flex;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D; // Import Path2D
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MyClass {
    public static void generateGradientImage(String filePath) throws IOException {
        int width = 480;
        int height = 480;

        // Create a BufferedImage object
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Get the Graphics2D object
        Graphics2D g2d = bufferedImage.createGraphics();

        // --- Improve rendering quality ---
        // g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Define the start and end colors for the gradient
        Color color1 = Color.RED;
        Color color2 = Color.BLUE;

        // Create a GradientPaint object
        GradientPaint gp = new GradientPaint(0, 0, color1, width, height, color2);

        // Set the paint to the GradientPaint
        g2d.setPaint(gp);

        // Fill the entire image with the gradient
        g2d.fillRect(0, 0, width, height);

        // --- Draw the hexagon using Path2D ---
        int centerX = width / 2;
        int centerY = height / 2;
        int hexagonRadius = 100; // Adjust size as needed
        Color hexagonColor = Color.GREEN; // Color of the hexagon

        Path2D.Double hexagonPath = createRegularHexagonPath(centerX, centerY, hexagonRadius);
        g2d.setColor(hexagonColor);
        g2d.fill(hexagonPath); // Use g2d.fill(Shape) for Path2D
        // --- End of hexagon drawing ---

        // Dispose of the Graphics2D object to free up resources
        g2d.dispose();

        // Create a File object for the output PNG file
        File outputFile = new File(filePath);

        // Write the BufferedImage to the PNG file
        ImageIO.write(bufferedImage, "png", outputFile);

        System.out.println("Gradient image with Path2D hexagon saved to: " + outputFile.getAbsolutePath());
    }

    // Helper function to create a regular hexagon Path2D.Double
    private static Path2D.Double createRegularHexagonPath(int centerX, int centerY, int radius) {
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3; // Angle in radians
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            if (i == 0) {
                path.moveTo(x, y); // Move to the first point
            } else {
                path.lineTo(x, y); // Draw a line to the subsequent points
            }
        }
        path.closePath(); // Connect the last point back to the first
        return path;
    }

    public static void main(String[] args) {
        try {
            // Specify the path where you want to save the PNG file
            String filePath = "gradient_image_with_path2d_hexagon.png";
            generateGradientImage(filePath);
        } catch (IOException e) {
            System.err.println("Error generating or saving image: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
