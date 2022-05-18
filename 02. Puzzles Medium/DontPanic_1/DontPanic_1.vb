Module Player

    Sub Main ()
        Dim inputs as String()

        Dim nbFloors as Integer ' number of floors
        Dim width as Integer ' width of the area
        Dim nbRounds as Integer ' maximum number of rounds
        Dim exitFloor as Integer ' floor on which the exit is found
        Dim exitPos as Integer ' position of the exit on its floor
        Dim nbTotalClones as Integer ' number of generated clones
        Dim nbAdditionalElevators as Integer ' ignore (always zero)
        Dim nbElevators as Integer ' number of elevators

        inputs = Console.ReadLine().Split(" ")
        nbFloors = inputs(0)
        width = inputs(1)
        nbRounds = inputs(2)
        exitFloor = inputs(3)
        exitPos = inputs(4)
        nbTotalClones = inputs(5)
        nbAdditionalElevators = inputs(6)
        nbElevators = inputs(7)

        dim dicElevator as new Dictionary(Of Integer, Integer)

        For i as Integer = 0 To nbElevators-1
            Dim elevatorFloor as Integer ' floor on which this elevator is found
            Dim elevatorPos as Integer ' position of the elevator on its floor
            
            inputs = Console.ReadLine().Split(" ")
            elevatorFloor = inputs(0)
            elevatorPos = inputs(1)

            dicElevator.add (key:=elevatorFloor, value:=elevatorPos)

        Next

        ' game loop
        While True

            Dim cloneFloor as Integer ' floor of the leading clone
            Dim clonePos as Integer ' position of the leading clone on its floor
            Dim direction as String ' direction of the leading clone: LEFT or RIGHT

            inputs = Console.ReadLine().Split(" ")

            cloneFloor = inputs(0)
            clonePos = inputs(1)
            direction = inputs(2)

            dim mDir as string

            if cloneFloor <> exitFloor and cloneFloor <> -1 then

                mDir = iif(clonePos < dicElevator(cloneFloor), "RIGHT","LEFT")

                if direction = mDir or clonePos = dicElevator(cloneFloor) then

                    Console.WriteLine("WAIT") ' action: WAIT or BLOCK

                else    

                    Console.WriteLine("BLOCK") ' action: WAIT or BLOCK

                end if                

            else

                mDir = iif(clonePos < exitPos, "RIGHT","LEFT")

                if direction = mDir or clonePos = exitPos then

                    Console.WriteLine("WAIT") ' action: WAIT or BLOCK

                else    

                    Console.WriteLine("BLOCK") ' action: WAIT or BLOCK

                end if                

            end if



        End While
    End Sub
End Module
