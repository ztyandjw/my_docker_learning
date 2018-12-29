import java.util.Arrays;

/**
 * Created by Tim Zhang on 2018/12/25
 */
public class MergeSort {

    public static void main(String[] args) {
        int []a  = {2,1,8,4,3,3,2,7,10,0,88};
        mergeSortInternally(a, 0, 10);
        System.out.println(Arrays.toString(a));

    }

    public static void mergeSortInternally(int [] a, int p, int r) {
        if(p >= r) {
            return;
        }
        int q = p + (r-p)/2;
        mergeSortInternally(a, p, q);
        mergeSortInternally(a, q+1, r);
        merge(a, p, q, r);


    }

    public static void merge(int [] a, int p, int q,  int r) {
        int i = p;
        int j = q + 1;
        int k = 0;
        int [] tmp = new int[r - p +1];
        while (i <=q && j<=r) {
            if(a[i] <= a[j]) {
                tmp[k++] = a[i++];
            }
            else {
                tmp[k++] = a[j++];
            }
        }
        int start = i;
        int end = q;
        if(j <= r) {
            start = j;
            end = r;
        }
        while(start <= end) {
            tmp[k++] = a[start++];
        }

        for(int ii = 0; ii <= r-p; ii++) {
            a[p + ii] = tmp[ii];
        }
    }
}
