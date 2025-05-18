import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Stack;

class ImageEditor extends JPanel {

    private final Stack<BufferedImage> UNDO_STACK;
    private final Stack<BufferedImage> REDO_STACK;
    private final JPanel IMAGE_PANEL;
    private final JMenuBar MENU_BAR;
    private final JScrollPane SCROLL_PANE;
    private final ShortcutKeyMap SHORTCUT_KEY_MAP;
    private final ZoomMouseEventListener ZOOM_LISTENER;
    private int zoomImageIndex;

    ImageEditor() {
        this.UNDO_STACK = new Stack<>();
        this.REDO_STACK = new Stack<>();
        this.SHORTCUT_KEY_MAP = new ShortcutKeyMap(this);
        this.IMAGE_PANEL = new ImagePanel(this);
        this.SCROLL_PANE = new JScrollPane(this.IMAGE_PANEL, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.MENU_BAR = new MenuBar(this);
        this.ZOOM_LISTENER = new ZoomMouseEventListener(this, this.IMAGE_PANEL);
        this.zoomImageIndex = 0;
        this.setLayout(new BorderLayout());
        this.add(this.MENU_BAR, BorderLayout.NORTH);
        this.add(this.SCROLL_PANE, BorderLayout.CENTER);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.IMAGE_PANEL.repaint();
    }

    @Override
    public void revalidate() {
        super.revalidate();
        if (this.IMAGE_PANEL != null) {
            this.IMAGE_PANEL.revalidate();
        }
    }

    /**
     * a method that reads each individual pixel of the image and its RGB values
     * then adds the pixels to a buffer image creator
     *
     * @param in the given file name/ppm file name to read from
     */
    void readPpmImage(String in) {
        try (BufferedReader br = new BufferedReader(new FileReader(in))) {
            String line = br.readLine();
            while (line != null && line.startsWith("#")) {
                line = br.readLine();
            }
            if (line == null || !line.equals("P3")) {
                throw new IllegalArgumentException("Only PPM files are supported");
            }
            Scanner current = new Scanner(br);
            int width = current.nextInt();
            int height = current.nextInt();
            int max = current.nextInt();
            if (max != 255)
                throw new IllegalArgumentException("Unsupported max value " + max);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int x = 0;
            int y = 0;
            while (current.hasNextInt()) {
                int r = current.nextInt();
                int g = current.nextInt();
                int b = current.nextInt();
                img.setRGB(x, y, new Color(r, g, b).getRGB());
                x++;
                if (x == width) {
                    x = 0;
                    y++;
                }
            }
            current.close();
            UNDO_STACK.clear();
            REDO_STACK.clear();
            zoomImageIndex = 0;
            addImage(img);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * a method writes the image and outputs it into the image box in the
     * photo editor
     *
     * @param out the given string of file to write to
     */
    void writePpmImage(String out) {
        try (PrintWriter pw = new PrintWriter(out)) {
            BufferedImage img = this.getImage();
            if (img == null) {
                throw new IOException("No image loaded");
            }
            int w = img.getWidth();
            int h = img.getHeight();
            pw.println("P3");
            pw.println(w + " " + h);
            pw.println("255");
            for (int y = 0; y < h; y++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < w; x++) {
                    Color c = new Color(img.getRGB(x, y));
                    row.append(c.getRed()).append(' ').append(c.getGreen()).append(' ').append(c.getBlue()).append(' ');
                }
                row.setLength(row.length() - 1);
                pw.println(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a new image to the editor and the undo stack. It is assumed that the image
     * being passed is not zoomed. If so, use the other addImage method.
     *
     * @param img image to add.
     */
    void addImage(BufferedImage img) {
        this.UNDO_STACK.push(img);
        this.REDO_STACK.clear();
        this.revalidate();
        this.repaint();
        this.zoomImageIndex++;
    }

    /**
     * Adds a new zoomed image to the editor. Because we only want to apply transformations
     * to non-zoomed images, we need to keep track of where the last non-zoomed image is in
     * the undo stack.
     *
     * @param img    image to add.
     * @param zoomed flag indicating whether the image is zoomed. This is always true.
     */
    void addImage(BufferedImage img, boolean zoomed) {
        this.UNDO_STACK.push(img);
        this.REDO_STACK.clear();
        this.revalidate();
        this.repaint();
        if (!zoomed) {
            this.zoomImageIndex++;
        }
    }

    /**
     * Removes the current image from the editor and the undo stack.
     * The undone image is pushed to the redo stack. If there are no images
     * to undo, this method does nothing.
     */
    void undoImage() {
        if (!this.UNDO_STACK.isEmpty()) {
            this.REDO_STACK.push(this.UNDO_STACK.pop());
            this.revalidate();
            this.repaint();
        }
    }

    /**
     * Redoes the last undone image. The redone image is pushed to the undo stack.
     * If there are no images to redo, this method does nothing.
     */
    void redoImage() {
        if (!this.REDO_STACK.isEmpty()) {
            this.UNDO_STACK.push(this.REDO_STACK.pop());
            this.revalidate();
            this.repaint();
        }
    }

    Stack<BufferedImage> getUndoStack() {
        return this.UNDO_STACK;
    }

    Stack<BufferedImage> getRedoStack() {
        return this.REDO_STACK;
    }

    BufferedImage getImage() {
        return this.UNDO_STACK.isEmpty() ? null : this.UNDO_STACK.peek();
    }

    BufferedImage getOriginalImage() {
        if (this.zoomImageIndex < 1 || this.zoomImageIndex >= this.UNDO_STACK.size()) {
            return null;
        } else {
            return this.UNDO_STACK.elementAt(this.zoomImageIndex - 1);
        }
    }

    MenuBar getMenuBar() {
        return (MenuBar) MENU_BAR;
    }

    JScrollPane getScrollPane() {
        return this.SCROLL_PANE;
    }

    ZoomMouseEventListener getZoomListener() {
        return this.ZOOM_LISTENER;
    }
}
