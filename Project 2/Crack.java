import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;
import java.util.*;

public class Crack {

    static final String hashFilePath = "./hashFile.txt";
    static final String saltFilePath = "./saltFile.txt";
    
    public static void main(String[] args) throws Exception {

        int file = readFileChoice();
        int length = readPasswordLength();

        //Values to count execution time of cracking a hashed file or salted file
        float startTime = 0;
        float endTime = 0;
        
        if(file == 1) {
            startTime = System.nanoTime();
            readHashedFile(hashFilePath, length);
            endTime = System.nanoTime();
        }
        else if (file == 2) {
            startTime = System.nanoTime();
            readSaltedFile(saltFilePath, length);
            endTime = System.nanoTime();
        }
        System.out.println("Time to Crack: " + (endTime - startTime) / 1000000);
    }


    //Reads user input for which file they would like to crack and verifies input
    private static int readFileChoice() {
        int choice = 0;

        Scanner sc = new Scanner(System.in);

        do {
            System.out.println("1 Hashed Password File");
            System.out.println("2 Hashed + Salted Password File");
            System.out.println("Enter the file to crack: ");
            
            while(!sc.hasNextInt()) {
                System.out.println("That's not a valid choice, try again!");
                System.out.println("1 Hashed Password File");
                System.out.println("2 Hashed + Salted Password File");
                sc.next();
            }

            choice = sc.nextInt();
        } while((choice != 1) && choice !=2);

        return choice;
    }

    /*
    *Retrieves the set length of the password... i.e. user would like to crack all 3 digit passwords
    *in hashed file or salted file
    */
    private static int readPasswordLength() {
        int length = 0;

        Scanner sc = new Scanner(System.in);

        do {
            System.out.println("Enter maximum password size (3-8): ");

            while(!sc.hasNextInt()) {
                System.out.println("That's not a valid choice, try again!");
                System.out.println("Enter maximum password size (3-8): ");
                sc.next();
            }

            length = sc.nextInt();
        }while((length < 3) || (length > 8));

        return length;
    }

    private static void readHashedFile(String fileName, int passLength) {

        String hashPassword = "";
        int numLines = 0;
        int i = 0;
        String temp = "";
        String guess = "";
        int number = 0;
        String solution = "";
        String temp2 = "";

        //Determines how many accounts are present in the hashed file
        try (BufferedReader bread = new BufferedReader(new FileReader(fileName))){
            while(bread.readLine() != null) numLines++;
        }
        catch(IOException e){
            e.printStackTrace();
        }


        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String username[] = new String[numLines];
            String splitLine[] = new String[numLines];
            String hash[] = new String[numLines];
            String line = br.readLine();
            String combined [] = new String[numLines];

            //Stores all the information present in the file to respective arrays
            while(line != null){
                splitLine = line.split(",");
                username[i] = splitLine[0];
                hash[i] = splitLine[1];
                combined[i] = username[i] + "," + hash[i];
                line = br.readLine();
                i++;
            }

            try {

                //Creates padding for the digits. i.e. if length is 5 digits 123 would be 00123
                for(int j = 0; j < passLength; j++) {
                    temp += "0";
                    temp2 += "9";
                }


                DecimalFormat df = new DecimalFormat(temp);
                int temp_2 = Integer.parseInt(temp2);

                int f = 0;
                    while(true) {

                        //Number being guessed is changed to string and then padded with zeroes
                        guess = Integer.toString(number);
                        guess = df.format(number);

                        //Used to exit while loop once there are no more values to check
                        if (f >= username.length){
                            break;
                        }

                        //Hashes the guessed password with SHA-256
                        MessageDigest md = MessageDigest.getInstance("SHA-256");
                        md.update(guess.getBytes(StandardCharsets.UTF_8));
                        byte[] digest = md.digest();
                        hashPassword = byteArrayToHexString(digest);

                        //Checks the current guessed hashed password to all the hashes in the file
                        for(int q = 0; q < hash.length; q++){
                            solution = hash[q];
                            //If guessed password = hashed password then display and increment to next hash
                            if (solution.equals(hashPassword)) {
                                for(int j = 0; j < combined.length; j++){
                                    if(combined[j].contains(hashPassword)){
                                        System.out.println(combined[j] + ": PASSWORD IS: " + number);
                                    }
                                }
                                f++;
                            }
                        }
                    

                        number++;

                        //Used to break from loop if guessed number is greater than digits its seeking
                        if(number > temp_2) {
                            break;
                        }
                    }                       
                }
            catch(NoSuchAlgorithmException n) {
                n.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }     
    }

