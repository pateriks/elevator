package elevator.demo;

public class FloorButton {
    boolean down = false;
    boolean up = false;

    public FloorButton(){

    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isUp() {
        return up;
    }

    public boolean isPushed() {
        return (up || down);
    }

    @Override
    public String toString() {
        return ("Down " + down + " Up " + up);
    }
}
