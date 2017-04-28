package security;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class MD5Generation {

    private static String getPasswordHash(String original) {

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(original.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashtext = number.toString(16);
            // Zero pad in case the password is small
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            System.out.println("PasswordHash::" + hashtext);
            System.out.println("-------------------------------------------------");
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getHashCode(String pwd, Date dob, String email) {
        Format formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dobStrings = formatter.format(dob);
        System.out.println("Password::" + pwd);
        System.out.println("email::" + dobStrings);
        System.out.println("DoB::" + email);
        return getPasswordHash(pwd + "-" + dobStrings + "-" + email);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        getHashCode("Colors@01", currentDate, "test_1@rsa.com");
        getHashCode("Colors@01", currentDate, "test_2@rsa.com");
    }

}
