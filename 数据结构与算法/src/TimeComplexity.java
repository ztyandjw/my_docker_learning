/**
 * Created by Tim Zhang on 2018/12/11
 */
public class TimeComplexity {

    public static void main(String[] args) {

        int array [] = new int [10];
        int length = 10;
        int i = 0;


    }

    public static  void insert(int element, int array[], int length, int i) {

        if(i >= length) {
            int[] new_array = new int[length*2];
            for(int j =0; j<length; ++j) {
                new_array[j] = array[j];
            }
            array = new_array;
            length = length * 2;
            array[i] = element;
            ++i;
        }
    }




}


//最好时间复杂度o(1) 最坏时间复杂度 O(n)  均摊时间复杂度O(1)