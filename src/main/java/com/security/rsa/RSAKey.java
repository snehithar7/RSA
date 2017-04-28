package com.security.rsa;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * Created by Manasee on 4/5/17.
 */
public class RSAKey {
    private BigInteger p;
    private BigInteger dP;
    private BigInteger q;
    private BigInteger dQ;
    private BigInteger d;
    private BigInteger n;
    private BigInteger e;
    private BigInteger phi;
    private BigInteger qInv;
    private int bitlength = Constant.KEY_LENGTH;

    public RSAKey() {
        setPrime1(null);
        setPrime2(null);
        setExponent(null);
        setPhi(null);
        setdPrime1(null);
        setdPrime2(null);
        setPrivateKey(null);
        setBigPrime(null);
        setQInv(null);
    }

    RSAKey(BigInteger prime1,
           BigInteger prime2,
           BigInteger exp,
           BigInteger Phi,
           BigInteger pKey) {
        setPrime1(prime1);
        setPrime2(prime2);
        setExponent(exp);
        setPhi(Phi);
        setBigPrime(getPrime1().multiply(getPrime2()));
        PrecomputedPrimes();
        setPrivateKey(pKey);
    }

    boolean isNull(BigInteger num) {
        return num == null;
    }

    BigInteger getPrivateKey() {
        return d;
    }

    void setPrivateKey(BigInteger pub) {
        d = pub;
    }

    BigInteger getPrime1() {
        return p;
    }

    void setPrime1(BigInteger prime1) {
        p = prime1;
    }

    BigInteger getPrime2() {
        return q;
    }

    void setPrime2(BigInteger prime2) {
        q = prime2;
    }

    public BigInteger getExponent() {
        return e;
    }

    public void setExponent(BigInteger exp) {
        e = exp;
    }

    BigInteger getPhi() {
        return phi;
    }

    void setPhi(BigInteger Phi) {
        phi = Phi;
    }

    BigInteger getdPrime1() {
        return dP;
    }

    void setdPrime1(BigInteger crtprime1) {
        dP = crtprime1;
    }

    public BigInteger getBigPrime() {
        return n;
    }

    public void setBigPrime(BigInteger bigPrime) {
        n = bigPrime;
    }

    BigInteger getdPrime2() {
        return dQ;
    }

    void setdPrime2(BigInteger crtprime2) {
        dQ = crtprime2;
    }

    BigInteger getQInv() {
        return qInv;
    }

    void setQInv(BigInteger qinv) {
        qInv = qinv;
    }

    public void PrecomputedPrimes() {
        if (isNull(getExponent())) {
            setdPrime1(null);
            setdPrime2(null);
            setQInv(null);
        }
        setdPrime1(getExponent().modInverse(getPrime1().subtract(BigInteger.ONE)));
        setdPrime2(getExponent().modInverse(getPrime2().subtract(BigInteger.ONE)));
        setQInv(getPrime2().modInverse(getPrime1()));

    }


    public void GenerateKeys() {
        SecureRandom r = new SecureRandom();

        setPrime1(BigInteger.probablePrime(bitlength, r));
        setPrime2(getPrime1().nextProbablePrime());
        setBigPrime(getPrime1().multiply(getPrime2()));
        setPhi((getPrime1().subtract(BigInteger.ONE)).multiply(getPrime2().subtract(BigInteger.ONE)));
        setExponent(BigInteger.probablePrime(bitlength / 2, r));

        while (getPhi().gcd(getExponent()).compareTo(BigInteger.ONE) > 0 && getExponent().compareTo(getPhi()) < 0) {
            getExponent().add(BigInteger.ONE);
        }
        setPrivateKey(getExponent().modInverse(getPhi()));
        PrecomputedPrimes();

    }
}
