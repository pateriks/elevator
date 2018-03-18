package elevator.demo;


import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BinaryMonitor{

    Lock up = new ReentrantLock();
    Lock down = new ReentrantLock();
    int modifiers = 0;

    public BinaryMonitor(){
    }

    public synchronized void change(int dir) throws Exception{
        dir = 1;
        if (modifiers > 1){
            throw new Exception("Access not granted");
        }
        if (dir != 1) {
            boolean retr = down.tryLock();
            if (!retr) {
                throw new Exception("Access not granted");
            }
        } else {
            boolean retr = up.tryLock();
            if (!retr) {
                throw new Exception("Access not granted");
            }
        }
        modifiers++;
    }
    public synchronized void release(int dir){
        dir = 1;
        if(dir != 1){
            if(down.tryLock()){
                down.unlock();
                modifiers--;
            }
        }else{
            if(up.tryLock()){
                System.out.println("unlocked");
                up.unlock();
                System.out.println(up.tryLock() + " " + modifiers);
                up.unlock();
                modifiers--;
            }
        }

    }

}