    private static void readSaltedFile(String fileName, int passLength) {
        String[] passwordHash;
        String[] username;
        String[] salt;
        String[] splitLine;
        String solution = "";
        String temp = "";
        String temp2 = "";
        String guess = "";
        boolean flag = true;
        int numLines = 0;
        int number = 0;

        //Determines how many accounts are present in the hashed file
        try (BufferedReader bread = new BufferedReader(new FileReader(fileName))){
            while(bread.readLine() != null) numLines++;
        }
        catch(IOException e){
            e.printStackTrace();
        }

        passwordHash = new String[numLines];
        username = new String[numLines];
        salt = new String[numLines];
        splitLine = new String[numLines];
        String combined [] = new String[numLines];

        int i = 0;
        int k = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();
            //Stores all the information present in the file to respective arrays
            while(line != null){
                splitLine = line.split(",");
                username[i] = splitLine[0];
                salt[i] = splitLine[1];
                passwordHash[i] = splitLine[2];
                combined[i] = username[i] + "," + salt[i] + "," + passwordHash[i];
                line = br.readLine();
                i++;
            }

            //Creates the size of padding for password lengths
            for(int j = 0; j < passLength; j++) {
                temp += "0";
                temp2 += "9";
            }
    
            //Creates padding for the digits. i.e. if length is 5 digits 123 would be 00123
            DecimalFormat df = new DecimalFormat(temp);
            int temp_2 = Integer.parseInt(temp2);

            while(flag){

                if(k >= passwordHash.length){
                    break;
                }

                //Formats int to string with padding so that it can be hashed
                guess = Integer.toString(number);
                guess = df.format(number);
                solution = passwordHash[k];
    


                byte[] byteArrSalt = hexStringToByteArray(salt[k]);
                KeySpec spec = new PBEKeySpec(guess.toCharArray(), byteArrSalt, 65536, 128);
                
                try {
                    //Decrypts salt
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
                    byte[] hash = factory.generateSecret(spec).getEncoded();
                    String hexPass = byteArrayToHexString(hash);
    
                    //Compares the guessed salted password hash to all of hashes in the file
                    for(int q = 0; q < passwordHash.length; q++){
                        solution = passwordHash[q];
                        //If hash in salted file matches guessed salted hash then display
                        if (solution.equals(hexPass)) {
                            for(int j = 0; j < combined.length; j++){
                                if(combined[j].contains(hexPass)){
                                    System.out.println(combined[j] + ": PASSWORD IS: " + guess);
                                }
                            }
                            //Resets the incrementer so that it can continue guessing values from XXX-passlength
                            number = 0;
                            k++;
                        }
                    }
                    number++;
                    //System.out.println(number);
                    }
                    catch(NoSuchAlgorithmException n) {
                        n.printStackTrace();
                    }
                    catch(InvalidKeySpecException m) {
                        m.printStackTrace();
                    }
                //If incrementor is greater than acceptable pass length for guessing then increment the salt and reset coutner
                if(number > temp_2){
                    k++;
                    number = 0;
                    //Breaks from loop if there are no more passwords of set length
                    if(k >= passwordHash.length){
                        flag = false;
                    }
                }
            }
        }
        catch(IOException e){
            e.printStackTrace();
        } 
        
    }

    //Converts Byte Array to Hex String
    public static String byteArrayToHexString(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return bigInteger.toString(16);
    }

    // Converts Hex Strings to Byte Array
    public static byte[] hexStringToByteArray(String hexString) {
        byte[] byteArray = new BigInteger(hexString, 16)
                .toByteArray();
        if (byteArray[0] == 0) {
            byte[] output = new byte[byteArray.length - 1];
            System.arraycopy(
                    byteArray, 1, output,
                    0, output.length);
            return output;
        }
        return byteArray;
    }

}
