package io.github.olviia.pasteimage;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageFileNamesTest {

    private static final LocalDateTime TIME = LocalDateTime.of(2026, 10, 4, 15, 30, 12);

    @Test
    void freeName_isTimestamped() {
        assertEquals("screenshot-2026-10-04-153012.png", ImageFileNames.forPaste(TIME, n -> false));
    }

    @Test
    void takenNames_getACounter() {
        Set<String> taken = Set.of("screenshot-2026-10-04-153012.png", "screenshot-2026-10-04-153012-2.png");
        assertEquals("screenshot-2026-10-04-153012-3.png", ImageFileNames.forPaste(TIME, taken::contains));
    }
}
