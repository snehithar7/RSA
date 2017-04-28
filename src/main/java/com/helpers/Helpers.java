package com.helpers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alec.ferguson on 4/27/2017.
 */
public class Helpers {
    public static String inStreamToJson(InputStream in) {
        try {
            BufferedReader streamReader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            return responseStrBuilder.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
