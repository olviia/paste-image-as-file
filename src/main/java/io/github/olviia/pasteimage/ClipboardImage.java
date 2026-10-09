package io.github.olviia.pasteimage;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Reads an image from the system clipboard and encodes it as PNG.
 * <p>
 * Only a clipboard that holds an image and no files counts: copied files must keep pasting
 * as files the way the IDE already does.
 */
final class ClipboardImage {

    private static final DataFlavor[] IMAGE_FILE_FLAVORS = {
            DataFlavor.javaFileListFlavor,
            DataFlavor.imageFlavor
    };

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
        BufferedImage image = readBufferedImage();
        if (image == null) return null;
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        ImageIO.write(image, "png", png);
        return png.toByteArray();
    }

    /** Replaces a clipboard image with the same image plus a local PNG file. */
    static File exposeAsFile(String name) throws IOException {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        BufferedImage image = readBufferedImage(clipboard);
        if (image == null) return null;

        Path directory = Files.createTempDirectory("paste-image-as-file-");
        Path path = directory.resolve(name);
        if (!ImageIO.write(image, "png", path.toFile())) {
            Files.deleteIfExists(path);
            Files.deleteIfExists(directory);
            throw new IOException("No PNG writer is available");
        }

        File file = path.toFile();
        file.deleteOnExit();
        directory.toFile().deleteOnExit();
        try {
            clipboard.setContents(new ImageFileTransferable(image, file), null);
        } catch (IllegalStateException e) {
            Files.deleteIfExists(path);
            Files.deleteIfExists(directory);
            return null;
        }
        return file;
    }

    private static BufferedImage readBufferedImage() {
        return readBufferedImage(Toolkit.getDefaultToolkit().getSystemClipboard());
    }

    private static BufferedImage readBufferedImage(Clipboard clipboard) {
        Image image;
        try {
            image = (Image) clipboard.getData(DataFlavor.imageFlavor);
        } catch (Exception e) {
            return null;
        }
        if (image == null) return null;
        return toBuffered(image);
    }

    private static BufferedImage toBuffered(Image image) {
        if (image instanceof BufferedImage b) return b;
        BufferedImage b = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = b.createGraphics();
        try {
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return b;
    }

    private static final class ImageFileTransferable implements Transferable {

        private final BufferedImage image;
        private final List<File> files;

        private ImageFileTransferable(BufferedImage image, File file) {
            this.image = image;
            files = List.of(file);
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return IMAGE_FILE_FLAVORS.clone();
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.javaFileListFlavor.equals(flavor) || DataFlavor.imageFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (DataFlavor.javaFileListFlavor.equals(flavor)) return files;
            if (DataFlavor.imageFlavor.equals(flavor)) return image;
            throw new UnsupportedFlavorException(flavor);
        }
    }
}
