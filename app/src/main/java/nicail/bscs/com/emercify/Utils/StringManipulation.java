package nicail.bscs.com.emercify.Utils;

import java.security.MessageDigest;

public class StringManipulation {

    public static String expandUsername(String username){
        return username.replace("."," ");
    }

    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }

    public static String getTags(String string){
        if(string.indexOf("#") > 0){
            StringBuilder sb = new StringBuilder();
            char[] charArray = string.toCharArray();
            boolean foundWord = false;
            for(char c: charArray){
                if(c == '#'){
                    foundWord = true;
                    sb.append(c);
                }
                else{
                    if(foundWord){
                        sb.append(c);
                    }
                }
                if (c == ' ') {
                    foundWord = false;
                }
            }
            String s = sb.toString().replace(" ","").replace("#",",");
            return s.substring(1, s.length());
        }
        return string;
    }

    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
