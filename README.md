# MathNotes LibreOffice Addon

LibreOffice addon for creating math notes, designed to support pupils with dysgraphia .

## License

This project is licensed under the GNU General Public License (GPL). See [https://www.gnu.org/licenses/gpl-3.0.html](https://www.gnu.org/licenses/gpl-3.0.html) for details.

## Requirements

- Java 17
- Gradle (or use the included `./gradlew` wrapper)
- LibreOffice with Java support enabled

## Build

```bash
./gradlew build
```

The `.oxt` extension package will be created at `build/dist/mathnotes-addon-1.0.0.oxt`.

## Install

**Option 1 — Gradle task:**

```bash
./gradlew installExtension
```

**Option 2 — Manual:**

```bash
unopkg add --force build/dist/mathnotes-addon-1.0.0.oxt
```
