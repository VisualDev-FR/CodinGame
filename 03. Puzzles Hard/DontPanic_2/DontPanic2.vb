Module Player

    'Variables publiques

    Private dicFloors as new dictionary (Of Integer, dictionary(Of string, integer))
    Private dicSolutions as new Dictionary (Of Integer, String)
    Private dicLadder as new Dictionary (Of String, Boolean)
    Private dicElevators as new dictionary (Of String, Integer())

    Private nbFloors as integer
    Private exitFloor as integer
    Private exitPos as integer    
    Private nbAdditionalElevators as integer

    Private initialClonePos as integer
    Private initialCloneDir as string

    Private exitBlocked as Boolean
    Private minDist as integer = int32.maxValue
    Private minKey as string = ""
    Private OptimizedKey as string = ""

    Sub Main ()

        Dim inputs as String() = Console.ReadLine().Split(" ")

        nbFloors = inputs(0)
        exitFloor = inputs(3)
        exitPos = inputs(4)
        nbAdditionalElevators = inputs(6)

        Dim width as Integer = inputs(1)
        Dim nbRounds as Integer = inputs(2)
        Dim nbTotalClones as Integer = inputs(5)        
        Dim nbElevators as Integer = inputs(7)

        Dim remainingTurns as integer = nbRounds

        'Initialisation du dictionaire des ascenceurs

        For i as integer = 0 to nbFloors -1           
            dicFloors.add(key:= i, value:=new dictionary(Of string, integer))    
        next i

        'Remplissage du dictionaire des ascenceurs

        For i as Integer = 0 To nbElevators-1

            inputs = Console.ReadLine().Split(" ")

            Dim elevatorFloor as Integer = inputs(0)
            Dim elevatorPos as Integer = inputs(1)

            Dim elevatorKey as string = format(i,"000")

            dicFloors(elevatorFloor).add(elevatorKey, elevatorPos)

            dicElevators.add(elevatorKey, new Integer() {elevatorFloor, elevatorPos})

        Next i

        'Ajout Manuel de la sortie

        dicFloors(exitFloor).add("999", exitPos)
        dicElevators.add("999", new Integer() {exitFloor, exitPos})

        'Analyse de la sortie

        if BlockedExit(exitFloor, exitPos) = True then 
            exitFloor = exitFloor - 1
            exitBlocked = True
            dicElevators.add("599", new Integer() {exitFloor, exitPos})
            nbAdditionalElevators=nbAdditionalElevators-1
        end if

        Console.error.writeline("nbAdditionalElevators = " & nbAdditionalElevators)

        'DEBUG: Affichage des elevators sur chaque étage

        For each k in dicFloors.keys
            Console.error.write(format(k,"00") & " : ")
            for each e in dicFloors(k).keys
                Console.error.write(e & " ")
            next
            console.error.write(vbcrlf)
        next
        
        'Boucle Principale

        While True

            inputs = Console.ReadLine().Split(" ")

            Dim cloneFloor as Integer = inputs(0)
            Dim clonePos as Integer = inputs(1)
            Dim direction as String = inputs(2)          

            Dim action as string = ""
            Dim tPath() as string

            if remainingTurns = nbRounds then

                initialClonePos = clonePos
                initialCloneDir = direction

                minKey = InitDicSolutions(0,"")
                minKey = AddMissingElevators(minKey)

                if nbAdditionalElevators > 0 then Call OptimizeKey(minKey, nbAdditionalElevators)

                Console.error.writeLine("Chemin    : " & join(SplitKey(minKey)," "))        

                For each k in dicSolutions.Keys 'as integer = 0 to 100
                    'console.error.writeline(format(k,"000") & " : " & join(SplitKey(dicSolutions(k))), " ")
                next

                For each k in dicElevators.keys
                    'Console.error.writeline(format(k,"000") & " Floor : " & dicElevators(k)(0) & ", Pos : " & dicElevators(k)(1))
                next                

                tPath = SplitKey(minKey)

                For i as integer = 0 to Ubound(tPath)
                    dim nextElevator as string = tPath(i)            
                    if Cint(nextElevator)>=500 and Cint(nextElevator)<=600 then
                        dicLadder.add(nextElevator,True)
                    end if
                next i

            end if: remainingTurns=remainingTurns-1            

            Select case direction

                case "NONE": action = "WAIT"

                case Else

                    Dim nextElevator as string = tPath(cloneFloor)
                    Dim nextPos as integer = dicElevators(nextElevator)(1)
                    Dim nextDir as string = iif(nextPos>=clonePos, "RIGHT", "LEFT")

                    Dim nextIsElevator as Boolean = Cint(nextElevator)>=500 and Cint(nextElevator)<=600 and dicLadder.containsKey(nextElevator)

                    'Console.error.Writeline(nextDir & " " & direction & " " & math.abs(nextPos-clonePos) & " " & nextIsElevator)

                    if nextDir <> direction and math.abs(nextPos-clonePos) > 0 then
                        action = "BLOCK"
                    elseif math.abs(nextPos-clonePos) > 0 or nextIsElevator = False then
                        action = "WAIT"
                    elseif nextIsElevator = True then
                        action = "ELEVATOR"
                        dicLadder.remove(nextElevator)
                    end if

            end select

            Console.writeline(action)

        End While
    End Sub

    Private Function OptimizeKey(mPath as string, nbAdditionalElevators as integer) as string

        Dim entirePath as string() = SplitKey(mPath)
        
        Dim minDist as integer = int32.maxValue
        Dim minKey as string = ""
        Dim minPos as integer
        Dim minIndex as string

        for i as integer = 0 to ubound(entirePath)-1

            Dim dicoTemp = dicElevators
            Dim pathTemp as string() = SplitKey(mPath)    
            Dim nbElevators as integer = nbAdditionalElevators  

            Dim mNewElevator as string = Cstr(500+nbElevators)
            Dim nextElevatorPos as integer = dicElevators(entirePath(i+1))(1)

            if not dicElevators.containsKey(mNewElevator) then
                dicElevators.add(mNewElevator, new integer()  {i, nextElevatorPos})
            else
                dicElevators(mNewElevator)(1)=nextElevatorPos
            end if

            pathTemp(i)=mNewElevator
            nbElevators=nbElevators-1 

            if nbElevators>0 then
                
                'Call OptimizeKey(join(mPath," "),nbElevators)

            else                               
            
                Dim mNewPath as string = join(pathTemp, "")
                Dim mNewDist as integer = GetTotalBlocks(mNewPath)*GetTotalDist(mNewPath)            

                if mNewDist<=minDist then
                    minDist = mNewDist
                    minKey = mNewPath
                    minPos = nextElevatorPos
                    minIndex = mNewElevator
                end if

            end if

        next i

        console.error.writeline("Optimized : " & join(SplitKey(minKey)," "))

        'dicElevators(minIndex)(1)=minPos

        Return minKey

    End Function

   

    Private Function InitDicSolutions(mFloor as integer, mKey as string) as string 

        if mFloor = exitFloor then

            if exitBlocked then mKey = mKey & "599"

            mKey = Mkey & "999"

            'if nbAdditionalElevators>0 then mKey = OptimizedKey(mKey)

            Dim mCriteria as long = GetTotalDist(mKey)*GetTotalBlocks(mKey)

            if mCriteria < minDist then
                minDist = mCriteria
                minKey = mKey            
            end if

            'dicSolutions.add(dicSolutions.count,mKey )
            mKey = ""

        elseif dicFloors(mFloor).count = 0 and dicFloors.containsKey(mFloor+1) then 

            mKey = mKey & "111"

            Call InitDicSolutions(mFloor+1, mKey)            

        elseif mFloor < exitFloor then

            For each k in dicFloors(mFloor).keys
                Call InitDicSolutions(mFloor+1, mKey & k)
            next

        end if

        Return minKey

    End Function

    Private Function SplitKey(keyToSPlit as string) as string()

        dim mTable(0 to len(keyToSPlit)/3-1) as string

        For i as integer = 0 to len(keyToSPlit)/3-1

            mTable(i) = mid(keyToSPlit,3*i+1,3)

        next i

        Return mTable

    End Function

    Private Function GetTotalBlocks(mPath as string) as integer

        Dim tPath as string() = SplitKey(mPath)
        Dim minBlock as integer = 0
        Dim actualDir as string = ""'initialCloneDir

        on error resume next

        For i as integer = 0 to Ubound(tPath)-1

            if not dicElevators.containsKey(tPath(i)) then i=i+1 

            Dim thisElevator as integer() = dicElevators(tPath(i))
            Dim nextElevator as integer() = dicElevators(tPath(i+1))

            Dim thisPos as integer = thisElevator(1)
            Dim nextPos as integer = nextElevator(1)

            Dim mDir as string = iif(thispos>nextPos, "RIGHT", "LEFT")

            if mDir <> actualDir then 
                actualDir = mDir
                minBlock = minBlock+1
            end if
        next  

        on error goto -1

        Return iif(minBlock>0, minBlock, 1)

    End function    

    Private Function GetTotalDist(mPath as string) as integer

        Dim tPath as string() = SplitKey(mPath)
        Dim minDist as integer = 0'math.abs(dicElevators(tPath(0))(1)-initialClonePos)

        on error resume next

        For i as integer = 1 to Ubound(tPath)-1

            if not dicElevators.containsKey(tPath(i)) then i=i+1 

            Dim thisElevator as integer() = dicElevators(tPath(i))
            Dim nextElevator as integer() = dicElevators(tPath(i+1))

            minDist = minDist + math.abs(thisElevator(1)- nextElevator(1)) 

        next  

        on error goto -1

        Return minDist

    End function

    Private Function AddMissingElevators(mPath as string) as string

        Dim tPath as string() = SplitKey(mPath)

        For i as integer = ubound(tPath)-1 to 0 step -1            

            if tPath(i) = "111" then

                Dim nextElevator as string = tPath(i+1)
                Dim nextPos as integer = dicElevators(nextElevator)(1)

                tPath(i) = format(500 + nbAdditionalElevators,"000")
                nbAdditionalElevators = nbAdditionalElevators - 1

                dicElevators.add(tPath(i), new Integer() {i, nextPos})

            end if

        next i

        Return Join(tPath,"")        

    End Function    

    Private Function BlockedExit(exitFloor as integer, exitPos as integer) as Boolean

        dim rhEl as boolean, lhEl as boolean

        if not dicFloors.containsKey(exitfloor) then return False

        for each elevatorPos in dicFloors(exitfloor).values

            if elevatorPos < exitPos then rhEl = true
            if elevatorPos > exitPos then lhEl = true

        next

        return lhEl = true and rhEl = true

    End function 
