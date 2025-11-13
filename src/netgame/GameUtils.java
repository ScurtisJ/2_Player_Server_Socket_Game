package src.netgame;

public class GameUtils {

    public static int HCF(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
            }
            return a;

    }
    public static int LCM(int a, int b) {
        int hcf = HCF(a, b);
         return (a * b) / hcf;
    }
    
    public static boolean primeNumber(int x) {
        if (x <= 1) return false;
        if (x == 2) return true;
        if (x % 2 == 0) return false;
        for (int i = 3; i * i <= x; i += 2) {
            if (x % i == 0) return false;
        }
        return true;

    }

    public static boolean legalMoveP1(int a) {
        if(primeNumber(a)) return false;
        if (a < 50 || a > 99) return false;
        return true;
    }

    public static boolean legalMoveP2(int b) {
        if(primeNumber(b)) return false;
        if (b < 60 || b > 99) return false;
        

        return true;
    }

}


