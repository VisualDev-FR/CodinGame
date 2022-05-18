import java.util.*;

class Solution {

    public static void main(String z[]) {

        char MESSAGE[] = new Scanner(System.in).nextLine().toCharArray();
        String binaryStr="",mStr=binaryStr;

        for (int mChar:MESSAGE){
            binaryStr+=String.format("%"+7+"s",Integer.toBinaryString(mChar)).replaceAll(" ", "0");
        }

        for(String mBin:binaryStr.split("(?<=(.))(?!\\1)")){
            mStr=mStr!=""?mStr+" ":mStr;
            mStr+=(mBin.charAt(0)=='1'?"0 ":"00 ")+"0".repeat(mBin.length());
        }
        System.out.println(mStr);
    }

}
//Sortcutted:
//import java.util.*;class Solution{public static void main(String z[]){char a[]=new Scanner(System.in).nextLine().toCharArray();String b="",d=b;for(int e:a){b+=String.format("%"+7+"s",Integer.toBinaryString(e)).replaceAll(" ","0");}for(String c:b.split("(?<=(.))(?!\\1)")){d=d!=""?d+" ":d;d+=(c.charAt(0)=='1'?"0 ":"00 ")+"0".repeat(c.length());}System.out.println(d);}}
