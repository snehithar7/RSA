package com.security.rsa;

import java.math.BigInteger;


/**
 * Created by Manasee on 4/5/17.
 */
public class RSA {
	private RSAKey key;
	
	public RSA() { key = null;}
	
	public RSA (RSAKey k) {key = k; }
	
	boolean isNull(BigInteger num) {return num == null;}
	
	public boolean isInValidEncryptKey() {
		boolean isInValid = isNull (key.getExponent()) ||
							isNull (key.getBigPrime());
		return isInValid;
	}
	
    public byte[] encrypt(byte[] message) {   
    	if (isInValidEncryptKey() || message.length == 0)
			return null;
        return (new BigInteger(message)).modPow(key.getExponent(), key.getBigPrime()).toByteArray(); 
    } 

	
	private boolean isInValidDecryptKey() {
		boolean inValid = isNull(key.getPrime1()) || 
							isNull (key.getPrime2()) ||
							isNull (key.getdPrime1()) ||
							isNull (key.getdPrime2()) ||
							isNull (key.getQInv());
		return inValid;
		
	}

	public byte[] decrypt(byte[] cipher) {
		 BigInteger ciphertext = new BigInteger(cipher);
		if (isNull (ciphertext) || isInValidDecryptKey())
			return null;
		
		BigInteger m1 = ciphertext.modPow(key.getdPrime1(), key.getPrime1());
		BigInteger m2 = ciphertext.modPow(key.getdPrime2(), key.getPrime2());
		BigInteger dm = m1.subtract(m2);
		BigInteger h = key.getQInv().multiply(dm).mod(key.getPrime1());
		BigInteger m = m2.add(h.multiply(key.getPrime2()));
		return m.toByteArray();
		
	} 
}
