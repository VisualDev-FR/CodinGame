    Private Function GetMaxGridWidht(dicOfFloors as Dictionary (Of Integer, SortedDictionary (Of Integer, String))) as integer

        Dim maxElevatorByFloor as integer = 0

        For each floor in dicOfFloors.Keys

            if dicOfFloors(floor).count>maxElevatorByFloor then maxElevatorByFloor = dicOfFloors(floor).Count

            Dim mStr as string = ""
            For each elevator in dicOfFloors(floor).Keys            
                mstr = mstr & elevator & " "
            next

            Console.error.writeline(format(floor,"00 : ") & mstr)

        next floor

        Return maxElevatorByFloor    

    End Function