'---------------------------------------------------------------------------------------------------------------
'                                       # OLD SUBS
'---------------------------------------------------------------------------------------------------------------    

    Private Function IsPathPossible(mPath as string) as Boolean

        Dim tPath as string() = SplitKey(mPath)

        For i as integer = Lbound(tPath) to Ubound(tPath)-1

            Dim thisElevator as integer() = dicElevators(tPath(i))
            Dim nextElevator as integer() = dicElevators(tPath(i+1))

            Dim thisPos as integer = thisElevator(1)

            Dim nextFloor as integer = nextElevator(0)
            Dim nextPos as integer = nextElevator(1)

            For each e in dicFloors(nextFloor).keys
                
                Dim mPos as integer = dicElevators(e)(1)

                if (mPos <= nextPos and mPos >= thisPos) or (mPos >= nextPos and mPos <= thisPos) then
                    return False
                End if

            next            

        Next i

        Return True

    End Function

    Private Function ChooseElevator(mFloor as integer, exPos as integer, clonePos as integer) as integer

        Dim minDist as integer = int32.maxValue
        Dim choosen as integer = -1

        for each elev in dicFloors(mFloor).keys

            dim mPos as integer = dicFloors(mFloor)(elev)

            if math.abs(mPos-exPos) < minDist then
                minDist = math.abs(mPos-exPos)
                choosen = elev
            end if

        next

        return choosen

    End function    

End Module