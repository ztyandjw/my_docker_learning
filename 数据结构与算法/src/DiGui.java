/**
 * Created by Tim Zhang on 2018/12/19
 */
public class DiGui {

    public int f1(int n) {
        if (n == 1) {
            return 1;
        }
        int ret = 1;
        for(int i = 2; i <=n; i++) {
            ret = ret + 1;
        }
        return ret;
    }

    public int f2(int n) {
        if(n == 1) {
            return 1;
        }
        if(n == 2) {
            return 2;
        }
        int ret = 0;
        int prepre = 1;
        int pre = 2;
        for(int i = 3; i <=n; i++) {
            ret = prepre + pre;
            prepre = pre;
            pre = ret;
        }
        return ret;
    }
}
