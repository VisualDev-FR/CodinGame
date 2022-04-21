Module p
Sub Main
Dim w as Console,i=w.ReadLine.Split(" "),a=i(7),b(a)
b(a)=i(4)
For j=0 To a-1
i=w.ReadLine.Split(" ")
b(i(0))=i(1)
Next
While 1
i=w.ReadLine.Split(" ")
Dim c=i(0),d=cint(i(1)),e="WAIT",g=i(2)(0),h
if g="N"then h=e else h=iif((d<=b(c)and g="R")or(d>=b(c)and g="L"),e,"BLOCK")
w.WriteLine(h)
End While
End Sub
End Module
