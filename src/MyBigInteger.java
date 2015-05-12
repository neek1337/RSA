import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MyBigInteger implements Comparable<MyBigInteger> {
    public static final Integer BASE = 10;
    public static final MyBigInteger ZERO = new MyBigInteger("0");
    public static final MyBigInteger ONE = new MyBigInteger("1");

    private List<Integer> digits;
    private boolean sign;

    public MyBigInteger(MyBigInteger a) {
        this.digits = new ArrayList<>();
        for (Integer digit : a.digits) {
            this.digits.add(digit);
        }
        this.sign = a.sign;
    }

    public MyBigInteger(String s) {
        this.digits = new ArrayList<>();
        if (s.charAt(0) == '-') {
            this.sign = false;
            s = s.replace("-", "");
        } else {
            this.sign = true;
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            digits.add(Integer.parseInt(s.charAt(i) + ""));
        }
    }

    public MyBigInteger() {
        this.digits = new ArrayList<>();
        this.sign = true;
    }


    public MyBigInteger(List<Integer> digits, boolean sign) {
        this.digits = digits;
        this.sign = sign;
    }

    public MyBigInteger(boolean sign) {
        this.digits = new ArrayList<>();
        this.sign = sign;
    }

    public boolean isPositive() {
        return sign;
    }

    public MyBigInteger negate() {
        return new MyBigInteger(this.digits, !sign);
    }

    public int length() {
        return digits.size();
    }

    public int get(int i) {
        return digits.get(i);
    }


    public MyBigInteger plus(MyBigInteger val) {
        int maxLength = Math.max(this.digits.size(), val.digits.size());
        boolean sameSign = this.sign == val.sign;
        fitLength(this, maxLength);
        fitLength(val, maxLength);
        if (sameSign) {
            int k = 0;
            MyBigInteger result = new MyBigInteger(this.sign);

            for (int j = 0; j < this.digits.size(); j++) {
                result.digits.add(j, (this.digits.get(j) + val.digits.get(j) + k) % BASE);
                k = (this.digits.get(j) + val.digits.get(j) + k) / BASE;
            }

            if (k != 0) {
                result.digits.add(1);
            }
            return result;
        } else {
            if (this.sign) {
                return this.minus(new MyBigInteger(val.digits, true));
            } else {
                return val.minus(new MyBigInteger(this.digits, true));
            }
        }

    }


    public MyBigInteger minus(MyBigInteger val) {
        int cmp = this.compareTo(val);
        if (val.sign != sign) {
            return this.plus(new MyBigInteger(val.digits, sign));
        }
        if (cmp == 0) {
            return ZERO;
        }
        MyBigInteger result = new MyBigInteger(cmp > 0 && sign);
        if (cmp > 0) {
            fitLength(val, this.digits.size());

            int k = 0;

            for (int j = 0; j < this.digits.size(); j++) {
                result.digits.add(j, (this.digits.get(j) - val.digits.get(j) + k + BASE) % BASE);
                k = (this.digits.get(j) - val.digits.get(j) + k) < 0 ? -1 : 0;
            }
        } else {
            fitLength(this, val.digits.size());

            int k = 0;

            for (int j = 0; j < val.digits.size(); j++) {
                result.digits.add(j, (val.digits.get(j) - this.digits.get(j) + k + BASE) % BASE);
                k = (val.digits.get(j) - this.digits.get(j) + k) < 0 ? -1 : 0;
            }
        }


        return result.normalize();
    }


    public MyBigInteger times(int val) {
        int k = 0;
        MyBigInteger result = new MyBigInteger(this.sign);

        for (int i = 0; i < digits.size(); i++) {
            int temp = this.digits.get(i) * val + k;
            result.digits.add(temp % BASE);
            k = temp / BASE;
        }
        result.digits.add(k);
        return result.normalize();
    }

    public MyBigInteger times(MyBigInteger val) {
        int m = this.digits.size();
        int n = val.digits.size();

        boolean sameSign = this.sign == val.sign;

        MyBigInteger result = new MyBigInteger(sameSign);
        for (int i = 0; i < m; i++) {
            result.digits.add(0);
        }

        int j = 0;
        int i = 0;
        int k = 0;

        while (j < n) {
            while (i < m) {
                int t = this.digits.get(i) * val.digits.get(j) + result.digits.get(i + j) + k;
                result.digits.set(i + j, t % BASE);
                k = t / BASE;
                i++;
            }
            result.digits.add(j + m, k);
            i = 0;
            k = 0;
            j++;
        }

        return result.normalize();
    }


    public BigIntegerDivisionResult div(MyBigInteger v) {
        if (v.digits.size() == 1) {
            return MyBigInteger.div(this, v.digits.get(0), v.sign);
        } else {
            return MyBigInteger.div(this, v);
        }
    }

    private static BigIntegerDivisionResult div(MyBigInteger u, int v, boolean valSign) {
        boolean sameSign = u.sign == valSign;
        MyBigInteger quotient = new MyBigInteger(sameSign);
        int r = 0;
        int j = u.digits.size() - 1;
        for (int i = 0; i < u.digits.size(); i++) {
            quotient.digits.add(0);
        }
        while (j >= 0) {
            int cur = r * BASE + u.digits.get(j);
            quotient.digits.set(j, cur / v);
            r = cur % v;
            j--;
        }
        quotient.normalize();

        String remainder = (!valSign ? "-" : "") + String.valueOf(r);
        return new BigIntegerDivisionResult(quotient, new MyBigInteger(remainder));
    }

    private static BigIntegerDivisionResult div(MyBigInteger u, MyBigInteger val) {
        BigInteger u1 = new BigInteger(u.toString());
        BigInteger v1 = new BigInteger(val.toString());
        return new BigIntegerDivisionResult(new MyBigInteger(u1.divide(v1).toString()), new MyBigInteger(u1.mod(v1).toString()));
    }

    private static BigIntegerDivisionResult divide(MyBigInteger u, MyBigInteger val) {

        if (u.compareTo(val) < 0) {
            return new BigIntegerDivisionResult(new MyBigInteger("0"), u);
        }
        boolean sameSign = u.sign == val.sign;
        MyBigInteger q = new MyBigInteger(sameSign);
        int n = val.digits.size();
        int m = u.digits.size() - n;

        //нормализация
        int d = BASE / (val.digits.get(n - 1) + 1);//d = [BASE/(v_(n-1) +1)]

        u = u.times(d);
        val = val.times(d);
        val = val.normalize();

        if (u.digits.size() == m + n) {
            u.digits.add(0);
        }

        int j = m;

        boolean flag;
        while (j >= 0) {
            flag = false;

            //вычисление q1
            int q1 = (u.digits.get(j + n) * BASE + u.digits.get(j + n - 1)) / val.digits.get(n - 1);//q' = [u_(j+n)*BASE+u_(j+n-1)/v_(n-1)]
            int r1 = (u.digits.get(j + n) * BASE + u.digits.get(j + n - 1)) % val.digits.get(n - 1);//r' = u_(j+n)*BASE+u_(j+n-1)

            do {
                if ((q1 == BASE) || ((q1 * val.digits.get(n - 2)) > (BASE * r1 + u.digits.get(j + n - 2)))) {
                    q1 -= 1;
                    r1 = (r1 + val.digits.get(n - 1));
                } else {
                    break;
                }
            } while (r1 < BASE);

            //todo sign
            MyBigInteger u_2 = new MyBigInteger(u.digits.subList(j, j + n + 1), true);
            if (u_2.compareTo(val.times(q1)) < 0) {
                flag = true;
                String s = "1";
                for (int i = 0; i <= n; i++) {
                    s += "0";
                }
                u_2 = u_2.plus(new MyBigInteger(s));
            }
            u_2 = u_2.minus(val.times(q1));
            if (flag) {
                q1--;
                u_2 = u_2.plus(val);
                if (u_2.digits.size() > n + j) {
                    u_2.digits.remove(u_2.digits.size() - 1);
                }

            }
            q.digits.add(0, q1);

            for (int i = j; i < j + n + 1; i++) {
                if (i - j >= u_2.digits.size()) {
                    u.digits.set(i, 0);
                } else {
                    u.digits.set(i, u_2.digits.get(i - j));
                }
            }
            j--;
        }
        q = q.normalize();
        u.normalize();

        BigIntegerDivisionResult normalization = MyBigInteger.div(u, d, val.sign);

        return new BigIntegerDivisionResult(q, normalization.quotient);

    }

    public MyBigInteger pow(MyBigInteger k, MyBigInteger n) {
        MyBigInteger a = new MyBigInteger(this.digits, this.sign);
        MyBigInteger b = new MyBigInteger("1");
        while (k.compareTo(ZERO) > 0) {
            BigIntegerDivisionResult q = MyBigInteger.div(k, 2, true);
            if (q.remainder.compareTo(ZERO) == 0) {
                k = q.quotient;
                a = a.times(a);
                a = a.div(n).remainder;
            } else {
                k = k.minus(ONE);
                b = b.times(a).div(n).remainder;
            }
        }
        return b;
    }

    private static void fitLength(MyBigInteger bigInteger, int length) {
        int size = bigInteger.digits.size();
        if (size < length) {
            for (int i = 0; i < length - size; i++) {
                bigInteger.digits.add(0);
            }
        }


    }


    public MyBigInteger normalize() {
        for (int i = digits.size() - 1; i > 0; i--) {
            if (digits.get(i) == 0) {
                digits.remove(i);
            } else {
                break;
            }
        }
        return this;
    }

    @Override
    public int compareTo(MyBigInteger o) {
        int oLength = o.digits.size();
        int thisLength = this.digits.size();
        o.normalize();
        this.normalize();

        int result = 0;

        if (o.digits.size() > this.digits.size()) {
            result = -1;
        } else if (o.digits.size() < this.digits.size()) {
            result = 1;
        } else {
            for (int i = this.digits.size() - 1; i >= 0; i--) {
                if (o.digits.get(i) > this.digits.get(i)) {
                    result = -1;
                    break;
                } else if (o.digits.get(i) < this.digits.get(i)) {
                    result = 1;
                    break;
                }
            }
        }
        fitLength(o, oLength);
        fitLength(this, thisLength);
        return result;
    }

    public boolean equals(MyBigInteger o) {
        return this.compareTo(o) == 0;
    }

    @Override
    public int hashCode() {
        int result = digits != null ? digits.hashCode() : 0;
        result = 31 * result + (sign ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        String result = digits.isEmpty() ? "0" : digits.get(digits.size() - 1) + "";
        result = (!sign ? "-" : "") + result;
        for (int i = digits.size() - 2; i >= 0; i--) {
            result += digits.get(i);
        }
        return result;
    }

    public boolean isProbablyPrime(int s) {
        if (this.length() == 1) {
            String stringThis = this.toString();
            return (stringThis.equals("2") || stringThis.equals("3") || stringThis.equals("5") || stringThis.equals("7"));
        }
        for (int i = 0; i < s; i++) {
            MyBigInteger a = generateRandom(this);
            if (miller(a, this) && (a.compareTo(MyBigInteger.ZERO) != 0)) {
                return false;
            }
        }
        return true;
    }

    private boolean miller(MyBigInteger a, MyBigInteger n) {
        MyBigInteger d = n.minus(MyBigInteger.ONE);
        int s = 0;
        MyBigInteger q = d.div(new MyBigInteger("2")).quotient;
        MyBigInteger r = d.div(new MyBigInteger("2")).remainder;
        while (r.compareTo(MyBigInteger.ZERO) == 0) {
            s++;
            d = q;
            MyBigInteger qlocal = q.div(new MyBigInteger("2")).quotient;
            MyBigInteger rlocal = q.div(new MyBigInteger("2")).remainder;
            q = qlocal;
            r = rlocal;
        }
        MyBigInteger x = a.pow(d, n);
        if (x.equals(MyBigInteger.ONE)) return false;
        for (int i = 0; i < s + 1; i++) {
            int mult = (int) Math.pow(2, i);
            x = a.pow(d.times(mult), n);
            if (x.equals(n.minus(MyBigInteger.ONE))) return false;
        }

        return true;
    }

    public MyBigInteger generateRandom(MyBigInteger n) {
        if (n.length() == 1) {
            int nString = Integer.parseInt(n.toString());
            Integer random = new Random().nextInt(nString);
            while (random == 0) {
                random = new Random().nextInt(nString);
            }
            return new MyBigInteger(random.toString());
        }
        ArrayList<Integer> digits = new ArrayList<>();
        MyBigInteger maxBoud = n.minus(MyBigInteger.ONE);
        Random random = new Random();
        boolean flag = true;
        for (int i = maxBoud.length() - 1; i >= 0; i--) {
            int temp = 0;
            if (flag) {
                temp = random.nextInt(maxBoud.get(i) + i);
                flag = (maxBoud.get(i) == temp);
            } else {
                temp = random.nextInt(MyBigInteger.BASE);
            }
            digits.add(0, temp);
        }
        return new MyBigInteger(digits, true).normalize();
    }

    public MyBigInteger modInverse(MyBigInteger m) {
        MyBigInteger x = extendGCD(this, m);
        MyBigInteger inverse = (x.plus(m).div(m)).remainder;
        if (!inverse.isPositive()) {
            inverse = inverse.plus(m);
        }
        return inverse;
    }

    private MyBigInteger extendGCD(MyBigInteger a, MyBigInteger m) {
        MyBigInteger x = new MyBigInteger("0");
        MyBigInteger lastX = new MyBigInteger("1");
        MyBigInteger y = new MyBigInteger("1");
        MyBigInteger lastY = new MyBigInteger("0");
        MyBigInteger r = m;
        MyBigInteger lastR = a;

        while (r.compareTo(MyBigInteger.ZERO) > 0) {
            MyBigInteger tempR = r;
            MyBigInteger q = lastR.div(r).quotient;
            MyBigInteger re = lastR.div(r).remainder;
            lastR = tempR;
            r = re;
            MyBigInteger tempX = x;
            x = lastX.minus(q.times(x));
            lastX = tempX;

            MyBigInteger tempY = y;
            y = lastY.minus(q.times(y));
            lastY = tempY;
        }
        if (!lastR.isPositive()) return lastX.negate();
        return lastX;
    }

    static class BigIntegerDivisionResult {
        MyBigInteger quotient;

        MyBigInteger remainder;

        public BigIntegerDivisionResult(MyBigInteger quotient, MyBigInteger remainder) {
            this.quotient = quotient;
            this.remainder = remainder;
        }

        public MyBigInteger component1() {
            return quotient;
        }


        public MyBigInteger component2() {
            return remainder;
        }
    }
}
