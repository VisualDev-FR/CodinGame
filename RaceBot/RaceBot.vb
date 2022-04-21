Module Player

    Private x as Integer 
    Private y as Integer

    Private nextCheckpointX as Integer
    Private nextCheckpointY as Integer 

    Private nextCheckpointDist as Integer
    Private nextCheckpointAngle as Integer 

    Sub Main ()
        
        Dim accelCurve() as double = GetAccelCurve(100)

        While True

            'CheckPoints Inputs

            dim inputs() as string = Console.ReadLine().Split(" ")

            x = inputs(0)
            y = inputs(1)

            nextCheckpointX = inputs(2)
            nextCheckpointY = inputs(3)

            nextCheckpointDist = inputs(4)
            nextCheckpointAngle = inputs(5)

            console.error.writeline(join(inputs," "))

            'Opponent Inputs

            inputs = Console.ReadLine().Split(" ")

            Dim opponentX as Integer = inputs(0)
            Dim opponentY as Integer = inputs(1)

            dim thrust as string = cstr(accelCurve(nextCheckpointAngle + 180))

            Dim nextCoord as integer() = CorrectAngle(600)

            if nextCheckpointDist > 3000 and nextCheckpointAngle = 0 then thrust = "BOOST"
            
            Console.WriteLine(nextCoord(0) & " " & nextCoord(1) & " " & thrust)

        End While
    End Sub

    Private function GetAccelCurve(sigma as integer) as double()

        Dim mGraph(0 to 360) as double
        Dim maxi as double = -1

        for i as integer = LBound(mGraph) to ubound(mGraph)

            dim x as integer = i - 180
            mGraph(i) = math.exp(-0.5*(x/sigma)^2)/(sigma*((2*math.pi)^0.5))
            if mGraph(i) > maxi then maxi = mGraph(i) 

        next i

        for i as integer = LBound(mGraph) to ubound(mGraph)
            mGraph(i) = cint(100*mGraph(i)/maxi)
            if mGraph(i) = 0 then mGraph(i) = 1
        next i        

        return mGraph

    end function

    Private function CorrectAngle(optional vDist as integer = 300) as integer()

        dim coord(0 to 1) as integer

        coord(0)=nextCheckpointX + (vDist*math.cos(nextCheckpointAngle*math.pi/30))
        coord(1)=nextCheckpointY - (vDist*math.sin(nextCheckpointAngle*math.pi/30))

        return coord

    end function

  

End Module