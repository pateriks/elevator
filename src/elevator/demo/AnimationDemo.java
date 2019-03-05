package elevator.demo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import elevator.rmi.*;
/**
 * Title:        Green Elevator
 * Description:  Green Elevator, 2G1915
 * Copyright:    Copyright (c) 2001
 * Company:      IMIT/KTH
 * @author Vlad Vlassov
 * @version 1.0
 */

/**
 * Modified: 2018-03-27
 * Modifier: Patrik Eriksson
 * Changes: Added RMI supported multithread control
 */

/**
 * The class is a primary class of the AnimationDemo Java application which
 * illustrates a way of controling the Elevators application via Java RMI.
 * <p>The program generates a sequence of control commands submitted to the
 * <a href="../Elevators.html">elevator.Elevators</a> application via Java RMI.
 * The program assumes that the rmiregistry (and Elevators) runs on "localhost"
 * and there are at least 3 elevators and 4 floors.
 * It opens/closes the door of the 1-st elevator with the interval of 2 sec.,
 * then it starts all motors to move all elevators up for 2 sec., stops them,
 * and moves them down. After 2 sec, it starts moving the 3-rd elevator
 * up to the 4-th floor updating its scale indicator each 0.1 sec.
 *
 * <p>To run the application, first, start rmiregistry, then Elevators and, finally,
 * AnimationDemo. It's recommended to start Elevators and AnimationDemo in different
 * windows so that you can keep Elevators running when you restart the demo
 * or when you start your own control/animation program.
 * To see result of AnimationDemo, you should look at Elevators. You may also click on
 * buttons of Elevators: action commands of buttons should be printed to the standard
 * output of AnimationDemo.You may also control the Elevators, when necessary,
 * via standard input. Try, for example, "m 0 1" (move all up), "m 0 -1" (move
 * all down), "d 3 1" (open the 3rd door), "s 3 0" (set the 3rd scale to 0).
 * <p>Starting both applications
 * you must indicate a path to <code>elevator.jar</code> with the <code>-classpath</code> option.
 * The JAR file is located under the <code>lib</code> directory of the Elevators home.
 * Starting Elevators you must also specify the <code>-Djava.security.policy</code>
 * property that is set to the <code>rmi.policy</code> file located
 * under the <code>lib</code> directory and the <code>-Djava.rmi.server.codebase</code>
 * property that poits to the <code>elevator.jar</code>.
 * For example (assume that the Elevators is located under "D:\home\vlad\edu\elevator\"),
 * <p><blockquote><pre>
 * C:\>start rmiregistry
 * C:\>java -classpath D:\home\vlad\edu\elevator\lib\elevator.jar -Djava.security.policy=D:\home\vlad\edu\elevator\lib\rmi.policy -Djava.rmi.server.codebase=file:d:\home\vlad\edu\elevator\lib\elevator.jar elevator.Elevators -top 5 -number 5 -rmi
 * C:\>java -classpath D:\home\vlad\edu\elevator\lib\elevator.jar elevator.demo.AnimationDemo
 * </pre></blockquote>
 * Where <code>rmi.security</code> is a text file that may specify the following permission:
 * <p><blockquote><pre>
 * grant {
 * // Allow everything for now
 * permission java.security.AllPermission;
 * };
 * </pre></blockquote>
 * The <code>elevator.jar</code> file is a JAR file that includes all resources
 * of the Elevators application including classes and interfaces needed to
 * develop a control program with Java RMI.
 * @see elevator.Elevators
 * @see elevator.rmi.MakeAll
 */
