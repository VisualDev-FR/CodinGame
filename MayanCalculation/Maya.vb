Module Solution

    Sub Main ()
        
        Dim inputs as String() = Console.ReadLine().Split(" ")        
        Dim L as Integer = inputs(0)
        Dim H as Integer = inputs(1)

        dim trad(0 to 19) as string

        For i as Integer = 0 To H-1
            Dim numeral as String = Console.ReadLine()

            for j as integer = 0 to 19

                trad(j) = trad(j) & mid(numeral,j*4+1,4)

            next j           
        Next

        'for k as integer = 0 to ubound(trad)
        '    console.error.writeline(k & " " & trad(k))
        'next        

        Dim S1 as Integer = Console.ReadLine()

        dim nbToParse1 as string = ""
        dim nbToParse2 as string = ""

        For i as Integer = 0 To S1-1
            Dim num1Line as String = Console.ReadLine()
            nbToParse1 = nbToParse1 & num1Line
            if (i+1) mod 4 = 0 then nbToParse1 = nbToParse1 & " "
        Next

        console.error.writeline(tradFromMaya(nbToParse1,trad))        

        Dim S2 as Integer = Console.ReadLine()

        'console.error.writeline(S1 & " " & S2)

        For i as Integer = 0 To S2-1
            Dim num2Line as String = Console.ReadLine()
            'console.error.writeline(num2Line)
            nbToParse2 = nbToParse2 & num2Line
            if (i+1) mod 4 = 0 then nbToParse2 = nbToParse2 & " "
        Next

        console.error.writeline(tradFromMaya(nbToParse2,trad))

        dim result as integer

        select case Console.ReadLine()

            case "+" : result = tradFromMaya(nbToParse1,trad) + tradFromMaya(nbToParse2,trad)
            case "-" : result = tradFromMaya(nbToParse1,trad) - tradFromMaya(nbToParse2,trad)
            case "*" : result = tradFromMaya(nbToParse1,trad) * tradFromMaya(nbToParse2,trad)
            case "/" : result = tradFromMaya(nbToParse1,trad) / tradFromMaya(nbToParse2,trad)

        end select

        Console.WriteLine(tradToMaya(result, trad))
    End Sub


    Private function tradToMaya (val as integer, trad() as string) as string

    for i = 0 to 14 
        if val < 19 * 20 ^ i then exit for
    next 

    end function

    Private function tradFromMaya (val as string, trad() as string) as integer

        dim tVal() as string = split(val," ")
        dim sumT as integer

        for i as integer = ubound(tval)-1 to 0 step -1 

            for j as integer = 0 to 19

                if tval(i) = trad(j) then sumT = sumT + j * 20 ^ i

            next j

        next i

        tradFromMaya = sumT
    
    end function



End Module