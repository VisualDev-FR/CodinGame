Module Solution
' Auto-generated code below aims at helping you parse
' the standard input according to the problem statement.
    Private trad() as string
    Private L as Integer
    Private H as Integer
    Private T as String

    Sub Main ()        
        
        L = Console.ReadLine()        
        H = Console.ReadLine()       
        T = Ucase(Console.ReadLine())

        Redim trad (0 to H) 

        For i as Integer = 0 To H-1
            trad(i) = Console.ReadLine() 
        Next


        Dim mStr as string = ""

        for j as integer = 0 to H-1

            for i as integer = 1 to len(T)                
                mStr = mstr & GetLine(mid(T,i,1),j) 
            next

            console.writeline(mStr)
            mstr = ""

        next

    End Sub

    private Function GetLine(mChar as char, line as integer) as string

        dim pos as integer 
        
        if strings.asc(mChar)>=65 and strings.asc(mChar) <=90 then
            pos=(strings.asc(mChar)-65)*L
        else
            pos=26*L
        end if

        'console.error.writeline(mid(trad(line), pos, L))

        return mid(trad(line), pos+1, L)

    end function
End Module
