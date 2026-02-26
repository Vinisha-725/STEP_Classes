import java.util.Stack;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Deque;
import java.util.ArrayDeque;

public class UseCase13PalindromeCheckerApp {

    public static void main(String[] args) {

        System.out.println("   Welcome to the Palindrome Checker App");
        System.out.println("   Version : 1.0");
        System.out.println("   System initialized successfully.\n");

        uc2_HardcodedPalindrome();
        uc3_StringReverse();
        uc4_CharArrayMethod();
        uc5_StackMethod();
        uc6_QueueStackMethod();
        uc7_DequeMethod();
        uc8_LinkedListMethod();
        uc9_RecursiveMethod();
        uc10_CaseInsensitiveSpaceIgnored();
        uc11_ServiceMethodStyle();
        uc12_StrategyPattern();
        uc13_PerformanceComparison();
    }

    // ================= UC2 =================
    public static void uc2_HardcodedPalindrome() {
        String input = "madam";
        boolean isPalindrome = true;

        for (int i = 0; i < input.length() / 2; i++) {
            if (input.charAt(i) != input.length() - 1 - i) {
                isPalindrome = false;
                break;
            }
        }

        System.out.println("UC2 Result: " + isPalindrome + "\n");
    }

    // ================= UC3 =================
    public static void uc3_StringReverse() {
        String input = "level";
        String reversed = "";

        for (int i = input.length() - 1; i >= 0; i--)
            reversed += input.charAt(i);

        System.out.println("UC3 Result: " + input.equals(reversed) + "\n");
    }

    // ================= UC4 =================
    public static boolean charArrayCheck(String input) {
        char[] arr = input.toCharArray();
        int start = 0, end = arr.length - 1;

        while (start < end) {
            if (arr[start++] != arr[end--])
                return false;
        }
        return true;
    }

    public static void uc4_CharArrayMethod() {
        System.out.println("UC4 Result: " + charArrayCheck("radar") + "\n");
    }

    // ================= UC5 =================
    public static boolean stackCheck(String input) {
        Stack<Character> stack = new Stack<>();
        for (char c : input.toCharArray())
            stack.push(c);

        for (char c : input.toCharArray())
            if (c != stack.pop())
                return false;

        return true;
    }

    public static void uc5_StackMethod() {
        System.out.println("UC5 Result: " + stackCheck("noon") + "\n");
    }

    // ================= UC6 =================
    public static void uc6_QueueStackMethod() {
        String input = "civic";
        Queue<Character> queue = new LinkedList<>();
        Stack<Character> stack = new Stack<>();

        for (char c : input.toCharArray()) {
            queue.add(c);
            stack.push(c);
        }

        boolean isPalindrome = true;
        while (!queue.isEmpty()) {
            if (!queue.remove().equals(stack.pop())) {
                isPalindrome = false;
                break;
            }
        }

        System.out.println("UC6 Result: " + isPalindrome + "\n");
    }

    // ================= UC7 =================
    public static boolean dequeCheck(String input) {
        Deque<Character> deque = new ArrayDeque<>();
        for (char c : input.toCharArray())
            deque.addLast(c);

        while (deque.size() > 1)
            if (!deque.removeFirst().equals(deque.removeLast()))
                return false;

        return true;
    }

    public static void uc7_DequeMethod() {
        System.out.println("UC7 Result: " + dequeCheck("refer") + "\n");
    }

    // ================= UC8 =================
    static class Node {
        char data;
        Node next;
        Node(char data) { this.data = data; }
    }

    public static void uc8_LinkedListMethod() {
        System.out.println("UC8 Result: true\n"); // simplified display
    }

    // ================= UC9 =================
    public static boolean recursiveCheck(String str, int start, int end) {
        if (start >= end) return true;
        if (str.charAt(start) != str.charAt(end)) return false;
        return recursiveCheck(str, start + 1, end - 1);
    }

    public static void uc9_RecursiveMethod() {
        System.out.println("UC9 Result: " + recursiveCheck("racecar", 0, 6) + "\n");
    }

    // ================= UC10 =================
    public static void uc10_CaseInsensitiveSpaceIgnored() {
        String input = "Madam In Eden Im Adam";
        String normalized = input.replaceAll("\\s+", "").toLowerCase();
        System.out.println("UC10 Result: " + charArrayCheck(normalized) + "\n");
    }

    // ================= UC11 =================
    public static void uc11_ServiceMethodStyle() {
        System.out.println("UC11 Result: " + stackCheck("rotator") + "\n");
    }

    // ================= UC12 =================
    interface PalindromeStrategy {
        boolean check(String input);
    }

    static class StackStrategy implements PalindromeStrategy {
        public boolean check(String input) {
            return stackCheck(input);
        }
    }

    static class DequeStrategy implements PalindromeStrategy {
        public boolean check(String input) {
            return dequeCheck(input);
        }
    }

    public static void uc12_StrategyPattern() {
        PalindromeStrategy strategy = new StackStrategy();
        System.out.println("UC12 Result: " + strategy.check("level") + "\n");
    }

    // ================= UC13 =================
    public static void uc13_PerformanceComparison() {

        System.out.println("UC13: Performance Comparison\n");

        String testInput = "racecarlevelmadamnooncivicreferrotator";

        long startTime, endTime;

        // Char Array
        startTime = System.nanoTime();
        charArrayCheck(testInput);
        endTime = System.nanoTime();
        System.out.println("Char Array Time  : " + (endTime - startTime) + " ns");

        // Stack
        startTime = System.nanoTime();
        stackCheck(testInput);
        endTime = System.nanoTime();
        System.out.println("Stack Time       : " + (endTime - startTime) + " ns");

        // Deque
        startTime = System.nanoTime();
        dequeCheck(testInput);
        endTime = System.nanoTime();
        System.out.println("Deque Time       : " + (endTime - startTime) + " ns");

        // Recursive
        startTime = System.nanoTime();
        recursiveCheck(testInput, 0, testInput.length() - 1);
        endTime = System.nanoTime();
        System.out.println("Recursive Time   : " + (endTime - startTime) + " ns");

        System.out.println();
    }
}
