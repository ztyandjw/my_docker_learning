/**
 * Created by Tim Zhang on 2018/12/18
 */
public class CircularQueue {

    private String [] items;
    private int n = 0;
    private int head = 0;
    private int tail = 0;

    public CircularQueue(int capacity) {
        items = new String[capacity];
        n = capacity;
    }

    public boolean enqueue(String item) {
        //队列满了
        if((tail + 1) %n == head) {
            return false;
        }
        items[tail] = item;
        tail = (tail + 1) % n;
        return true;
    }

    public String dequeue() {
        if(head == tail) {
            return null;
        }
        else {
            String ret = items[head];
            head = (head + 1) % n;
            return ret;
        }
    }
}
