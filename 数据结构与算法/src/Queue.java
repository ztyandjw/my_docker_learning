/**
 * Created by Tim Zhang on 2018/12/18
 */
public class Queue {

    private String []items;
    private int n = 0;
    private int head = 0;
    private int tail = 0;

    public Queue(int capacity) {
        items = new String[capacity];
        n = capacity;
    }

    public boolean enqueue1(String item) {
        //队列已经满了
        if(tail == n) {
            return false;
        }
        items[tail] = item;
        ++tail;
        return true;
    }

    public String dequeue() {
        if(head == tail) {
            return null;
        }
        else {
            String ret = items[head];
            ++head;
            return ret;
        }
    }


    public boolean enqueue2(String item) {
        //表示队尾没有空间了
        if(tail == n) {
            //没有可以搬移的数据了
            if(head == 0) {
                return false;
            }
            else {
                for(int i = 0; i < head; i++) {
                    items[i] = items[head + i];
                }
                head = 0;
                tail = tail - head;

            }
        }
        items[tail] = item;
        ++tail;
        return true;
    }

}
