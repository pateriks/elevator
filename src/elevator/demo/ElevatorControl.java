/**
  * Author: Patrik Eriksson
  * Owner: Patrik Eriksson
  * Created: 2018-03-27
  */

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
    boolean reSchedule = false;

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

    public synchronized void addJob(int job){
        reSchedule = true;
        jobs.add(job);
    }
    public synchronized void remJob(int job){
        jobs.remove(job);
    }

    //TODO: Make elevator claim job if it is moving upward and the job is highest scheduled, easiest just allowed for highest floor
    private void claiming(){
        for(int i = 0; i < master.fbs.length; i++){
            if((master.fbs[i].isUp() && upwards) || (master.fbs[i].isDown() && !upwards) || (master.fbs[i].isPushed() && jobs.isEmpty())){
                try {
                    //second condition since whereIs needs to be as real as possible
                    if(((i > elevator.whereIs()) && upwards) || ((i < elevator.whereIs()) && !upwards)) {
                        if (master.bm[i].tryLock()) {
                            job = true;
                            if(!jobs.contains(i)) {
                                addJob(i);
                            }
                        }
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
        System.err.println("err");
        sb.append("Fatal Error : System Exits");
        master.out(sb);
        System.exit(1);
    }

    @Override
    public void run() {
        dream(); //wait on master
        while(true) {
            while (job) {
                claiming();
                try {
                    int todo = 0;
                    ///get appropriate job
                    try {
                        if(jobs.isEmpty()){
                            //System.out.println(id + " message\t" + "no jobs");
                            upwards = false;
                            //System.out.println(id + " message\t" + upwards);
                        }else if(upwards) {
                            todo = jobs.higher((int) Math.round(elevator.whereIs()));
                        }else{
                            todo = jobs.lower((int) Math.round(elevator.whereIs()));
                        }
                    } catch (NullPointerException e) { //no jobs in current direction change direction
                        if(upwards) {
                            upwards = false;
                            //System.out.println(id + " message\t" + upwards);
                        }else{
                            upwards = true;
                            //System.out.println(id + " message\t" + upwards);
                        }
                        claiming();
                        continue;
                    }
                    //System.out.println(id + " message\t" + upwards);
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
                        claiming();
                    } while (Math.abs(where - todo) > 0.001 && !reSchedule);

                    if(reSchedule){
                        //System.out.println(id + "message\t" + "retake");
                        reSchedule = false;
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
                    master.ebs[id][todo] = 0;

                    try{
                        master.bm[todo].unlock();
                    }catch (IllegalMonitorStateException e){

                    }

                    //remove job if job is not job 0
                    //set job to false (i.e. begin to dream) if job was 0 and there is no more jobs
                    remJob(todo);
                    if(todo != 0) {

                    }else{
                        upwards = true; //at floor zero can just go up
                        //System.out.println(id + " message\t" + upwards);
                        if(jobs.isEmpty())
                            job = false;
                    }

                } catch (RemoteException e) {
                    printNexit(e);
                }
                //System.out.println(id + " message\t" + jobs.toString());
            }
            //System.out.println(id + "message\t" + "dream: " + !job);
            claiming();
            if(!job)
                dream();
            claiming();
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
