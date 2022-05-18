    Private Function ParseGrid(dicOfFloors as Dictionary (Of Integer, SortedDictionary (Of Integer, String))) as integer()

        Dim maxElevatorByFloor as integer = 0
        Dim gridPositionsTemp as New SortedDictionary (Of Integer, Integer)
        Dim gridPositions as New SortedDictionary (Of Integer, Integer)

        For each floor in dicOfFloors.Keys

            if dicOfFloors(floor).count>maxElevatorByFloor then maxElevatorByFloor = dicOfFloors(floor).Count

            For each elevatorPos in dicOfFloors(floor).Keys
                gridPositionsTemp.TryAdd(elevatorPos, gridPositionsTemp.count)
            Next

            'Dim mStr as string = ""
            'For each elevator in dicOfFloors(floor).Keys            
            'mstr = mstr & elevator & " "
            'next:Console.error.writeline(format(floor,"00 : ") & mstr)
        next floor

        Dim i as integer = 0
        for each pos in gridPositionsTemp.Keys            
            gridPositions.TryAdd(gridPositions.Count, pos)
            i=i+1
        next
        gridPositionsTemp = Nothing

        Dim mGrid(0 to dicFloors.count-1, 0 to maxElevatorByFloor-1) as integer

        For i as integer 0 to Ubound(mGrid,1)

            For j as integer = 0 to Ubound(mGrid,2)

                if dicFloors(i).containsKey(j)




            next j

        next i

    End Function
