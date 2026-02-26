import java.util.Scanner;
import java.lang.String;

public class UseCase1PalindromeCheckerApp {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String a = sc.nextLine();
        char[] b= a.toCharArray();
        int i=0;
        boolean flag = false;
        int l= a.length();
        while (i<l/2) {
            if(a[i] != a[l - i]) {
                System.out.println("Not a Palindrome");
                return;
            }
            else
                flag=true;
            i++;
        }
        if(flag==true)
            System.out.println("Is a Palindrome");

    }
}
