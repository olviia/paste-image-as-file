package io.github.olviia.pasteimage;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Reads an image from the system clipboard and encodes it as PNG.
 * <p>
 * Only a clipboard that holds an image and no files counts: copied files must keep pasting
 * as files the way the IDE already does.
 */
final class ClipboardImage {

    private ClipboardImage() {
    }

    /** True when the clipboard holds an image (and no copied files). */
    static boolean isAvailable() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            return clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor)
                    && !clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor);
        } catch (IllegalStateException e) {
            return false; // clipboard busy in another application
        }
    }

    /** The clipboard image as PNG bytes, or null when there is none. */
    static byte[] asPng() throws IOException {
        Image image;
        try {
            image = (Image) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.imageFlavor);
        } catch (Exception e) {
            return null;
        }
        if (image == null) return null;
        BufferedImage buffered = toBuffered(image);
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(buffered, "png", png);
        return png.toByteArray();
    }

    private static BufferedImage toBuffered(Image image) {
        if (image instanceof BufferedImage b) return b;
        BufferedImage b = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        b.getGraphics().drawImage(image, 0, 0, null);
        return b;
    }
}