public class AnimationDemo extends Thread implements ActionListener{
  private int NE = 3;
  private int NF = 4;
  Motor motor;
  Door door;
  Scale scale;
  Elevators elevators;
  String rmihost;
  ElevatorControl[] eC;
  Elevator[] e;
  Scanner in = new Scanner(System.in);
  FloorButton[] fbs;
  int[][] ebs;
  ReentrantLock bm[];
  Lock[] fbls;
  /**
   * Creates an instace of <code>AnimationDemo</code> to run in a separate thread
   */
  public AnimationDemo(String[] args) {
    rmihost = (args.length > 0)? args[0] : "localhost";
  }
  /**
   * Runs the demo in a thread
   */
  public void run() {
    try {

      MakeAll.init(rmihost);
      MakeAll.addFloorListener(this);
      MakeAll.addInsideListener(this);
      MakeAll.addPositionListener(this);
      //MakeAll.addPositionListener(3, this);
      motor = MakeAll.getMotor(1);
      scale = MakeAll.getScale(1);
      door = MakeAll.getDoor(1);
      elevators = MakeAll.getElevators();
      NE = MakeAll.getNumberOfElevators();
      NF = MakeAll.getNumberOfFloors();
      e = new Elevator[NE+1];
      eC = new ElevatorControl[NE+1];
      ebs = new int[NE+1][NF];
      fbs = new FloorButton[NF];
      bm = new ReentrantLock[NF];
      /* Start of Warmup */

        elevators.open();
        sleep(1000);
        elevators.close();
        sleep(1000);
        elevators.open();
        sleep(1000);
        elevators.close();
        elevators.up();
        sleep(1000);
        elevators.stop();
        sleep(1000);
        elevators.down();
        sleep(1000);

      /* End of Warmup */

      /* Iterate number of floors */
      for(int i = 0; i < NF; i++) {
        fbs[i] = new FloorButton();
        bm[i] = new ReentrantLock();
      }
      /* Iterate number of elevators */
      for(int i = 1; i <= NE; i++) {
        e[i] = MakeAll.getElevator(i);
        eC[i] = new ElevatorControl(this, e[i], i);
        new Thread(eC[i]).start();

      }


      double where = e[3].whereIs();
      System.out.println(where);
      if(where == 0) {
        e[3].up();
        do {
          e[3].setScalePosition((int)where);
          sleep(scale.getScalePosition());
        } while ((where = e[3].whereIs()) < 1.999);
      }else{
        e[3].down();
        do {
          e[3].setScalePosition((int)where);
          sleep(scale.getScalePosition());
        } while ((where = e[3].whereIs()) > 0.001);
      }
      e[3].stop();
      e[3].setScalePosition((int)Math.round(e[3].whereIs()));
      e[3].open();
      sleep(3000);
      e[3].close();
      for(int i = 1; i <= NE; i++){
          eC[i].wake();
      }
      exitMSG();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(0);
    }
    System.exit(1);
  }
  /**
   * The entry point of the <code>AnimationDemo</code> application. Create
   * a <code>AnimationDemo</code> thread and start it.
   */
  public static void main(String[] args) {
    (new AnimationDemo(args)).start();
  }
  /**
   * Invoked when any button on the Elevators is pressed. Prints an action command
   * assigned to the button.
   *
   * f = position
   * v = velocity
   * b = floor buttons
   * p = passenger buttons
   */
  public void actionPerformed(ActionEvent e) {
    new ActionHandler(this, e).run();
  }

    /**
     * Makes printing to standard out with mutal exclusion
     * @param out
     */
  public synchronized void out (StringBuilder out){
    String [] printArray = out.toString().split("\n");
    for(String print : printArray){
      System.out.println(print);
    }
  }

    /**
     * Makes a new line to the
     * StringBuilder
     * @param sb
     */
  public static void nl(StringBuilder sb){
    sb.append("\n");
  }


  private void exitMSG() throws RemoteException {
    new Scanner(System.in).nextLine();
    StringBuilder sb = new StringBuilder();
    for(int i = 1; i <= NE; i++){
      sb.append("Elevator " + i + " current position: ");
      sb.append(elevators.whereIs(i));
      nl(sb);
    }
    sb.append("Bye Bye");
    nl(sb);
    sb.append("See You Again Soon");
    nl(sb);
    out(sb);
  }
}
