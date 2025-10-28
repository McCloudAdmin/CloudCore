package systems.mythical.cloudcore.utils;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {
    private static final Map<UUID, BufferedImage> headCache = new HashMap<>();
    private static final String[] COLORS = {
        "§0", // Black
        "§1", // Dark Blue
        "§2", // Dark Green
        "§3", // Dark Aqua
        "§4", // Dark Red
        "§5", // Dark Purple
        "§6", // Gold
        "§7", // Gray
        "§8", // Dark Gray
        "§9", // Blue
        "§a", // Green
        "§b", // Aqua
        "§c", // Red
        "§d", // Light Purple
        "§e", // Yellow
        "§f"  // White
    };

    private static final Color[] RGB_COLORS = {
        new Color(0, 0, 0),      // Black
        new Color(0, 0, 170),    // Dark Blue
        new Color(0, 170, 0),    // Dark Green
        new Color(0, 170, 170),  // Dark Aqua
        new Color(170, 0, 0),    // Dark Red
        new Color(170, 0, 170),  // Dark Purple
        new Color(255, 170, 0),  // Gold
        new Color(170, 170, 170),// Gray
        new Color(85, 85, 85),   // Dark Gray
        new Color(85, 85, 255),  // Blue
        new Color(85, 255, 85),  // Green
        new Color(85, 255, 255), // Aqua
        new Color(255, 85, 85),  // Red
        new Color(255, 85, 255), // Light Purple
        new Color(255, 255, 85), // Yellow
        new Color(255, 255, 255) // White
    };

    public static BufferedImage fetchHead(UUID uuid, String name, File cacheDir, Logger logger) {
        CloudLogger cloudLogger = CloudLoggerFactory.get();
        // Check memory cache first
        if (headCache.containsKey(uuid)) {
            return headCache.get(uuid);
        }

        // Check file cache
        File cacheFile = new File(cacheDir, uuid.toString() + ".png");
        if (cacheFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(cacheFile);
                if (img != null) {
                    headCache.put(uuid, img);
                    return img;
                }
            } catch (IOException ex) {
                cloudLogger.warn("Failed to read cached head for " + name + ": " + ex.getMessage());
            }
        }

        // Fetch from mc-heads.net if not cached
        String request = "https://mc-heads.net/avatar/" + name + "/32.png";
        try {
            @SuppressWarnings("deprecation")
            BufferedImage img = ImageIO.read(new URL(request));
            if (img != null) {
                // Save to memory cache
                headCache.put(uuid, img);
                
                // Ensure cache directory exists
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                
                // Save to file cache
                try {
                    ImageIO.write(img, "PNG", cacheFile);
                } catch (IOException ex) {
                    cloudLogger.warn("Failed to cache head for " + name + ": " + ex.getMessage());
                }
                return img;
            }
        } catch (Exception ex) {
            cloudLogger.warn("Head of player " + name + " could not be parsed! " + ex.getMessage());
        }
        return null;
    }

    public static String[][] toChatColorArray(BufferedImage image, int height) {
        double ratio = (double) image.getHeight() / image.getWidth();
        int width = (int) (height / ratio);
        
        BufferedImage resized = resizeImage(image, width, height);
        String[][] chatImg = new String[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = resized.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                
                // Skip fully transparent pixels
                if (alpha < 128) {
                    chatImg[x][y] = null;
                    continue;
                }

                Color pixelColor = new Color(rgb, true);
                chatImg[x][y] = getClosestChatColor(pixelColor);
            }
        }
        return chatImg;
    }

    public static String[] toImgMessage(String[][] colors, char imgChar) {
        String[] lines = new String[colors[0].length];
        
        for (int y = 0; y < colors[0].length; y++) {
            StringBuilder line = new StringBuilder();
            String lastColor = null;
            
            for (int x = 0; x < colors.length; x++) {
                String color = colors[x][y];
                
                // Handle transparent pixels
                if (color == null) {
                    line.append(" ");  // Use space for transparency
                    continue;
                }
                
                // Only add color code if it's different from the last one
                if (!color.equals(lastColor)) {
                    line.append(color);
                    lastColor = color;
                }
                
                line.append(imgChar);
            }
            
            // Reset color at end of line
            lines[y] = line.append("§r").toString();
        }
        return lines;
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        
        // Enable better quality rendering
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Handle transparency properly
        g.setComposite(AlphaComposite.Src);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        
        return resized;
    }

    private static String getClosestChatColor(Color color) {
        if (color.getAlpha() < 128) return null;

        String closestColor = COLORS[15]; // Default to white
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < RGB_COLORS.length; i++) {
            double distance = getColorDistance(color, RGB_COLORS[i]);
            if (distance < minDistance) {
                minDistance = distance;
                closestColor = COLORS[i];
            }
        }

        return closestColor;
    }

    private static double getColorDistance(Color c1, Color c2) {
        double rmean = (c1.getRed() + c2.getRed()) / 2.0;
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        double b = c1.getBlue() - c2.getBlue();
        
        // Weighted RGB distance, giving more weight to red channel
        double weightR = 2 + rmean / 256.0;
        double weightG = 4.0;
        double weightB = 2 + (255 - rmean) / 256.0;
        
        return weightR * r * r + weightG * g * g + weightB * b * b;
    }
} 