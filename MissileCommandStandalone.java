import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;

/**
 * Standalone launcher for MissileCommandApplet (originally a 1997 browser applet).
 *
 * Overrides getDocumentBase() to return the current working directory as a
 * file:// URL, so the existing getImage(getDocumentBase(), "Base.gif") calls
 * in MissileCommandApplet.init() find the GIFs without any changes to the
 * original source.
 *
 * Run from the project root (where the .gif files live):
 *   java -cp . MissileCommandStandalone
 */
public class MissileCommandStandalone extends v2.MissileCommandApplet {

    private static Frame frame;

    @Override
    public void startGame() {
        // The canvas has zero size until the frame is validated. Base/City constructors
        // call getGraphics().create(...) to get their drawing contexts — those contexts
        // will have an empty clip if we don't force layout first.
        if (frame != null) frame.validate();
        super.startGame();
    }

    @Override
    public Image getImage(URL base, String name) {
        try {
            return Toolkit.getDefaultToolkit().getImage(new URL(base, name));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot load image: " + name, e);
        }
    }

    @Override
    public URL getDocumentBase() {
        try {
            return new File(System.getProperty("user.dir") + File.separator).toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cannot form file URL for working directory", e);
        }
    }

    public static void main(String[] args) {
        MissileCommandStandalone applet = new MissileCommandStandalone();

        frame = new Frame("Missile Command (1997)");
        frame.setSize(540, 490);
        frame.setResizable(false);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                applet.stop();
                frame.dispose();
                System.exit(0);
            }
        });

        frame.add(applet);
        frame.setVisible(true);  // native peers created here — must precede init()
        applet.init();
        applet.start();
        frame.validate();        // re-layout: init() added child components while frame was already visible
    }
}
