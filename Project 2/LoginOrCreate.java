import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.concurrent.ThreadLocalRandom;
import java.text.DecimalFormat;
import java.util.*;


public class LoginOrCreate { //Main class, prompt user to Create Account or Login/Authenticate

    static final int maxLengthAllowed = 10; //max password length

    //File paths
    static final String plainTextFilePath = "./plainTextFile.txt";
    static final String hashFilePath = "./hashFile.txt";
    static final String saltFilePath = "./saltFile.txt";

    public static void main(String[] args) {
        System.out.println("----- | CS3780 Project 2 | -----");
        System.out.println("Would you like to Create an Account or Authenticate?");

        int choice = readChoice(); //Get user input

        int numAccounts = 0;
        int passLength = 0;        

        //Determine what user wants to do
        if (choice == 1) createAccount();
        else if (choice == 2) authenticate();
        else if (choice == 3) {
            numAccounts = accNum();
            passLength = pass();
            generateUserPassAcc(numAccounts, passLength);
        }
        else System.out.println("Something went wrong!");

    }

    /**
    * Gets the choice from the user with input validation.
    * @return Returns the users input validated choice.
    **/
    private static int readChoice(){ //Get and validate user choice
        int choice=0; //init choice
        Scanner sc = new Scanner(System.in); //scanner for input

        do {

            try {
                System.out.println("Valid choices are:");
                System.out.println("1 Create Account");
                System.out.println("2 Authenticate");
                System.out.println("3 Create Multiple Accounts");
                System.out.println("Please enter your choice:");

                while (!sc.hasNextInt()) {
                    System.out.println("That's not a valid choice, try again:");
                    System.out.println("1 Create Account");
                    System.out.println("2 Authenticate");
                    System.out.println("3 Create Multiple Accounts");
                    sc.next();
                }

                choice = sc.nextInt();
            }
             catch(InputMismatchException e){ //catch exception in case, this should never get called because of the !sc.hasNextInt()
                System.out.println(e);
                System.exit(1);
            }

        } while ((choice != 3) && (choice!=2) && (choice!=1)); //repeat the loop if the user didn't enter 1 or 2

        return choice; //return validated choice
    }

    /**
    * Handles Authenticating/logging in to an account.
    **/
    private static void authenticate(){
        System.out.println("Authenticate using a username and password.");
        String username = getUsername();
        String password = getPassword();

        if(validatePlain(username,password)){ //Plaintext Validation
            System.out.println("Authenticated with Plaintext.");
        }
        else{
            System.out.println("Plaintext Authentication failed.");
        }

        if(validateHash(username,password)){ //Hash Validation
            System.out.println("Authenticated with Hash.");
        }
        else{
            System.out.println("Hash Authentication failed.");
        }

        if(validateSalt(username,password)){ //Salt + Hash Validation
            System.out.println("Authenticated with Salt + Hash.");
        }
        else{
            System.out.println("Salt + Hash Authentication failed.");
        }



    }


