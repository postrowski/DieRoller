package com.ostrowski.dieroller;

import org.eclipse.swt.widgets.Display;

import com.ostrowski.DieShell;

public class DieRoller {

   public static void main(String[] args) {
    Display display = new Display();
    Integer RGBcolor = 0x3030FF;
    DieShell shell = new DieShell(display, "d20=20", 50.0f, RGBcolor);
    while (!shell.shell.isDisposed()) {
       if (!display.readAndDispatch ()) {
          display.sleep ();
       }
    }
    display.dispose ();
 }

//   static final Map<Integer, ObjData> DIE_MAP = new HashMap<>();
//   static private BufferedImage TEXTURE = null;

//   static Canvas DOUBLE_BUFFERED_CANVAS;
//   public static void main(String[] args) {
//      loadDice();
//
//      Display display = new Display();
//      //Image image = display.getSystemImage(SWT.ICON_QUESTION);
//      Integer RGBcolor = 0x3030FF;
//      DieShell shell = new DieShell(display, "d20=20", 50.0f, RGBcolor);
////      //shell.setLayout (new GridLayout());
////      Button button = new Button(shell, SWT.PUSH);
////      button.setImage(image);
////      button.setText("Button");
////      button.addSelectionListener(new SelectionListener() {
////
////         @Override
////         public void widgetSelected(SelectionEvent e) {
//////            Display.getDefault().asyncExec(new Runnable(){
//////               @Override
//////               public void run() {
////
////                  final Display display = new Display();
////                  //Shell must be created with style SWT.NO_TRIM
////                  final Shell shell = new Shell(display, SWT.NO_TRIM);// | SWT.ON_TOP);
////
////                  shell.setLayout(new FillLayout());
////
////                  DOUBLE_BUFFERED_CANVAS = new Canvas(shell, SWT.NO_BACKGROUND);
////                  Integer RGBcolor = new Integer(0x3030FF);
////
////                  List<String> fakeArgs = Arrays.asList("d4=3","d20=14");
////                  String[] args = new String[fakeArgs.size()];
////                  for (int i=0 ; i<fakeArgs.size() ; i++) {
////                     args[i] = fakeArgs.get(i);
////                  }
////                  List<DieRoller> dieRollers = new ArrayList<>();
////                  for (String arg : args) {
////                     if (arg.startsWith("d")) {
////                        String sideStr = arg.substring(1);
////                        Integer results = null;
////                        int equalLoc = arg.indexOf("=");
////                        if (equalLoc != -1) {
////                           sideStr = arg.substring(1, equalLoc);
////                           String resultsStr = arg.substring(equalLoc + 1);
////                           if (resultsStr.equals("+")) {
////                              resultsStr = "12";
////                           }
////                           if (resultsStr.equals("-")) {
////                              resultsStr = "11";
////                           }
////                           results = Integer.parseInt(resultsStr);
////                        }
////                        Integer sides = Integer.parseInt(sideStr);
////                        DieRoller dieRoller = new DieRoller(sides, 50/*scale*/, shell, TEXTURE, results, RGBcolor);
////                        dieRollers.add(dieRoller);
////                        DOUBLE_BUFFERED_CANVAS.addPaintListener(dieRoller);
////                     }
////                  }
//////                DieRoller dieRoller = new DieRoller(6/*sides*/, 100/.577350f/*scale*/, shell, TEXTURE);
//////                DOUBLE_BUFFERED_CANVAS.addPaintListener(dieRoller);
////
////                  shell.setSize(100, 100);
////                  shell.open();
////                  for (DieRoller roller : dieRollers) {
////                     roller.start();
////                  }
////
////                  while (!shell.isDisposed()) {
////                     if (!display.readAndDispatch()) {
////                        display.sleep();
////                     }
////                  }
////                  display.dispose();
//////               }
//////            });
////         }
////
////         @Override
////         public void widgetDefaultSelected(SelectionEvent e) {
////         }
////      });
////      shell._shell.setSize(300, 300);
////      shell._shell.open();
//      while (!shell._shell.isDisposed ()) {
//         if (!display.readAndDispatch ()) {
//            display.sleep ();
//         }
//      }
//      display.dispose ();
//   }
//
//
//   static void loadDice() {
//      if (DIE_MAP.isEmpty()) {
//         try {
//            DIE_MAP.put( 4, ObjLoader.loadObj("res/dice/d4.obj"));
//            DIE_MAP.put( 6, ObjLoader.loadObj("res/dice/d6.obj"));
//            DIE_MAP.put( 8, ObjLoader.loadObj("res/dice/d8.obj"));
//            DIE_MAP.put(10, ObjLoader.loadObj("res/dice/d10.obj"));
//            DIE_MAP.put(12, ObjLoader.loadObj("res/dice/d12.obj"));
//            DIE_MAP.put(99, ObjLoader.loadObj("res/dice/d99.obj"));
//            DIE_MAP.put(20, ObjLoader.loadObj("res/dice/d20.obj"));
//            TEXTURE = ObjLoader.loadImage("res/dice/diceMapLettersOnBlack.png");
//         } catch (IOException e) {
//            e.printStackTrace();
//         }
//      }
//   }
}