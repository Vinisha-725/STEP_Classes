All weeks the indivisual weeks have been uploaded in their respective branches Refer to the branches for the respective assignments Kindly take Master as week1 and then after that take according to the branch names

UC1: Application Entry & Welcome Message Goal: Display a welcome message and app details at startup.

Actor: User

Flow: ● Program starts.

● JVM invokes the main() method.

● Application name is displayed.

● Application version is displayed.

● Program continues to next use case or exits.

Key Concepts used in UC1: ● Class – Acts as a container for the Palindrome Checker application logic.

● Main Method – Entry point of the Java application with the signature public static void main(String[] args).

● Static Keyword – Allows the JVM to invoke the main() method without creating an object.

● Console Output – System.out.println() is used to display messages on the console.

● Application Flow Control – Defines the startup behavior before palindrome processing begins.

UC2: Print a Hardcoded Palindrome Result Goal: Display whether a hardcoded string is a palindrome.

Actor: User

Flow: Program starts Hardcoded string is checked Result is printed Program exits Key Concepts used in UC2: ● Class – In Java, even the simplest program must be written inside a class, as a class acts as a container to hold program logic.

● Main Method – Entry point of the Java application with the signature public static void main(String[] args).

● Static Keyword – Allows the JVM to invoke the main() method without creating an object of the class.

● String – A built-in Java class used to store and manipulate textual data. Here, it stores the word to be checked for a palindrome.

● String Literal – Text enclosed within double quotes ("madam"), stored in the String constant pool.

● Conditional Statement (if-else) – Used to evaluate whether the given string satisfies the palindrome condition.

● Console Output – System.out.println() is used to display the result on the console.

UC3: Palindrome Check Using String Reverse Goal: Check whether a string is a palindrome by reversing it.

Flow: Reverse string using loop Compare original and reversed Display result Key Concepts used in UC3: ● Loop (for loop) – Used to iterate through the characters of the string in reverse order.

● String Immutability – In Java, String objects are immutable; every modification creates a new String object.

● String Concatenation (+) – Used to build the reversed string character by character (introduced here to later discuss drawbacks).

● equals() Method – Used to compare the actual content of two String objects instead of memory references

UC4: Character Array Based Palindrome Check Goal: Convert string to character array and compare characters.

Flow: Convert string to char[] Use two-pointer approach Compare start & end characters Key Concepts used in UC4: ● Character Array (char[]) – A primitive array used to store individual characters of a string for index-based access.

● Array Indexing – Accessing elements using index positions starting from 0.

● Two-Pointer Technique – One pointer starts from the beginning and the other from the end, reducing unnecessary comparisons.

● Time Complexity Awareness – Demonstrates efficient comparison without creating extra objects.

UC5: Stack-Based Palindrome Checker Goal: Use stack to reverse characters and validate palindrome.

Flow: Push characters into stack Pop and compare Print result Key Concepts used in UC5: ● Stack – A linear data structure that follows the Last In First Out (LIFO) principle.

● Push Operation – Used to insert characters into the stack.

● Pop Operation – Used to remove characters from the stack in reverse order.

● Reversal Logic – Stack naturally reverses the order of elements, making it suitable for palindrome validation

UC6: Queue + Stack Based Palindrome Check Goal: Demonstrate FIFO vs LIFO using Queue and Stack.

Flow: Enqueue characters Push characters to stack Compare dequeue vs pop Key Concepts used in UC6: ● Queue – A linear data structure that follows the First In First Out (FIFO) principle.

● Enqueue & Dequeue Operations – Used to insert and remove elements from the queue.

● Stack vs Queue – Demonstrates the behavioral difference between LIFO and FIFO structures.

● Logical Comparison – Matching dequeue (queue) output with pop (stack) output to validate palindrome logic.

UC7: Deque-Based Optimized Palindrome Checker Goal: Use Deque to compare front and rear elements.

Flow: Insert characters into deque Remove first & last Compare until empty Key Concepts used in UC7: ● Deque (Double Ended Queue) – A data structure that allows insertion and deletion from both front and rear ends.

● Front and Rear Access – Enables direct comparison of first and last characters.

● Optimized Data Handling – Eliminates the need for separate reversal data structures.

UC8: Linked List Based Palindrome Checker Goal: Check palindrome using singly linked list.

Flow: Convert string to linked list

Reverse second half

Compare halves

Key Concepts used in UC8: Singly Linked List – A dynamic data structure where elements are connected using node references. Node Traversal – Sequential access to elements using next references. Fast and Slow Pointer Technique – Used to find the middle of the linked list efficiently. In-Place Reversal – Reverses the second half of the list without extra memory. UC9: Recursive Palindrome Checker Goal: Check palindrome using recursion.

Flow: Recursive call compares start & end Base condition exits recursion Key Concepts used in UC9: Recursion – A technique where a method calls itself to solve smaller subproblems. Base Condition – Prevents infinite recursion and terminates the recursive calls. Call Stack – Memory structure used to manage method calls during recursion. UC10: Case-Insensitive & Space-Ignored Palindrome Goal: Ignore spaces and case while checking a palindrome.

Flow: Normalize string Apply previous logic Key Concepts used in UC10: String preprocessing Regular expressions
