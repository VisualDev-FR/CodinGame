Module Player

    Private timeMatrix as double(,)

    private const zSpeed as integer = 400
    Private const hSpeed as integer = 1000
    Private heroX as Integer
    Private heroY as Integer

    Private Const id as integer = 0
    Private Const x as integer = 1
    Private Const y as integer = 2
    Private Const xNext as integer = 3
    Private Const yNext as integer = 4       

    Sub Main ()
        Dim inputs as String()    

        While True

            inputs = Console.ReadLine().Split(" ")
            heroX = inputs(0)
            heroY = inputs(1)

            Dim humanCount as Integer = Console.ReadLine()

            Dim dicHumans as new Dictionary (Of Integer, string())
            Dim dicZombies as new Dictionary (Of Integer, string())

            For i as Integer = 0 To humanCount-1
                Dim human as String() = Console.ReadLine().Split(" ")

                if Cint(human(x)) <> heroX and Cint(human(y))<>heroY then dicHumans.Add(i, human)
            Next

            Dim zombieCount as Integer = Console.ReadLine()

            For i as Integer = 0 To zombieCount-1
                dicZombies.Add(i, Console.ReadLine().Split(" "))
            Next

            if dicZombies.count = 1 then 

                Console.error.writeline("Focus last Zombie")
            
                heroX = dicZombies(0)(x)
                heroY = dicZombies(0)(y)

            else

                Dim zToFocus(0) as integer

                For each h in dicHumans.values

                    if IsHumanInDanger(h, dicZombies) Then

                        Console.error.writeline("Human cant be saved" & h(x) & "/" & h(y))

                        redim zToFocus(0 to 1)

                        zToFocus(0)=h(x)
                        zToFocus(1)=h(y) 

                        exit for
                    End if

                next

                if ubound(zToFocus)=0 then

                    Console.error.writeline("Go to Max density")

                    Dim positionToGo() as integer = GetMaxZombieDensity(dicZombies)

                    heroX = positionToGo(0)
                    heroY = positionToGo(1)
                
                else

                    Console.error.writeline("Go to human in danger")

                    heroX = zToFocus(0)
                    heroY = zToFocus(1)
                end if

            end if             

            Console.WriteLine(heroX & " " & heroY) ' Your destination coordinates

        End While

    End Sub

    Private Function GetMaxZombieDensity(dicZombies as Dictionary (Of Integer, string())) as integer()

        Dim maxZombies as integer = 0
        Dim xMax as integer = 0
        Dim yMax as integer = 0

        For i as integer = 0 to 16000 step 500

            For j as integer = 0 to 9000 step 500

                Dim nbZombies as integer = 0

                for each z in dicZombies.Values
                    dim xA as integer = i
                    dim yA as integer = j

                    dim xB as integer = z(x)
                    dim yB as integer = z(y)        

                    dim zDist as double =(((xa-xb)^2+(ya-yb)^2)^0.5)

                    if zDist <= 2000 then nbZombies = nbZombies + 1
                next

                if nbZombies > maxZombies then
                    xMax = i
                    yMax = j
                    maxZombies = nbZombies
                end if
            Next
        Next

        return new integer() {xMax, yMax}

    End Function

    Private Function IsHumanInDanger(human() as string, dicZombies as Dictionary (Of Integer, string())) as Boolean

        dim xA as integer = human(1)
        dim yA as integer = human(2) 

        Dim inDanger as Boolean = false

        Console.error.writeline("Checking Human danger")

        For each zombie in dicZombies.Values

            if IsTarget(human, zombie) then

                dim xB as integer = zombie(1)
                dim yB as integer = zombie(2)

                dim zDist as double =(((xa-xb)^2+(ya-yb)^2)^0.5)
                dim hDist as double =(((xa-heroX)^2+(ya-heroY)^2)^0.5)

                Dim deltaTime as double = math.abs(cint(zDist/zSpeed - hDist/hSpeed))

                if deltaTime<10 and deltaTime>0 then inDanger = True

            end if
        Next

        Console.error.writeline("Human" & human(x) & "/" & human(y) & " in danger = " & inDanger)

        return inDanger

    End Function

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
        dim mAngle as double = math.abs(math.acos(pScal / pNorm)*180/math.pi)

        if mAngle >= -criteria and mAngle <= criteria then
            Console.error.writeline("HumanTargeted" & human(x) & "/" & human(y))
            return true
        else
            Console.error.writeline("Human not Targeted" & human(x) & "/" & human(y))
            return false
        end if

    end function

'---------------------------------------------------------------------------
'                           OLD FUNCTIONS
'---------------------------------------------------------------------------

    Private function CanBeSaved(human() as string, zombie() as string) as Boolean      

        dim xA as integer = human(1)
        dim yA as integer = human(2) 

        dim xB as integer = zombie(1)
        dim yB as integer = zombie(2)        

        dim zDist as double =(((xa-xb)^2+(ya-yb)^2)^0.5)
        dim hDist as double =(((xa-heroX)^2+(ya-heroY)^2)^0.5)

        if zDist/zSpeed < hDist/hSpeed then
            Console.error.writeline("Human cant be saved" & human(x) & "/" & human(y))
            return false
        end if

        

        return True

    end function

    Private Function GetZombieCenter(dicZombies as Dictionary (Of Integer, string())) as integer()

        Dim sumX as integer = 0
        Dim sumY as integer = 0

        for each z in dicZombies.Values
            sumX = sumX + z(x)
            sumY = sumY + z(y)
        next

        sumX = sumX / dicZombies.count
        sumY = sumY / dicZombies.count

        return new integer() {sumX, sumY}

    End Function

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
End Module
