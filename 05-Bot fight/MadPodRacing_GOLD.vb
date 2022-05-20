Imports System.math

Module Player

    Private const x as Integer = 0      'x position of your pod
    Private const y as Integer = 1      'y position of your pod
    Private const vx as Integer = 2     'x speed of your pod
    Private const vy as Integer = 3     'y speed of your pod
    Private const angle as Integer = 4  'angle of your pod
    Private const nextID as Integer = 5 'next check point id of your pod
    
    Private const nextX as Integer = 6
    Private const nextY as Integer = 7
    Private const nextDist as Integer = 8
    Private const nextAngle as integer = 9

    Private laps as Integer 
    Private checkpointCount as Integer

    Private checkPoints as Dictionary(of Integer, integer())
    Private opPods as Dictionary(of Integer, String())
    Private myPods as Dictionary(of Integer, String())

    Private accelCurve as String()    

    Sub Main ()

        Call ReadCheckPoints()

        Dim boostUsed as Boolean = False
        Dim playedTurns as Integer = 0

        accelCurve = InitAccelCurve(150)

        While True

            opPods = New Dictionary(of Integer, String())
            myPods = New Dictionary(of Integer, String())

            Call ReadPods()

            For i as integer = 0 to 1

                Dim nextCoord as Integer() = CorrectAngle(myPods(i))
                Dim nextCheckpointAngle as Integer = myPods(i)(nextAngle)

                Dim thrust as string = GetThrust(nextCheckpointAngle)
                'Dim enemyDist as Integer = GetMinEnemyDist(x, y, opponentX, opponentY)

                if playedTurns = 0 and boostUsed = false then 
                    thrust = "BOOST"
                    boostUsed=True
                'elseif enemyDist<=1200 and nextCheckpointDist<=1200 Then
                    'thrust = "SHIELD"
                end if

                Console.WriteLine(nextCoord(0) & " " & nextCoord(1) & " " & thrust)

            Next

            playedTurns=playedTurns+1

        End While
    End Sub

'--------------------------------------------------------------------------------
'                           READING FUNCTIONS
'--------------------------------------------------------------------------------
    Private Sub ReadCheckPoints()
        
        laps = Console.ReadLine()
        checkpointCount = Console.ReadLine()

        checkPoints = New Dictionary(of Integer, integer())         

        For i as Integer = 0 To checkpointCount-1

            Dim inputs as String() = Console.ReadLine().Split(" ")

            checkPoints.Add(i, New Integer(){Cint(inputs(0)), Cint(inputs(1))})

        Next

    End Sub

    Private Sub ReadPods()

        For i as Integer = 0 To 1

            Dim inp as String() = Console.ReadLine().Split(" ")
            
            Dim mNextCheckPoint as Integer() = checkPoints(inp(nextID))

            Dim mX as integer = inp(x)
            Dim mY as Integer = inp(y)

            Dim mNextX as integer = mNextCheckPoint(0)
            Dim mNextY as integer = mNextCheckPoint(1)
            
            Dim mNextDist as Integer = GetDistance(mX, mY, mNextX, mNextY)
            Dim mNextAngle as Integer = GetNextAngle(inp, mNextCheckPoint)

            myPods.Add(i, New String(){inp(0), inp(1), inp(2), inp(3), inp(4), inp(5), mNextX, mNextY, mNextDist, mNextAngle})
            
        Next

        For i as Integer = 0 To 1
            opPods.Add(i, Console.ReadLine().Split(" "))
        Next

    End Sub
