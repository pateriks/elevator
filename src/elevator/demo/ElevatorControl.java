package elevator.demo;

import elevator.rmi.Elevator;
import java.rmi.RemoteException;
import java.util.TreeSet;

public class ElevatorControl extends Thread {
    AnimationDemo master;
    Elevator elevator;
    int id;
    boolean dreaming = false;
    public TreeSet<Integer> jobs = new TreeSet<>();
    public boolean job = true;
    boolean upwards = true;
    boolean reTake = false;

    public ElevatorControl(AnimationDemo m, Elevator e, int id){
        this.master = m;
        this.elevator = e;
        this.id = id;
    }

    public synchronized void wake(){
        if(!dreaming && !job) {
            job = true;
            return;
        }
        notifyAll();
    }

    public synchronized void dream(){
        dreaming = true;
        try{
            wait();
            dreaming = false;
        }catch (InterruptedException e){
            dreaming = false;
        }
    }

    public int work(){
        int work = 0;
        try {
            double where = elevator.whereIs();
            for(int btn : master.ebs[id]){
                work += btn;
            }
        } catch (RemoteException e) {
            printNexit(e);
        }
        return work;
    }

    private void take(int level, int dir){

        try {
            if(master.bm[level].tryLock()) {
                StringBuilder sb = new StringBuilder();
                wake();
                master.out(sb);
            }
        } catch (Exception e) {

        }

    }

    private void take(){
        System.err.println(id + " woke ");
        for(int i = 0; i < master.fbs.length; i++){
            if(master.fbs[i].isPushed()){
                try {
                    if (master.bm[i].tryLock()) {
                        job = true;
                        jobs.add(i);
                    }
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder(e.getMessage());
                    master.out(sb);
                }
            }
        }
    }

    private void printNexit(Exception e){
        StringBuilder sb = new StringBuilder(e.getMessage());
        AnimationDemo.nl(sb);
        sb.append("Fatal Error : System Exits");
        master.out(sb);
        System.exit(1);
    }

    @Override
    public void run() {
        dream(); //wait for master
        jobs.add(0); //default job
        while(true) {
            while (job) {
                try {
                    int todo = 0;
                    ///get appropriate job
                    try {
                        if(jobs.size() == 1){
                            throw new NullPointerException();
                        }
                        if(upwards) {
                            todo = jobs.higher((int) Math.round(elevator.whereIs()));
                        }else{
                            todo = jobs.lower((int) Math.round(elevator.whereIs()));
                        }
                    } catch (NullPointerException e) { //no jobs in current direction change direction (down has always job 0)
                        if(upwards){
                            upwards = false;
                        }else{
                            upwards = true;
                        }
                    }
                    //double check direction
                    double where = elevator.whereIs();
                    if (todo > where) {
                        if(upwards) {
                            elevator.up();
                        }else{
                            System.exit(1);
                        }
                    } else {
                        if(!upwards) {
                            elevator.down();
                        }else{
                            System.exit(1);
                        }
                    }
                    //move elevator
                    do {
                        where = elevator.whereIs();
                        elevator.setScalePosition((int) Math.round(elevator.whereIs()));
                    } while (Math.abs(where - todo) > 0.001 && !reTake);

                    if(reTake){
                        jobs.add(todo);
                        reTake = false;
                        continue;
                    }

                    elevator.stop();
                    //open doors
                    elevator.open();
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    elevator.close();

                    //clear floor buttons TODO: make routine in master
                    master.fbs[todo].setUp(false);
                    master.fbs[todo].setDown(false);

                    //release lock if has lock TODO: should not need to check
                    if(master.bm[todo].tryLock()) {
                        master.bm[todo].unlock();
                    }

                    //remove job if job is not job 0
                    //begin to dream (i.e. set job to false) if job was 0 and there is no more jobs
                    if(todo != 0) {
                        jobs.remove(todo);
                    }else{
                        if(jobs.size() == 1)
                            job = false;
                    }

                } catch (RemoteException e) {
                    printNexit(e);
                }
            }
            if(!job)
                upwards = true;
                dream();
            //job = false; //job was true to break elevator from going to default position
            take();
        }

    }
}

/*
            try {
                double where;
                where = elevator.whereIs();
                if(where > 0.0) {
                    elevator.down();
                    upwards = false;
                    do {
                        try {
                            elevator.setScalePosition((int)Math.round(where));
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while ((where = elevator.whereIs()) > 0.001 && !job);
                }
                elevator.stop();
                elevator.setScalePosition((int)Math.round(elevator.whereIs()));
            } catch (RemoteException e) {
                printNexit(e);
                */
