import java.awt.*;
import java.awt.image.BufferedImage;

class ImageOperations {

    /**
     * a method that gets rid of all red in the pixels
     *
     * @param img the buffered image to be editted
     * @return a image with no red in the pixels at all
     */
    static BufferedImage zeroRed(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                out.setRGB(x, y, new Color(0, c.getGreen(), c.getBlue()).getRGB());
            }
        }
        return out;
    }

    /**
     * A method that turns the image gray by applying the standard equation to each
     * individual pixel
     *
     * @param img the given image to do operations on
     * @return a greyed image
     */
    static BufferedImage grayscale(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                int g = (int) Math.round(0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue());
                out.setRGB(x, y, new Color(g, g, g).getRGB());
            }
        }
        return out;
    }

    /**
     * a method that inverts each individual pixel so it is the opposite color
     *
     * @param img the given image
     * @return a image with each of the pixels being inverted in color terms
     */
    static BufferedImage invert(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = new Color(img.getRGB(x, y));
                out.setRGB(x, y, new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()).getRGB());
            }
        }
        return out;
    }

    /**
     * A method that mirrors the image on either of the axis horizontal or vertical
     * by flipping down the middle of either axis
     *
     * @param img the given image to mirror
     * @param dir the direction to mirror the image in
     * @return the mirrored image
     */
    static BufferedImage mirror(BufferedImage img, MirrorMenuItem.MirrorDirection dir) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        if (dir == MirrorMenuItem.MirrorDirection.HORIZONTAL) {
            for (int y = 0; y < h / 2; y++) {
                for (int x = 0; x < w; x++) {
                    newImg.setRGB(x, y, img.getRGB(x, y));
                }
            }
            for (int y = h / 2; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    newImg.setRGB(x, y, img.getRGB(x, h - 1 - y));
                }
            }
        } else {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w / 2; x++) {
                    newImg.setRGB(x, y, img.getRGB(x, y));
                }
            }
            for (int y = 0; y < h; y++) {
                for (int x = w / 2; x < w; x++) {
                    newImg.setRGB(x, y, img.getRGB(w - 1 - x, y));
                }
            }
        }
        return newImg;
    }

    /**
     * a method that rotates the image in either the clockwise or counter clockwise
     * direction
     *
     * @param img the given image to rotate
     * @param dir the direction to rotate
     * @return the rotated image
     */
    static BufferedImage rotate(BufferedImage img, RotateMenuItem.RotateDirection dir) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);
        if (dir == RotateMenuItem.RotateDirection.CLOCKWISE) {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    newImg.setRGB(h - 1 - y, x, img.getRGB(x, y));
                }
            }
        } else {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    newImg.setRGB(y, w - 1 - x, img.getRGB(x, y));
                }
            }
        }
        return newImg;
    }

    /**
     * A method that repeats the image in either a vertical or horizontal axis
     * a specified number of times
     *
     * @param img the given image to repeat
     * @param n   the number of times to repeat
     * @param dir the direction to repeat it in
     * @return the repeated image
     */
    static BufferedImage repeat(BufferedImage img, int n, RepeatMenuItem.RepeatDirection dir) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg;
        if (dir == RepeatMenuItem.RepeatDirection.HORIZONTAL) {
            newImg = new BufferedImage(w * n, h, BufferedImage.TYPE_INT_RGB);
            for (int r = 0; r < n; r++)
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++)
                        newImg.setRGB(x + r * w, y, img.getRGB(x, y));
        } else {
            newImg = new BufferedImage(w, h * n, BufferedImage.TYPE_INT_RGB);
            for (int r = 0; r < n; r++)
                for (int y = 0; y < h; y++)
                    for (int x = 0; x < w; x++)
                        newImg.setRGB(x, y + r * h, img.getRGB(x, y));
        }
        return newImg;
    }

    /**
     * Zooms in on the image. The zoom factor increases in multiplicatives of 10% and
     * decreases in multiplicatives of 10%.
     *
     * @param img        the original image to zoom in on. The image cannot be already zoomed in
     *                   or out because then the image will be distorted.
     * @param zoomFactor The factor to zoom in by.
     * @return the zoomed in image.
     */
    static BufferedImage zoom(BufferedImage img, double zoomFactor) {
        int newImageWidth = (int) (img.getWidth() * zoomFactor);
        int newImageHeight = (int) (img.getHeight() * zoomFactor);
        BufferedImage newImg = new BufferedImage(newImageWidth, newImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, newImageWidth, newImageHeight, null);
        g2d.dispose();
        return newImg;
    }
}
