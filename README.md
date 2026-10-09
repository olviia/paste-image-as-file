# Paste Image as File

A JetBrains IDE plugin (Rider, IntelliJ IDEA, PyCharm, …): paste a screenshot or copied image
straight into a folder of your project.

Select a folder (or a file in it) in the Project / Explorer tree and press **Ctrl+V** (**Cmd+V** on
macOS). The clipboard image is saved there as `screenshot-2026-10-04-153012.png` (`-2`, `-3`, … if
the name is taken).
No more saving the image somewhere, finding it, and dragging it into the project.

- Only acts when the clipboard holds an image; pasting files or text works as before.
- In Remote Development, the client exposes the image as a temporary local PNG and lets the IDE's
  native file paste action upload it to the selected remote folder.
- Works in Rider's Explorer views (tested on Rider 2026.2.2, Unity project), and in any IDE tree that
  knows its selected file.
- In a Unity project the PNG lands in `Assets/…` and Unity imports it as usual.

Existing image-paste plugins only paste into Markdown files; this one pastes into the file tree.

## Install

Download the zip from Releases, then *Settings → Plugins → ⚙ → Install Plugin from Disk…*, restart.

## Build

Requires JDK 21+ (the JetBrains Runtime bundled with your IDE works).

```
./gradlew test buildPlugin
```

## See also

- [Clean Terminal Copy](https://github.com/olviia/clean-terminal-copy): copy from the terminal without the margin and wrap breaks.
- [Clickable Terminal Prompt](https://github.com/olviia/clickable-terminal-prompt): click to move the caret in a terminal prompt.

## License

MIT
