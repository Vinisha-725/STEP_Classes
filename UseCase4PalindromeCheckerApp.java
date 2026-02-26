public class UseCase4PalindromeCheckerApp {
    public static void main (String[] args)
    {
        uc4_CharArrayMethod();
    }
    public static void uc4_CharArrayMethod() {

        String input = "racecar";
        char[] chars = input.toCharArray();

        int start = 0;
        int end = chars.length - 1;
        boolean isPalindrome = true;

        while (start < end) {

            if (chars[start] != chars[end]) {
                isPalindrome = false;
                break;
            }

            start++;
            end--;
        }

        System.out.println("UC4: Character Array Based Palindrome Check");
        System.out.println("Input : " + input);
        System.out.println("Is Palindrome? : " + isPalindrome);
        System.out.println();
    }
}
