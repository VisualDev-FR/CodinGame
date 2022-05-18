Module Solution

    public trad() as string
    public L as integer
    public H as integer
    public base as integer

    Sub Main ()
        
        Dim inputs as String() = Console.ReadLine().Split(" ")        
        L = inputs(0)
        H = inputs(1)

        For i as Integer = 0 To H-1

            Dim numeral as String = Console.ReadLine()

            if i = 0 then 
                base = len(numeral)/L
                redim preserve trad(0 to base-1) 
            end if

            for j as integer = 0 to base-1
                trad(j) = trad(j) & mid(numeral,j*L+1,L)
            next j  

        Next

        'for k as integer = 0 to ubound(trad)
        '    console.error.writeline(k & " " & trad(k))
        'next    

        'exit sub    

        Dim S1 as Integer = Console.ReadLine()

        dim nbToParse1 as string = ""
        dim nbToParse2 as string = ""

        For i as Integer = 0 To S1-1
            Dim num1Line as String = Console.ReadLine()
            nbToParse1 = nbToParse1 & num1Line
            if (i+1) mod L = 0 then nbToParse1 = nbToParse1 & " "
        Next

        'console.error.writeline("S1 : " & tradFromMaya(nbToParse1))        

        Dim S2 as Integer = Console.ReadLine()

        'console.error.writeline(S1 & " " & S2)

        For i as Integer = 0 To S2-1
            Dim num2Line as String = Console.ReadLine()
            'console.error.writeline(num2Line)
            nbToParse2 = nbToParse2 & num2Line
            if (i+1) mod L = 0 then nbToParse2 = nbToParse2 & " "
        Next

        'console.error.writeline("S2 : " & tradFromMaya(nbToParse2))

        dim result as long
        dim operation as string = Console.ReadLine()

        select case operation

            case "+" : result = tradFromMaya(nbToParse1) + tradFromMaya(nbToParse2)
            case "-" : result = tradFromMaya(nbToParse1) - tradFromMaya(nbToParse2)
            case "*" : result = tradFromMaya(nbToParse1) * tradFromMaya(nbToParse2)
            case "/" : result = tradFromMaya(nbToParse1) / tradFromMaya(nbToParse2)

        end select

        console.error.writeline(tradFromMaya(nbToParse1) & operation & tradFromMaya(nbToParse2) & " = " & result)
        
        call WriteMaya(ParseValue(result))

    End Sub


    private function WriteMaya (tValues() as integer) as string

        dim mStr as string = ""

        'console.error.writeline(ubound(tValues))

        for i as integer = ubound(tValues) - 1 to 0 step -1

            dim mValue as long = tValues(i)
            dim mTrad as string = trad(mValue)            

            for j as integer = 0 to len(mTrad) - 1

                mstr = mstr & mid(mTrad,j+1,1)

                if (j+1) mod L = 0 then                 
                    console.writeline(mstr)
                    mstr = ""
                end if

            next j

        next i 

        'console.error.writeline("final : " & vbcrlf & mstr)

        WriteMaya = mStr

    end function

    Private function ParseValue (val as long) as integer()

        dim mMod as long, index as integer = 0

        do while mMod <> val

            index = index + 1
            mMod = val mod (base ^ index)            

        loop 

        dim cMod(0 to index) as long, cFinal(0 to index) as integer

        if index = 0 then redim cFinal(0 to 1)

        for i as integer = index to 0 step -1

            if i = index then
                cmod(i) = val
                cFinal(i) = 0
            else
                cmod(i) = cmod(i+1) mod (base ^ i) 
                cFinal(i) = (cmod(i+1)-cmod(i))/(base ^ i)
            end if

            'console.error.writeline(i & " " & cmod(i) & " " & cFinal(i))

        next

        ParseValue = cFinal

    end function

    Private function tradFromMaya (val as string) as long

        dim tVal() as string = split(val," ")
        dim sumT as integer

        'console.error.writeline("L = " & ubound(tval))

        for i as integer = ubound(tval)-1 to 0 step -1

            for j as integer = 0 to base - 1 

                if tval(i) = trad(j) then sumT = sumT + j * (base ^ (ubound(tval)-1 - i))

            next j

        next i

        tradFromMaya = sumT
    
    end function

End Module
