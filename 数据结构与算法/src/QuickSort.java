import java.util.Arrays;

/**
 * Created by Tim Zhang on 2018/12/25
 */
public class QuickSort {

    public static void main(String[] args) {
        quickSort();
    }

    public static void quickSort() {
        int []a  = {2,1,8,4,3,3,2,7,10,0,88};
        quidkSortInternally(a, 0, a.length - 1);
        System.out.println(Arrays.toString(a));



    }

    public static void quidkSortInternally(int []a , int p, int r) {
        if(p >= r) {
            return;
        }
        int q = patition(a, p, r);
        quidkSortInternally(a, p, q-1);
        quidkSortInternally(a, q+1, r);

    }

    public static int  patition(int []a, int p, int r) {
        int patitionValue = a[r];
        int i = p;
        for(int j = p; j < r - p; j++) {
            if(a[j] < patitionValue) {
                if(i != j) {
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                }
                i++;
            }
        }
        int temp = a[i];
        a[i] = patitionValue;
        a[r] = temp;
        System.out.println(i);
        return i;
    }
}
