import java.util.LinkedList;

/**
 * Created by Tim Zhang on 2018/12/13
 */
public class Linked {

    public static void main(String[] args) {

        String str = "aleoela";
        LinkedList<Character> linkedList = new LinkedList<Character>();
        for(int i =0; i < str.length(); i++) {
            linkedList.add(str.charAt(i));
        }
        Character a = linkedList.element();
        System.out.println(a);




    }
}
