Module Player
' Auto-generated code below aims at helping you parse
' the standard input according to the problem statement.
' ---
' Hint: You can use the debug stream to print initialTX and initialTY, if Thor seems not follow your orders.

    Sub Main ()
        
        Dim inputs as String()
        Dim lightX as Integer ' the X position of the light of power
        Dim lightY as Integer ' the Y position of the light of power
        Dim initialTX as Integer ' Thor's starting X position
        Dim initialTY as Integer ' Thor's starting Y position
        inputs = Console.ReadLine().Split(" ")
        lightX = inputs(0)
        lightY = inputs(1)
        initialTX = inputs(2)
        initialTY = inputs(3)

        dim direction as string = ""

        ' game loop
        While True
        
            Dim remainingTurns as Integer
            remainingTurns = Console.ReadLine() ' The remaining amount of turns Thor can move. Do not remove this line.

            dim movX as integer = lightX - initialTX
            dim movY as integer = lightY - initialTY

            dim movN as boolean = movY < 0 and initialTY > 0
            dim movS as boolean = movY > 0 and initialTY < 18
            dim movE as boolean = movX > 0 and initialTX < 40
            dim movW as boolean = movX < 0 and initialTX > 0

            if movN then
                direction = "N"
                initialTY = initialTY - 1
            elseif movS then
                direction = "S"
                initialTY = initialTY + 1
            end if

            if movE then
                direction = direction & "E"
                initialTX = initialTX + 1 
            elseif movW then
                direction = direction & "W"
                initialTX = initialTX -1
            end if

            ' A single line providing the move to be made: N NE E SE S SW W or NW
            Console.WriteLine(direction)

            direction = ""

        End While
    End Sub
End Module