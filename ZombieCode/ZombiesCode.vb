Module Player

    Private timeMatrix as double(,)

    private const zSpeed as integer = 400
    Private const hSpeed as integer = 1000
    Private x as Integer
    Private y as Integer    

    Sub Main ()
        Dim inputs as String()        
        ' game loop
        While True

            inputs = Console.ReadLine().Split(" ")
            x = inputs(0)
            y = inputs(1)

            Dim humanCount as Integer = Console.ReadLine()

            Dim dicHumans as new Dictionary (Of String, string())
            Dim dicZombies as new Dictionary (Of String, string())

            For i as Integer = 0 To humanCount-1
                Dim humanId as Integer
                Dim humanX as Integer
                Dim humanY as Integer
                inputs = Console.ReadLine().Split(" ")
                humanId = inputs(0)
                humanX = inputs(1)
                humanY = inputs(2)

                dicHumans.Add(i, inputs)

            Next

            Dim zombieCount as Integer = Console.ReadLine()

            For i as Integer = 0 To zombieCount-1
                Dim zombieId as Integer
                Dim zombieX as Integer
                Dim zombieY as Integer
                Dim zombieXNext as Integer
                Dim zombieYNext as Integer
                inputs = Console.ReadLine().Split(" ")
                zombieId = inputs(0)
                zombieX = inputs(1)
                zombieY = inputs(2)
                zombieXNext = inputs(3)
                zombieYNext = inputs(4)

                dicZombies.Add(i, inputs)

            Next

            'Initialisation de la matrice des cibles prioritaires

            redim timeMatrix(0 to humanCount-1, 0 to zombieCount-1)

            for each h in dicHumans.keys
                
                for each z in dicZombies.keys

                    dim cbs as Boolean = CanBeSaved(dicHumans(h), dicZombies(z))
                    dim istgt as Boolean = IsTarget(dicHumans(h), dicZombies(z), 55)

                    if istgt and cbs then
                        timeMatrix(h, z) = GetMinDist(dicHumans(h)) 
                    else if not cbs or not istgt then
                        timeMatrix(h, z) = -1
                    end if

                    'console.error.write(timeMatrix(h, z) & " ")

                next z

                'console.error.write(vbcrlf)

            next

            'console.error.write(vbcrlf)

            if dicZombies.count = 1 then 
            
                x = dicZombies(0)(1)
                y = dicZombies(0)(2)

            elseif dicHumans.count = 1 then 
            
                x = dicHumans(0)(1)
                y = dicHumans(0)(2)                

            else

                Dim humanToSave() as string = dicHumans(TakeDecision(timeMatrix))

                x = humanToSave(1)
                y = humanToSave(2)

            end if             

            Console.WriteLine(x & " " & y) ' Your destination coordinates

        End While

    End Sub

    Private Function AllZombiesAligned() as Boolean

    end function

    Private Function TakeDecision(matrix(,) as double) as integer

        dim minDist as double = int32.maxValue
        dim cannotBeSaved as new Dictionary(Of integer, double)
        dim hToSave as integer

        for i as integer = 0 to ubound(matrix, 1)
        
            for j as integer = 0 to ubound(matrix, 2)

                if matrix(i,j) <> -1 and matrix(i, j) < minDist and not cannotBeSaved.containsKey(i) then

                    minDist = matrix(i, j)
                    hToSave = i

                elseif matrix(i,j) = -1 and not cannotBeSaved.containsKey(i) then

                    cannotBeSaved.add(i, minDist)

                end if

            next j
            
        next     

        return hToSave

    end function

    Private function GetMinDist(human() as string) as Double

        dim xA as integer = human(1)
        dim yA as integer = human(2)        

        dim hDist as double =(((xa-x)^2+(ya-y)^2)^0.5)

        return  hDist

    end function

    private function CanBeSaved(human() as string, zombie() as string) as Boolean      

        dim xA as integer = human(1)
        dim yA as integer = human(2) 

        dim xB as integer = zombie(1)
        dim yB as integer = zombie(2)        

        dim zDist as double =(((xa-xb)^2+(ya-yb)^2)^0.5)
        dim hDist as double =(((xa-x)^2+(ya-y)^2)^0.5)

        if zDist/zSpeed > hDist/hSpeed then
            return true
        else
            return false
        end if

    end function

    private function IsTarget(human() as string, zombie() as string, optional criteria as integer = 20) as Boolean

        dim xA as integer = human(1)
        dim yA as integer = human(2)        

        dim xB as integer = zombie(1)
        dim yB as integer = zombie(2)
        
        dim xC as integer = zombie(3)
        dim yC as integer = zombie(4)

        'Console.error.writeline(xa & "/" & ya & " " & xb & "/" & yb & " " & xc & "/" & yc)

        dim pScal as double = (xa-xb)*(xc-xb)+(yc-yb)*(ya-yb)
        dim pNorm as double =(((xa-xb)^2+(ya-yb)^2)^0.5) * (((xc-xb)^2+(yc-yb)^2)^0.5)
        dim mAngle as double = math.acos(pScal / pNorm)*180/math.pi

        if mAngle >= -criteria and mAngle <= criteria then
            return true
        else
            return false
        end if

    end function
End Module