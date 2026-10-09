package io.github.olviia.pasteimage;

import org.junit.jupiter.api.Test;

import java.awt.Canvas;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasteImageWatcherTest {

    @Test
    void commandV_isPaste() {
        assertTrue(PasteImageWatcher.isPasteKey(key(KeyEvent.VK_V, InputEvent.META_DOWN_MASK)));
    }

    @Test
    void controlV_isPaste() {
        assertTrue(PasteImageWatcher.isPasteKey(key(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK)));
    }

    @Test
    void shiftInsert_isPaste() {
        assertTrue(PasteImageWatcher.isPasteKey(key(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK)));
    }

    @Test
    void modifiedPasteShortcut_isNotPaste() {
        assertFalse(PasteImageWatcher.isPasteKey(
                key(KeyEvent.VK_V, InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)));
    }

    private static KeyEvent key(int keyCode, int modifiers) {
        return new KeyEvent(new Canvas(), KeyEvent.KEY_PRESSED, 0, modifiers, keyCode, KeyEvent.CHAR_UNDEFINED);
    }
}
