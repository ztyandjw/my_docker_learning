/**
 * Created by Tim Zhang on 2018/12/14
 */

/**
 * 用数组实现栈
 */
public class Stack {

    private String[]items;
    private int count;
    private int n;

    public Stack(int n) {
        this.items = new String[n];
        this.n = n;
        this.count = 0;
    }

    public boolean push(String str) {
        if (count == n) {
            return false;
        }
        items[count] = str;
        ++count;
        return true;
    }

    public String pop() {
        if(count == 0) {
            return null;
        }
        String str = items[count -1];
        --count;
        return str;
    }
}
