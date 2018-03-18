package elevator.demo;

import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.Arrays;

public class ActionHandler implements Runnable {
    AnimationDemo master;
    ActionEvent e;
    public ActionHandler(AnimationDemo m, ActionEvent e){
        this.e = e;
        this.master = m;
    }
    private int freeze(int d){
        double[] dis = new double[master.e.length];
        int min = 100;
        int ret = 2;
        for(int i = 1; i <= master.e.length; i++){
            try {
                if(master.eC[i].upwards) {
                    if(d == 1)
                    if (Math.abs(master.e[i].whereIs() - d) < min) {
                        min = Math.abs((int) master.e[i].whereIs() - d);
                        ret = i;
                    }
                }else{

                }
            } catch (RemoteException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        }
        return ret;
    }
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
            if((Integer.parseInt(params[2])) == 1){
                master.fbs[Integer.parseInt(params[1])].setUp(true);
            }else{
                master.fbs[Integer.parseInt(params[1])].setDown(true);
            }
            /*
            TODO: REWRITE TO SUPPORT DIFFERENT NUMBER OF ELEVATORS
             */
            if(master.eC[1].dreaming){
                master.eC[1].wake();
            }else if(master.eC[2].dreaming){
                master.eC[2].wake();
            }else if(master.eC[3].dreaming){
                master.eC[3].wake();
            }
            int toWake = freeze(Integer.parseInt(params[1]));
            //REMEBER: no downward interrupt but reTake interrupt
            if(!master.eC[toWake].job && !master.eC[toWake].dreaming){
                master.eC[toWake].jobs.add(Integer.parseInt(params[2]));
                master.eC[toWake].reTake = true;
            }
            sb.append(Arrays.toString(master.fbs));
        }
        master.out(sb);
    }

}
