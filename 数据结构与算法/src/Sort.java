import java.util.Arrays;

/**
 * Created by Tim Zhang on 2018/12/20
 */
public class Sort {

    public static void main(String[] args) {
        int []a  = {2,1,8,4,3,3,2,7,10,0,88};
        selectSort(a, a.length);
        System.out.println(Arrays.toString(a));

    }



    public static void selectSort(int []a, int n) {
        if(n <= 1) {
            return;
        }

        for(int i =0; i < n-1; i++) {
            int minIndex = i;
            int j = i + 1;

            for(; j < n; j++) {
                if(a[j] < a[minIndex]) {
                    minIndex = j;
                }
            }
            int tmp = a[i];
            a[i] = a[minIndex];
            a[minIndex] = tmp;

        }


    }

    public static void insertSort(int []a, int n) {
        if(n <= 1) {
            return;
        }

        for(int i = 1; i < n; i++) {
            //需要被比较的元素
            int value = a[i];
            //排序元素的最后1个，第一个被比较的元素
            int j = i -1;
            for(; j >=0; j--) {
                if(value < a[j]) {
                    a[j + 1] = a[j];
                }
                else {
                    break;
                }
            }
            a[j + 1] = value;

        }
    }




    public static void bubbleSort(int []a, int n) {
        if(n <= 1) {
            return;
        }
        for(int i = 0; i < n -1; i++) {
            boolean flag = false;
            for(int j = 0; j < n -1 -i; j++) {
                if (a[j] > a[j+1]) {
                    int tmp = a [j + 1];
                    a[j+1] = a[j];
                    a[j] = tmp;
                    flag = true;
                }
            }
            if(flag = false) {
                break;
            }
        }
    }

}
