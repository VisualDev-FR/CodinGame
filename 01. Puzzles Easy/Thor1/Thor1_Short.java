import java.util.*;

class Player {

    public static void main(String args[]) {
        Scanner i = new Scanner(System.in);
        int a=i.nextInt(),b=i.nextInt(),c=i.nextInt(),d=i.nextInt();
        
        for(;;){
            i.nextInt();
            boolean e=d>0&&b<d,f=d<18&&b>d,g=c>0&&a<c,h=c<40&&a>c;            
            System.out.println((e?"N":f?"S":"")+(g?"W":h?"E":""));
            d+=(f?1:(e?-1:0));
            c+=(h?1:(g?-1:0));

        }
    }
}

//Shorcutted :
//import java.util.*;class Player {public static void main(String args[]) {Scanner i=new Scanner(System.in);int a=i.nextInt(),b=i.nextInt(),c=i.nextInt(),d=i.nextInt();for(;;){i.nextInt();boolean e=d>0&&b<d,f=d<18&&b>d,g=c>0&&a<c,h=c<40&&a>c;System.out.println((e?"N":f?"S":"")+(g?"W":h?"E":""));d+=(f?1:(e?-1:0));c+=(h?1:(g?-1:0));}}}
