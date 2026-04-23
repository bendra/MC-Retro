# Missile Command (1997)

A Java implementation of the classic Missile Command arcade game, originally written
in 1997 by Benjamin Drasin (me) as a browser applet. Modernized with a thin standalone
launcher and minimal changes to make it compile and run on a current JVM.

## Historical Context

Written in 1997 using Java 1.1 and AWT 1.0 in IBM VisualAge for Java (the IDE comments
reading "This method was created by a SmartGuide" are its fingerprints). Deployed as a
browser applet in the era of Netscape Navigator 4, published on the web (originally 
Geocities), also featured on some applet gallaries. It won some web awards, and got 
enough attention that I felt encouraged to pursue a career as a software engineer.

Some code looks odd to a modern eye, but I'm rather proud of it. I think it represents
a fairly sophisticated design for its time: a reusable game framework separated into
its own package `VideoGame/` with Observer pattern, interface-driven abstractions, and
level manager state machine, all built by a self-taught programmer before the Java
Collections framework even existed. Architecture heavily influinced by ideas which I
encountered in the then-new Gang of Four *Design Patterns* book, applied in a new
language that was itself less than two years old.

The `archive/` directory contains the original 1997 source archive and the 1999 compiled
deployment packages (see Structure below).

## Requirements

**Java 11–21** (Java 22+ removed `java.applet.Applet`, which the original code uses).
Java 21 LTS is recommended.

On macOS with Homebrew:
```
brew install openjdk@21
export JAVA_HOME=$(brew --prefix openjdk@21)
```

## Build and Run

```
make run
```

Or manually:
```
javac --release 11 -sourcepath VideoGame:v2:. VideoGame/*.java v2/*.java MissileCommandStandalone.java
java -cp . MissileCommandStandalone
```

Run from the project root — the game loads `Base.gif`, `City.gif`, `DeadBase.gif`, and
`DeadCity.gif` from the working directory.

## Controls

| Input | Action |
|-------|--------|
| Click in game area | Fire defensive missile at that point |
| `z` | Select Alpha base (left) |
| `x` | Select Delta base (center) |
| `c` | Select Omega base (right) |
| Start Game button | Begin a new game |

Each base has 10 missiles. Defend your cities — if all 6 are destroyed, it's game over.
Difficulty increases each level; smart evasive missiles appear on alternating levels.

## Structure

```
VideoGame/    original 1997 framework (22 files)
v2/           original 1997 game logic (20 files)
*.gif         sprite images
archive/
  Source.zip          original source archive (1997)
  missilecommand.zip  compiled Netscape/jar deployment (1999)
  videogame.cab       compiled Internet Explorer deployment (1999)
MissileCommandStandalone.java   standalone launcher (new)
Makefile
```

## Changes from the 1997 original

The goal was to preserve the original source as faithfully as possible. Two things
required changes, and one new file was added.

### `VideoGame/DynamicManager.java` — pause/resume threading

The original used `Thread.suspend()` and `Thread.resume()` to pause the game loop
when the mouse left the window and resume it when the mouse returned. These methods
were deprecated in Java 1.2 (1998) because they can cause deadlocks if the suspended
thread holds a monitor lock, and in Java 19 they were changed to throw
`UnsupportedOperationException` outright.

The replacement uses a `volatile boolean _paused` flag with Java's `wait()`/`notifyAll()`
mechanism: `pauseGame()` sets the flag, `resumeGame()` clears it and wakes the thread,
and the game loop checks the flag at the top of each iteration before doing any work.
The behavior from the player's perspective is identical.

### `MissileCommandStandalone.java` — new standalone launcher

This is the only entirely new file. It subclasses `MissileCommandApplet` and handles
three things the browser used to provide:

1. **Image loading** — The original calls `getImage(getDocumentBase(), "Base.gif")`.
   In a browser, `getDocumentBase()` returns the URL of the HTML page, and `getImage()`
   uses the applet's browser context to load the image. Standalone, there is no browser
   context. The fix overrides `getImage()` to load via `Toolkit.getDefaultToolkit().getImage()`,
   and overrides `getDocumentBase()` to return the current working directory as a
   `file://` URL. The original image-loading lines in `MissileCommandApplet` are untouched.

2. **Canvas sizing** — When running in a browser, the applet's canvas is sized by the
   HTML `<applet width=... height=...>` tag before `init()` is called. Standalone, the
   canvas starts at size zero. The bases and cities each capture a sub-region of the
   canvas's graphics context during `init()` for all their future drawing; if that
   context has a zero clip at capture time, nothing ever appears. The fix overrides
   `startGame()` (called from `init()` after the canvas is added to the window) to call
   `frame.validate()` first, forcing the layout engine to give the canvas its correct
   dimensions before the graphics contexts are captured.

3. **Window and lifecycle** — Creates an AWT `Frame`, wires up a window-close handler,
   and calls the applet's `init()` and `start()` lifecycle methods manually.

## Known Quirks

- Game pauses when mouse is not over game surface (design choice, perhaps questionable one)
- Slight rendering flicker is possible on modern hardware — the original used no double-buffering
