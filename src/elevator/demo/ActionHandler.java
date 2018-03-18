package elevator.demo;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Random;

public class ActionHandler implements Runnable {
    AnimationDemo master;
    ActionEvent e;
    public ActionHandler(AnimationDemo m, ActionEvent e){
        this.e = e;
        this.master = m;
    }
    private int freeze(int d, int dir){
        int min = 100;
        int ret = new Random().nextInt(master.e.length-1)+1;
        for(int i = 1; i < master.e.length; i++){
            try {
                if(d == 1)
                if (Math.abs(master.e[i].whereIs() - d) < min) {
                    if(dir == 1){
                        if(master.eC[i].upwards){
                            min = Math.abs((int) master.e[i].whereIs() - d);
                            ret = i;
                        }
                    }else{
                        if(!master.eC[i].upwards);
                            min = Math.abs((int) master.e[i].whereIs() - d);
                            ret = i;
                    }
                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }
        return ret;
    }
    //TODO: Shared variables may need mutal exclusion
    @Override
    public void run() {
        String[] params = e.getActionCommand().split(" ");
        StringBuilder sb = new StringBuilder();
        if(params[0].equalsIgnoreCase("f")) {
            //sb.append("command=" + e.getActionCommand());
            return;
        }else if(params[0].equalsIgnoreCase("p")) {
            master.ebs[Integer.parseInt(params[1])][Integer.parseInt(params[2])] = 1;
        }else if(params[0].equalsIgnoreCase("b")) {
            int floor = Integer.parseInt(params[1]);
            int dir = Integer.parseInt(params[2]);
            if(dir == 1){
                master.fbs[floor].setUp(true);
            }else{
                master.fbs[floor].setDown(true);
            }
            /*
            TODO: REWRITE TO SUPPORT DIFFERENT NUMBER OF ELEVATORS
             */
            sb.append(Arrays.toString(master.fbs));
            for(int i = 1; i < master.e.length; i++)
                master.eC[i].wake();

/*
                int toWake = freeze(floor, dir);
                //int toWake = 2;
                //REMEBER: no downward interrupt but reTake interrupt
                System.out.println(" --toWake = " + toWake);
                if (!master.eC[toWake].dreaming) {
                    //if(master.bm[floor].tryLock()){
                    System.out.println(" --tryLocked = " + toWake);
                    master.eC[toWake].addJob(floor);
                    //master.eC[toWake].reTake = true;
                        /*if(dir == 1) {
                            while(master.fbs[floor].isUp()){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }else{
                            while(master.fbs[floor].isDown()){
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }*/
                    //master.bm[floor].unlock();
                //}
                //}

        }
        master.out(sb);
        System.out.println("OK");
    }

}
