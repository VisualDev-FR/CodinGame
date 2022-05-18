import java.util.*;
import java.io.*;
import java.math.*;

class Solution {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        String operation = in.nextLine();
        int pseudoRandomNumber = in.nextInt();        
        if (in.hasNextLine()) {
            in.nextLine();
        }
        String rotors[] = new String[3];

        rotors[0] = in.nextLine();
        rotors[1] = in.nextLine();
        rotors[2] = in.nextLine();

        String message = in.nextLine();
        System.err.println(message);  

        if (operation.charAt(0)=='E'){

            message = Encode_FirstShift(message, pseudoRandomNumber);            
            message = Encode_Rotor(message, rotors[0]);    
            message = Encode_Rotor(message, rotors[1]);
            message = Encode_Rotor(message, rotors[2]);

            System.out.println(message);

        }else{
                    
            message = Decode_Rotor(message, rotors[2]);    
            message = Decode_Rotor(message, rotors[1]);
            message = Decode_Rotor(message, rotors[0]);
            message = Decode_FirstShift(message, pseudoRandomNumber);    

            System.out.println(message);            

        }
    }

//-------------------------------------------------------------------
//                  DECODING FUNCTIONS
//-------------------------------------------------------------------

    static String Decode_FirstShift (String message, Integer Number){
        
        String mStr = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for(int i =0; i< message.length(); i++){
            
            char mChar = message.charAt(i);            
            int position = (alpha.indexOf(mChar)-Number-i)%26;            

            if (position<0){position += 26;}

            mStr = mStr + alpha.charAt(position);
        }

        return mStr;
    }

    static String Decode_Rotor(String message, String mRotor){

        String mStr = "";

        for(int i = 0; i < message.length(); i++){

            char mChar = Decode_ParseChar(message.charAt(i), mRotor);

            mStr = mStr + mChar;
        }

        return mStr;
    }

    static char Decode_ParseChar(char mChar, String rotor){
        
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int position = rotor.indexOf(mChar);

        return alpha.charAt(position);
    }

//-------------------------------------------------------------------
//                  ENCODING FUNCTIONS
//-------------------------------------------------------------------

    static String Encode_FirstShift (String sentence, Integer Number){ //, String rotor1, String rotor2, String rotor3) {
        
        String mStr = "";
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for(int i =0; i<sentence.length(); i++){
            
            char mChar = sentence.charAt(i);            
            int position = (alpha.indexOf(mChar)+Number+i)%26;            

            mStr = mStr + alpha.charAt(position);
        }

        return mStr;
    }

    static String Encode_Rotor(String message, String mRotor){

        String mStr = "";

        for(int i = 0; i < message.length(); i++){

            char mChar = Encode_ParseChar(message.charAt(i), mRotor);

            mStr = mStr + mChar;
        }

        return mStr;
    }

    static char Encode_ParseChar(char mChar, String rotor){
        
        String alpha = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int position = alpha.indexOf(mChar);

        return rotor.charAt(position);
    }   
}
