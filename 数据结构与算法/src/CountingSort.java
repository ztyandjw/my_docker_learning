import java.util.Arrays;

/**
 * Created by Tim Zhang on 2018/12/27
 */
public class CountingSort {

    public static void main(String[] args) {
        int []a  = {2,1,8,4,3,3,2,7,10,0,88};
        countingSort(a, a.length);
        System.out.println(Arrays.toString(a));
    }

    public static void countingSort(int []a, int n) {
        if(n <= 1) {
            return;
        }
        //找到数组中最大的数
        int max = a[0];
        for(int i = 1; i < n; i++) {
            if(max < a[i]) {
                max = a[i];
            }
        }

        int[]c = new int [max + 1];

        for(int i = 0; i < n; i++) {
            c[a[i]] ++;
        }

        for(int i = 1; i <= max; i++) {
            c[i] = c[i-1] + c[i];
        }

        int [] r = new int [n];

        for(int i = 0; i < n; i++) {
            int index = c[a[i]] -1;
            r[index] = a[i];
            c[a[i]] --;
        }

        for(int i =0; i < n; i++) {
            a[i] = r[i];
        }
    }
}
