Module Player

    Sub Main ()

        While True
            
            Dim mIndex as Integer = -1
            Dim maxHeight as integer = 0

            For i as Integer = 0 To 7

                Dim mountainH as Integer = Console.ReadLine()

                if mountainH > maxHeight then 
                    maxheight = mountainH
                    mIndex = i
                end if
                
            Next

            Console.WriteLine(mIndex) 

        End While

    End Sub
End Module