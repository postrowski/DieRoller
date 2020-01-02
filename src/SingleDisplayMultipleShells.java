

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.ostrowski.DieShell;

public class SingleDisplayMultipleShells
{
  public SingleDisplayMultipleShells()
  {
     // ======================================================
     // Create the main Display object that represents the UI
     // subsystem and contains the single UI handling thread
     // ======================================================
     final Display display = Display.getDefault();

     // ====================================================
     // create a shell for the main window from the Display
     // ====================================================
     final Shell mainWindowShell = new Shell(display, SWT.CLOSE);

     // =====================
     // Set the Window Title
     // =====================
     mainWindowShell.setText("Main Shell");

     // =========================================
     // Create a button that spawns die Shells
     // =========================================
     Button spawn = new Button(mainWindowShell, SWT.PUSH);
     spawn.setText("Roll Die");
     spawn.setBounds(10, 10, 150, 30);
     spawn.addSelectionListener(new SelectionListener() {
        @Override
        public void widgetSelected(SelectionEvent e) {
           // =====================================================
           // on button press, create a die Shell object passing
           // the main Display. The shell could also access the
           // display itself by calling Display.getDefault()
           // =====================================================
           System.out.println("Main Shell handling Button press, about to create die Shell");

           Integer RGBcolor = 0x3030FF;
           @SuppressWarnings("unused")
           DieShell shell = new DieShell(display, "d6=2", 100/.577350f, RGBcolor);
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {
           widgetSelected(e);
        }
     });

     // =============================================================
     // Register a listener for the Close event on the main Shell.
     // This disposes the Display which will cause the entire die
     // tree to dispose
     // =============================================================
     mainWindowShell.addListener(SWT.Close, event -> {
        System.out.println("Main Shell handling Close event, about to dipose the main Display");
        display.dispose();
     });

     // ================================
     // Set size on main Shell and open
     // ================================
     mainWindowShell.setSize(200, 200);
     mainWindowShell.open();

     // =====================================
     // Main UI event dispatch loop
     // that handles all UI events from all
     // SWT components created as children of
     // the main Display object
     // =====================================
     while (!display.isDisposed()) {
        // ===================================================
        // Wrap each event dispatch in an exception handler
        // so that if any event causes an exception it does
        // not break the main UI loop
        // ===================================================
        try {
           if (!display.readAndDispatch()) {
              display.sleep();
           }
        }
        catch (Exception e) {
           e.printStackTrace();
        }
     }

     System.out.println("Main Display event handler loop has exited");
  }

  public static void main(String[] args) {
     SingleDisplayMultipleShells a = new SingleDisplayMultipleShells();
     a.getClass();
  }

}
