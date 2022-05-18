Module Player
Sub Main ()
Dim i=Console.ReadLine().Split(" "),s
dim a=cint(i(0)),b=cint(i(1)),c=cint(i(2)),d=cint(i(3))
While True
Console.ReadLine()
s=""
if d>0 and b<d then s="N":d=d-1
if d<18 and b>d then s="S":d=d+1
if c>0 and a<c then s=s &"W":c=c-1
if c<40 and a>c then s=s &"E":c=c+1
Console.WriteLine(s)
End While
End Sub
End Module