    /**
    * Handles Creating an account.
    **/
    private static void createAccount(){ //Create an account

        String filename="./usernames.txt"; //Path to usernames file
        File userFile = new File(filename);
        System.out.println("Create Account.");

        String username = createUsername(); //create a username

        //Check if username is taken
        if(userFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(userFile))) {
                while (br.ready()) {
                     if(br.readLine().equals(username)) {
                        System.out.println("That username is already taken, please restart and try again.");
                        System.exit(2);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Add username to a list to check if it exists before creating the files
        try {
            FileWriter fileWriter = new FileWriter(filename, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(username);
            printWriter.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }

        String password = createPassword(); //create a password

        createFiles(username,password); //Create password files


    }


    /**
    * Prompts the user for a username and validates that it fits
    * the criteria.
    * @return Returns the username as a string.
    **/
    private static String createUsername(){ //Create a valid username from input
        String username="";
        Scanner sc = new Scanner(System.in);

        try{
            System.out.println("Usernames may be up to 10 characters long and consist only of alphanumeric characters.");
            System.out.println("Enter a username:");

            while (!sc.hasNext("[A-Za-z0-9]{1,10}")) { //using regex to validate
                System.out.println("Invalid: Usernames may be up to 10 characters long and consist only of alphabetic characters. Try again:");
                sc.next();
            }
            username = sc.next();
            System.out.println("Username Entered: " + username);

        }
        catch(InputMismatchException e){
            System.out.println(e);
            System.exit(1);
        }

        return username;
    }

    /**
    * Prompts the user for a password and validates the password fits
    * the criteria.
    * @return Returns the password as a string.
    **/
    private static String createPassword(){
        String password="";
        Scanner sc = new Scanner(System.in);

        try{
            System.out.println("Passwords may be up to " + maxLengthAllowed + " long and consist only of the numbers 0-9.");
            System.out.println("Enter a password:");

            while (!sc.hasNext("[0-9]{1,"+maxLengthAllowed+"}")) { //using regex to validate
                System.out.println("Invalid: Passwords may be up to " + maxLengthAllowed + " long and consist only of the numbers 0-9. Try again:");
                sc.next();
            }
            password = sc.next();
            System.out.println("Password Entered: " + password);

        }
        catch(InputMismatchException e){
            System.out.println(e);
            System.exit(1);
        }

        return password;
    }

    /**
    * Handles creating the password files by calling the appropriate methods.
    **/
    private static void createFiles(String username, String password){
        createPlainTextFile(username, password);
        createHashedFile(username,password);
        createSaltFile(username, password);
    }

    /**
    * Creates or appends username/password to plaintext password file.
    **/
    private static void createPlainTextFile(String username, String password){
        String textToAppend = username + "," + password;

        try {
            FileWriter fileWriter = new FileWriter(plainTextFilePath, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(textToAppend);  //New line
            printWriter.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }
    }

    /**
    * Creates or appends username/password to hashed password file.
    **/
    private static void createHashedFile(String username, String password) {
        String textToAppend="";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            String hexPass = byteArrayToHexString(digest);

            textToAppend = username + "," + hexPass;
        }
        catch(NoSuchAlgorithmException n){
            n.printStackTrace();
        }

        try {
            FileWriter fileWriter = new FileWriter(hashFilePath, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(textToAppend);  //New line
            printWriter.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }
    }


    /**
     * Creates or appends username/password to Salt password file.
     **/
    private static void createSaltFile(String username, String password){
        String textToAppend="";
        String hexPass;
        String hexSalt;

        SecureRandom random = new SecureRandom(); //Get a secure random number
        byte[] salt = new byte[1];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            hexPass = byteArrayToHexString(hash); //salted hash pass in hex
            hexSalt = byteArrayToHexString(salt); //hexed Salt value
            textToAppend = username + "," + hexSalt + "," + hexPass; //text to add to file
        }

        catch(NoSuchAlgorithmException n){
            n.printStackTrace();
        }
        catch(InvalidKeySpecException i){
            i.printStackTrace();
        }


        try {
            FileWriter fileWriter = new FileWriter(saltFilePath, true); //Set true for append mode
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.println(textToAppend);  //New line
            printWriter.close();
        }
        catch(IOException io){
            io.printStackTrace();
        }
    }

    /**
    * Converts Byte Array to Hex String
    **/
    public static String byteArrayToHexString(byte[] bytes) {
        BigInteger bigInteger = new BigInteger(1, bytes);
        return bigInteger.toString(16);
    }


    /**
    * Converts Hex Strings to Byte Array
    **/
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

    /**
    * Get a username for authentication
    **/
    private static String getUsername(){
        String username="";
        Scanner sc = new Scanner(System.in);

        try{
            System.out.println("Enter username:");

            while (!sc.hasNext("[A-Za-z0-9]{1,10}")) { //using regex to validate
                System.out.println("Invalid username, try again:");
                sc.next();
            }
            username = sc.next();

        }
        catch(InputMismatchException e){
            System.out.println(e);
            System.exit(1);
        }

        return username;
    }


    /**
    * Get a password for authentication
    **/
    private static String getPassword(){
        String password="";
        Scanner sc = new Scanner(System.in);

        try{

            System.out.println("Enter password:");

            while (!sc.hasNext("[0-9]{1,"+maxLengthAllowed+"}")) { //using regex to validate
                System.out.println("Invalid password, try again:");
                sc.next();
            }
            password = sc.next();

        }
        catch(InputMismatchException e){
            System.out.println(e);
            System.exit(1);
        }

        return password;
    }

    /**
     * Validates against the plain text password file.
     * @return true if validated, false if not.
     */
    private static boolean validatePlain(String username, String password){
        boolean validated = false;
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(plainTextFilePath));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.equals(username+","+password)){
                    validated = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return validated;
    }


    /**
     * Validates against the hash password file.
     * @return true if validated, false if not.
     */
    private static boolean validateHash(String username, String password){
        boolean validated = false;
        String hashPassword="";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            md.update(password.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();

            hashPassword = byteArrayToHexString(digest);


        }
        catch(NoSuchAlgorithmException n){
            n.printStackTrace();
        }


        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(hashFilePath));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.equals(username+","+hashPassword)){
                    validated = true;
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return validated;
    }


    /**
     * Validates against the salt + hash password file.
     * @return true if validated, false if not.
     */
    private static boolean validateSalt(String username, String password){
        boolean validated = false;
        File file = new File(saltFilePath);
        List<String> authList = null; //array containing the username,salt,password
        ArrayList<String> authArrayList = null;

        //get user salt and pass from file
        if(file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                while (br.ready()) {
                    String line = br.readLine();
                    if(line.startsWith(username)) {
                        String[] splitLine = line.split(",");
                        authList = Arrays.asList(splitLine); //store csv row to array
                        authArrayList = new ArrayList(authList);
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        byte[] byteArrSalt = hexStringToByteArray(authArrayList.get(1));

        KeySpec spec = new PBEKeySpec(password.toCharArray(), byteArrSalt, 65536, 128);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            String hexPass = byteArrayToHexString(hash); //salted hash pass in hex
            if(hexPass.equals(authArrayList.get(2))){
                validated = true;
            }
        }

        catch(NoSuchAlgorithmException n){
            n.printStackTrace();
        }
        catch(InvalidKeySpecException i){
            i.printStackTrace();
        }
        return validated;
    }

 //Asks for user to input the number of accounts to be generated
 private static int accNum() {

    int numAcc = 0;

    Scanner sc = new Scanner(System.in);

    System.out.println("Enter the number of accounts: ");
    numAcc = sc.nextInt();

    while(numAcc < 0 || numAcc > Integer.MAX_VALUE) {
        System.out.println("Please enter a valid number of accounts.");
        numAcc = sc.nextInt();
    }

    return numAcc;
}

//Asks for user to input the length of the password
private static int pass() {

    int length = 0;

    Scanner sc = new Scanner(System.in);

    System.out.println("Enter the password length: ");
    length = sc.nextInt();

    while(length < 3 || length > 8) {
        System.out.println("Please enter a valid password length.");
        length = sc.nextInt();
    }

    sc.close();

    return length;
}

//
private static void generateUserPassAcc(int num, int passWord) {

    String base = "user";
    String pass_word;

    for(int i = 0; i < num; i++) {

        String username = "";

        //0000001 format - converts i to string to store
        String temp = Integer.toString(i);

        if(i < 10) {
            username = base + "00000" + temp;
        }
        else {
            username = base + "0000" + temp;
        }

        pass_word = passwords(passWord);

        createFiles(username,pass_word); // from your code so we can send those newly generated accounts to the three files

    }
}

private static String passwords(int length) {

    String password = "";
    String temp = "9";     //placeholder for the largest digit for the range ie 9xxxx - depends on user length (len=4 => 9999)
    String temp2 = "";

    for(int i = 0; i < length - 1; i++) {
        //creates the temp strings as mentioned above
        temp += "9";
    }

    
    for(int i = 0; i < length; i++) {
        temp2 += "0";
    }

    //Converts strings to integers for range
    int newNum2 = Integer.parseInt(temp);

    //Generates random number in user's length range
    int randomNum = ThreadLocalRandom.current().nextInt(0, newNum2);

    DecimalFormat df = new DecimalFormat(temp2);

    password = Integer.toString(randomNum);
   
    password = df.format(randomNum);
    
    return password;
}
    
}
