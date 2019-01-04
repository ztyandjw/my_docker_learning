import java.util.Arrays;

/**
 * Created by Tim Zhang on 2018/12/29
 */


public class BinarySearch {

    public static void main(String[] args) {
        int [] a = {1,2,3,4,6,6,6,6,6,7,8,9};
//        System.out.println(binarySearch3(a, a.length, 5));
//        System.out.println(binarySearch3(a, a.length, 6));
        removeValues(a, 6, a.length);


    }



    /**
     * 求平方根，要求精度为小数点6
     * @param n
     */
    public static double sqr(double n, double precision) {
        double low = 0;
        double high = n;
        while(low <= high) {
            double mid = low + ((high - low) / 2);
            if(Math.abs(mid * mid - n) < precision) {
                return mid;
            }
            if(mid * mid > n) {
                high = mid;
            }
            else if(mid * mid < n) {
                low = mid;
            }
        }
        return -1;
    }

    /*int [] a = {1,2,3,4,6,6,6,6,6,7,8,9}
     search the first one >= value
     */
    public static int binarySearch3(int [] a, int n, int value) {
        int low = 0;
        int high = n - 1;
        do {
            int mid = low + ((high - low) >> 2);
            if(a[mid]< value) {
                low = mid + 1;
            }
            else {
                if(mid == 0 || a[mid - 1] < value) {
                    return mid;
                }
                else {
                    high = mid - 1;
                }
            }
        }while(low <= high);
        return -1;
    }

    /*int [] a = {1,2,3,4,6,6,6,6,6,7,8,9}
     search the last one <= value
     */
    public static int binarySearch5(int [] a, int n, int value) {
        int low = 0;
        int high = n - 1;
        do {
            int mid = low + ((high - low) >> 2);
            if(a[mid] >  value) {
                high = mid - 1;
            }
            else {
                if(mid == n -1  || a[mid + 1] > value) {
                    return mid;
                }
                else {
                    low = mid + 1;
                }
            }
        }while(low <= high);
        return -1;
    }

    public static void  removeValues(int [] a, int value, int n) {
        int i = 0;
        int j = 0;
        for(; j < n -1; j++) {
            if (a[j] == value && i != j) {
                int temp = a[i];
               a[i] = value;
               a[j] = temp;
               i++;
            }
        }
        int [] b = new int[n - i];
        int l = n -1;
        for(int k = 0; k < n -i; k++) {
            b[k] = a[l];
            l--;
        }
    }
}
