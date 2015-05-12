import javafx.util.Pair;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class Main {
    public static String alphabet = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя1234567890 ";

    public static void main(String[] args) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File("input.txt"));

        String mode = scanner.nextLine();
        if (mode.equals("зашифровать")) {
            MyBigInteger p = new MyBigInteger(scanner.nextLine());
            MyBigInteger q = new MyBigInteger(scanner.nextLine());
            String message = scanner.nextLine();
            String cryptedMessage = crypt(p, q, message);
            if (cryptedMessage != null) {
                System.out.println("Зашифрованный текст: " + cryptedMessage);
            }
        } else if (mode.equals("расшифровать")) {
            MyBigInteger n = new MyBigInteger(scanner.nextLine());
            MyBigInteger d = new MyBigInteger(scanner.nextLine());
            String cryptedMessage = scanner.nextLine();
            String message = decrypt(n, d, cryptedMessage);
            System.out.println(message);
        } else {
            System.out.println("Введено неправльное значение");
        }

    }

    private static String decrypt(MyBigInteger n, MyBigInteger d, String cryptedMessage) {
        String message = "";
        String[] blocks = cryptedMessage.split(" ");
        for (int i = 0; i < blocks.length; i++) {
            MyBigInteger blockBigInt = new MyBigInteger(blocks[i]);
            MyBigInteger result = blockBigInt.pow(d, n);
            message += result.toString();
        }
        return decode(message);
    }

    private static String decode(String message) {
        String result = "";
        double messageSize = Math.floor(Math.log10(alphabet.length() / 1)) + 1;
        String dots = "";
        for (int i = 0; i < messageSize; i++) {
            dots += ".";
        }
        String[] octalElements = message.split("(?<=\\G" + dots + ")");
        for (int i = 0; i < octalElements.length; i++) {
            String element = "";
            String octallElement = octalElements[i];
            for (int j = 0; j < octallElement.length(); j++) {
                element += (Integer.parseInt(String.valueOf(octallElement.charAt(j))) - 1);
            }
            result += String.valueOf(alphabet.charAt(Integer.parseInt(element, 8) - 1));
        }

        return result;
    }

    private static String crypt(MyBigInteger p, MyBigInteger q, String message) {
        if (!p.isProbablyPrime(100)) {
            System.out.println("p не простое");
            return null;
        }
        if (!q.isProbablyPrime(100)) {
            System.out.println("q не простое");
            return null;
        }

        String codedMessage = codeMessage(message);
        if (codedMessage == null) {
            return null;
        } else {
            System.out.println("Код сообщения: " + codedMessage

            );
        }
        Pair<MyBigInteger, Pair<MyBigInteger, MyBigInteger>> pair = generateKey(p, q);
        MyBigInteger n = pair.getKey();
        MyBigInteger e = pair.getValue().getKey();


        if (codedMessage != null) {
            String result = "";
            int nLength = n.length();
            int rounder = 0;
            if (codedMessage.length() % (nLength - 1) == 0) {
                rounder = 0;
            } else {
                rounder = 1;
            }
            int blocksNumber = codedMessage.length() / (nLength - 1) + rounder;
            while (true) {
                int size = calculateSize(codedMessage, n);
                String block = codedMessage.substring(0, size);
                MyBigInteger blockBigInt = new MyBigInteger(block);
                result += blockBigInt.pow(e, n).toString() + " ";
                codedMessage = codedMessage.substring(size, codedMessage.length());
                if (codedMessage.length() == 0) {
                    break;
                }
            }
            return result;
        }
        return null;
    }

    private static Integer calculateSize(String codedMessage, MyBigInteger n) {
        if (codedMessage.length() < n.length()) {
            return codedMessage.length();
        }
        MyBigInteger myBigInteger = new MyBigInteger(codedMessage.substring(0, n.length()));
        if (myBigInteger.compareTo(n) >= 0) {
            return n.length() - 1;
        }
        return n.length();
    }

    private static Pair<MyBigInteger, Pair<MyBigInteger, MyBigInteger>> generateKey(MyBigInteger p, MyBigInteger q) {
        MyBigInteger n = p.times(q);
        System.out.println("n: " + n);
        MyBigInteger m = p.minus(MyBigInteger.ONE).times(q.minus(MyBigInteger.ONE));
        System.out.println("m: " + m);
        MyBigInteger e = new MyBigInteger();
        e = e.generateRandom(m);
        while (true) {
            if (coPrime(e, m)) {
                break;
            }
            if (e.equals(m)) {
                e = e.generateRandom(m);
            } else {
                e = (e.plus(MyBigInteger.ONE)).normalize();
            }
        }
        MyBigInteger d = e.modInverse(m);
        System.out.println("e: " + e);
        System.out.println("d: " + d);
        return new Pair<>(n, new Pair(e, d));
    }

    private static boolean coPrime(MyBigInteger a, MyBigInteger b) {
        if (a.toString().equals("0") || b.toString().equals("0")) return false;
        if (gcd(a, b).equals(MyBigInteger.ONE)) {
            return true;
        }
        return false;
    }

    private static MyBigInteger gcd(MyBigInteger a, MyBigInteger b) {
        MyBigInteger copyA = new MyBigInteger(a);
        MyBigInteger copyB = new MyBigInteger(b);
        if (!copyA.isPositive()) {
            copyA = copyA.negate();
        }
        if (!copyB.isPositive()) {
            copyB = copyB.negate();
        }

        if (copyA.compareTo(copyB) < 0) {
            MyBigInteger temp = copyA;
            copyA = copyB;
            copyB = temp;
        }
        while ((copyB.compareTo(MyBigInteger.ZERO)) != 0) {
            MyBigInteger temp = copyB;
            copyB = (copyA.div(copyB)).remainder;
            copyA = temp;
        }
        return copyA;
    }

    private static String codeMessage(String message) {
        String result = "";
        double messageSize = Math.floor(Math.log10(alphabet.length() / 1)) + 1;
        for (int i = 0; i < message.length(); i++) {
            char character = message.toLowerCase().charAt(i);
            int index = alphabet.indexOf(character) + 1;
            if (index < 0) {
                System.out.println("Сообщение содержит недопустимый символ");
            }
            String hexIndex = Integer.toOctalString(index);
            int codedMessageLength = hexIndex.length();
            if (codedMessageLength < messageSize) {
                for (int j = 0; j < messageSize - codedMessageLength; j++) {
                    hexIndex = "0"+hexIndex;
                }
            }
            for (int j = 0; j < hexIndex.length(); j++) {
                result += (Integer.parseInt(String.valueOf(hexIndex.charAt(j)))) + 1;
            }
        }
        return result;
    }
}