'--------------------------------------------------------------------------------
'                           GENERIC FUNCTIONS
'--------------------------------------------------------------------------------
    Private Function GetMinEnemyDist(pod as String()) as Integer

        Dim pod0 as String() = opPods(0)
        Dim pod1 as String() = opPods(1)

        Dim dist0 = GetDistance(pod(x), pod(y), pod0(x), pod0(y))
        Dim dist1 = GetDistance(pod(x), pod(y), pod1(x), pod1(y))

        return Math.min(dist0, dist1)

    End Function

    Private Function GetThrust(nextAngle as Integer) as String

        Dim mAngle as Integer = 180+nextAngle*180/pi

        if mAngle < 0 Then mAngle = mAngle+360 

        DEBUG(mAngle)
    
        return accelCurve(mAngle)
        
    End Function
    
    Private function InitAccelCurve(sigma as integer) as String()

        Dim mGraph(0 to 360) as Double
        Dim tReturn(0 to 360) as String
        Dim maxi as double = -1

        for i as integer = LBound(mGraph) to ubound(mGraph)

            dim x as integer = i-180
            mGraph(i) = math.exp(-0.5*(x/sigma)^2)/(sigma*((2*math.pi)^0.5))
            if mGraph(i) > maxi then maxi = mGraph(i) 

        next i

        for i as integer = LBound(mGraph) to ubound(mGraph)
            tReturn(i) = Cstr(Cint(100*mGraph(i)/maxi))
            if tReturn(i) = "0" then mGraph(i) = "1"
        next i

        return tReturn

    end function

    Private function CorrectAngle_Silver(pod as String(), optional vDist as integer = 600) as integer()

        Dim xa as integer = x
        Dim ya as integer = y

        Dim xb as integer = pod(nextX)
        Dim yb as Integer = pod(nextY)

        Dim D as Double = ((xb-xa)^2+(yb-ya)^2)^0.5
        Dim r as integer = vDist

        Dim teta as double = 2*pod(nextAngle)*math.pi/180
        Dim xc as Integer, yc as Integer, alpha as Double

        alpha = atan2((yb-ya), (xb-xa))       

        xc = xb - r*(cos(teta)*cos(alpha)+sin(teta)*sin(alpha))
        yc = yb - r*(cos(teta)*sin(alpha)-sin(teta)*cos(alpha))        

        return new Integer() {xc, yc}
    
    end function

    Private function CorrectAngle(pod as String(), optional vDist as integer = 600) as integer()

        Dim xa as integer = pod(x)
        Dim ya as integer = pod(y)

        Dim xb as integer = pod(nextX)
        Dim yb as Integer = pod(nextY)

        Dim D as Double = ((xb-xa)^2+(yb-ya)^2)^0.5
        Dim r as integer = vDist

        Dim mVx as Integer = pod(vx)
        Dim mVy as Integer = pod(vy) 

        Dim alpha as Double = atan2((yb-ya), (xb-xa))        
        Dim teta as double = pod(nextAngle)

        Dim xc as Integer = xb - r*(cos(teta)*cos(alpha)+sin(teta)*sin(alpha))
        Dim yc as Integer = yb - r*(cos(teta)*sin(alpha)-sin(teta)*cos(alpha))        

        return new Integer() {xc, yc}
    
    end function    

    Private Function GetNextAngle(pod as String(), nextCheckPoint as Integer()) as Integer

        Dim xa as integer = pod(x)
        Dim ya as integer = pod(y)

        Dim xb as integer = nextCheckPoint(0)
        Dim yb as Integer = nextCheckPoint(1)

        Dim mVx as Integer = pod(vx)
        Dim mVy as Integer = pod(vy) 

        Dim alpha as Double = atan2((yb-ya), (xb-xa))        
        Dim teta as double = -(pi/2) + atan2(mVx*cos(alpha)+mVy*sin(alpha), mVy*cos(alpha)-mVx*sin(alpha))

        Return teta

    End Function

    Private Function GetSpeed(pod as String())
        return (pod(vx)^2+pod(vy^2))^0.5
    End Function

    Public Function GetDistance(xa as integer, ya as integer, xb as integer, yb as integer) as integer
        return ((xA-xB)^2+(yA-yB)^2)^0.5
    End Function

    Public sub DEBUG(message1 as string, optional message2 as String="", optional message3 as String="", optional message4 as String="", optional message5 as String="", optional message6 as String="", optional message7 as String="", optional message8 as String="", optional message9 as String="", optional message10 as String="", optional message11 as String="", optional message12 as String="", optional message13 as String="", optional message14 as String="")
        Console.error.Writeline(message1 & " " & message2 & " " & message3 & " " & message4 & " " & message5 & " " & message6 & " " & message7 & " " & message8 & " " & message9 & " " & message10  & " " & message11  & " " & message12 & " " & message13 & " " & message14)
    End sub 

End Module
