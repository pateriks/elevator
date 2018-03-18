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

    public synchronized void addJob(int job){
        reTake = true;
        jobs.add(job);
    }
    public synchronized void remJob(int job){
        jobs.remove(job);
    }

    private void take(){
        for(int i = 0; i < master.fbs.length; i++){
            if((master.fbs[i].isUp() && upwards) || (master.fbs[i].isDown() && !upwards) || (master.fbs[i].isPushed() && jobs.isEmpty())){
                try {
                    if(((i > elevator.whereIs()) && upwards) || ((i < elevator.whereIs()) && !upwards)) {
                        if (master.bm[i].tryLock()) {
                            job = true;
                            addJob(i);
                            reTake = false;
                        }
                    }
                } catch (Exception e) {
                    StringBuilder sb = new StringBuilder(e.getMessage());
                    master.out(sb);
                }
            }
        }
        for(int i = 0; i < master.ebs[0].length; i++){
            if(master.ebs[id][i] == 1){
                addJob(i);
            }
        }
    }

    private void printNexit(Exception e){
        StringBuilder sb = new StringBuilder(e.getMessage());
        AnimationDemo.nl(sb);
        System.err.println("EXIT ERROR");
        sb.append("Fatal Error : System Exits");
        master.out(sb);
        System.exit(1);
    }

    @Override
    public void run() {
        dream(); //wait for master
        while(true) {
            while (job) {
                try {
                    int todo = 0;
                    reTake = false;
                    ///get appropriate job
                    try {
                        if(jobs.isEmpty()){
                            System.out.println(id + " message\t" + "no jobs");
                            upwards = false;
                        }else if(upwards) {
                            todo = jobs.higher((int) Math.round(elevator.whereIs()));
                        }else{
                            todo = jobs.lower((int) Math.round(elevator.whereIs()));
                        }
                    } catch (NullPointerException e) { //no jobs in current direction change direction (down has always job 0)
                        upwards = false;
                        continue;
                    }
                    System.out.println(id + " message\t" + upwards);
                    //double check direction
                    double where = elevator.whereIs();
                    if (todo > where) {
                        if(upwards) {
                            elevator.up();
                        }else{
                            printNexit(new IllegalArgumentException("Direction Contradiction"));
                        }
                    } else {
                        if(!upwards) {
                            elevator.down();
                        }else{
                            printNexit(new IllegalArgumentException("Direction Contradiction"));
                        }
                    }
                    //move elevator
                    do {
                        where = elevator.whereIs();
                        elevator.setScalePosition((int) Math.round(elevator.whereIs()));
                        take();
                    } while (Math.abs(where - todo) > 0.001 && !reTake);

                    if(reTake){
                        System.out.println(id + "message\t" + "retake");
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
                    try{
                        master.bm[todo].unlock();
                    }catch (IllegalMonitorStateException e){

                    }

                    //remove job if job is not job 0
                    //set job to false (i.e. begin to dream) if job was 0 and there is no more jobs
                    if(todo != 0) {
                        remJob(todo);
                    }else{
                        upwards = true; //at floor zero can just go up
                        if(jobs.isEmpty())
                            job = false;
                    }

                } catch (RemoteException e) {
                    printNexit(e);
                }
                System.out.println(id + " message\t" + jobs.toString());
            }
            //if(!job)
            System.out.println(id + "message\t" + "dream: " + !job);
            upwards = true; //at floor zero can just go up
            if(!job)
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
