import java.util.*;class Solution{public static void main(String x[]){Scanner i=new Scanner(System.in);int n=i.nextInt();int a=999;int b=a;int c=a;for(int j=0;j<n;j++){int t=i.nextInt();int d=Math.abs(t);b=d<a?t:d==a&&t>b?t:b;a=d<a?d:a;}System.out.println(b==c?0:b);}}
