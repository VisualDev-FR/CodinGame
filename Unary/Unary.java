import java.util.*;

class Solution {

    public static void main(String args[]) {

        Scanner in = new Scanner(System.in);
        String MESSAGE = in.nextLine();
        String mStr = "";

        for(int i = 0; i < MESSAGE.length(); i++){

            int mChar = MESSAGE.charAt(i);
            mStr = mStr + GetBinaryString(mChar);
        }

        System.out.println(ConvertToBinary0(mStr));
    }

    static String GetBinaryString(int asciiVal){
        
        String binaryStr = Integer.toBinaryString(asciiVal);

        for(int i = 0; i < 7-binaryStr.length() ;i++){
            binaryStr = "0" + binaryStr;
        }
        return binaryStr;
    }

    static String ConvertToBinary0(String binaryStr){
        
        int currentBin = -1;
        String mStr = "";

        List<String> myList = new ArrayList<String>();

        for(int i = 0; i < binaryStr.length(); i++){

            char mBin = binaryStr.charAt(i);

            if(mBin != currentBin){

                if(mStr != ""){myList.add(mStr);}

                if(mBin == '0'){
                    myList.add("00");
                }else{
                    myList.add("0");
                }
                mStr= "0";
                currentBin = mBin;
            }else{
                mStr = mStr + "0";
            }
        }

        myList.add(mStr);

        return String.join(" ", myList);
    }
}
