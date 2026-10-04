package io.github.olviia.pasteimage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * Names files for pasted images: {@code screenshot-2026-10-04-153012.png}, with "-2", "-3", …
 * when a file of that name already exists. Pure, so unit-testable.
 */
public final class ImageFileNames {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss");

    private ImageFileNames() {
    }

    /** A name for an image pasted at {@code time} that {@code taken} does not report as existing. */
    public static String forPaste(LocalDateTime time, Predicate<String> taken) {
        String base = "screenshot-" + STAMP.format(time);
        String name = base + ".png";
        for (int n = 2; taken.test(name); n++) name = base + "-" + n + ".png";
        return name;
    }
}
