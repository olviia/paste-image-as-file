package io.github.olviia.pasteimage;

import com.intellij.ide.AppLifecycleListener;
import com.intellij.ide.DataManager;
import com.intellij.ide.IdeEventQueue;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Turns Ctrl+V (or Shift+Insert) in a project tree into "save the clipboard image as a PNG in
 * the selected folder", when the clipboard holds an image.
 * <p>
 * Watches keys rather than the Paste action, because a file tree's own paste only knows files
 * and text and has no hook for images. Any other paste passes through untouched.
 */
public final class PasteImageWatcher implements AppLifecycleListener {

    private static final Logger LOG = Logger.getInstance(PasteImageWatcher.class);

    private static final int MODIFIERS = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK
            | InputEvent.ALT_DOWN_MASK | InputEvent.META_DOWN_MASK;

    /** Set after a consumed paste press whose KEY_TYPED twin must be dropped too (UI thread only). */
    private static boolean swallowTyped;

    @Override
    public void appFrameCreated(@NotNull List<String> commandLineArgs) {
        IdeEventQueue.getInstance().addDispatcher(PasteImageWatcher::onEvent, ApplicationManager.getApplication());
    }

    /** Returns true when the key was used up as an image paste. */
    private static boolean onEvent(@NotNull AWTEvent event) {
        if (!(event instanceof KeyEvent key)) return false;
        if (key.getID() == KeyEvent.KEY_TYPED && swallowTyped) {
            swallowTyped = false;
            return true;
        }
        if (key.getID() != KeyEvent.KEY_PRESSED || !isPasteKey(key)) return false;
        try {
            Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (!isInTree(focus) || !ClipboardImage.isAvailable()) return false;
            DataContext context = DataManager.getInstance().getDataContext(focus);
            Project project = CommonDataKeys.PROJECT.getData(context);
            VirtualFile folder = targetFolder(context);
            LOG.debug("paste key in " + focus.getClass().getName() + " project=" + project + " folder=" + folder);
            if (project == null || folder == null) return false;
            pasteInto(project, folder);
            swallowTyped = true;
            return true;
        } catch (RuntimeException e) {
            LOG.debug("paste failed: " + e);
            return false;
        }
    }

    /** Saves the clipboard image as a new PNG in {@code folder} and says so in the status bar. */
    private static void pasteInto(Project project, VirtualFile folder) {
        byte[] png;
        try {
            png = ClipboardImage.asPng();
        } catch (IOException e) {
            LOG.debug("could not encode clipboard image: " + e);
            return;
        }
        if (png == null) return;
        String name = ImageFileNames.forPaste(LocalDateTime.now(), n -> folder.findChild(n) != null);
        WriteCommandAction.runWriteCommandAction(project, "Paste Image as File", null, () -> {
            try {
                VirtualFile file = folder.createChildData(PasteImageWatcher.class, name);
                file.setBinaryContent(png);
            } catch (IOException e) {
                LOG.debug("could not write " + name + ": " + e);
            }
        });
        var statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) statusBar.setInfo("Pasted image as " + name);
    }

    /** The selected folder, or the folder of the selected file; null when nothing is selected. */
    private static VirtualFile targetFolder(DataContext context) {
        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(context);
        if (file == null) {
            VirtualFile[] files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(context);
            if (files != null && files.length > 0) file = files[0];
        }
        if (file == null || !file.isValid()) return null;
        return file.isDirectory() ? file : file.getParent();
    }

    private static boolean isPasteKey(KeyEvent key) {
        int mods = key.getModifiersEx() & MODIFIERS;
        return key.getKeyCode() == KeyEvent.VK_V && mods == InputEvent.CTRL_DOWN_MASK
                || key.getKeyCode() == KeyEvent.VK_INSERT && mods == InputEvent.SHIFT_DOWN_MASK;
    }

    private static boolean isInTree(Component component) {
        return component instanceof JTree || SwingUtilities.getAncestorOfClass(JTree.class, component) != null;
    }
}
