import java.util.Random;

/**
 * Created by Tim Zhang on 2019/1/4
 */

/*
跳表
 */
public class SkipList {
    public static void main(String[] args) {
        SkipList skipList = new SkipList();
        for(int i = 0; i < 100; i++) {
            skipList.insert(i);
        }
        skipList.printAll();
        Node findNode = skipList.find(22);
        System.out.println(findNode);
    }
    public int levelCount = 1;
    private static final int MAX_LEVEL = 16;
    private Node head = new Node();
    private Random r = new Random();
    private int randomLevel() {
        int level = 1;
        for(int i = 1; i < MAX_LEVEL; i++) {
            if(r.nextInt() %2 == 1) {
                level ++;
            }
        }
        return level;
    }

    public Node find(int value) {
        //拿到头节点
        Node p = head;
        for (int i = levelCount - 1; i >= 0; --i) {
            while (p.forwards[i] != null && p.forwards[i].data < value) {
                p = p.forwards[i];
            }
        }

        if (p.forwards[0] != null && p.forwards[0].data == value) {
            return p.forwards[0];
        } else {
            return null;
        }
    }

    public void insert(int value) {
        //随机生成level
        int level = randomLevel();
        Node newNode = new Node();
        newNode.data = value;
        newNode.maxLevel = level;
        Node update[] = new Node[level];
        for(int i = 0; i < level; i++) {
            update[i] = head;
        }
        Node p = head;
        for(int i = level - 1; i >= 0; i--) {
            while(p.forwards[i] != null && p.forwards[i].data < value) {
                p = p.forwards[i];
            }
            update[i] = p;
        }
        for(int i = 0; i < level; i++) {
            newNode.forwards[i] = update[i].forwards[i];
            update[i].forwards[i] = newNode;
        }
        if(levelCount < level) {
            levelCount = level;
        }
    }

    public void printAll() {
        Node p = head;
        while (p.forwards[0] != null) {
            System.out.print(p.forwards[0] + " ");
            p = p.forwards[0];
        }
        System.out.println();
    }

    public static class Node {
        private int data = -1;
        private Node forwards[] = new Node[MAX_LEVEL];
        private int maxLevel = 0;
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ data: ").append(data).append("; levels: ").append(maxLevel).append("} ");
            return sb.toString();
        }
    }
}
