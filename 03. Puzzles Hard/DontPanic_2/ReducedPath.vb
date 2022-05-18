    Private Function ReducedPath(mPath as integer, nbElevators as integer) as string 

        Dim tablePath as string() = SplitKey(mPath)
        
        Dim optiDist as integer = int32.maxValue
        Dim optiKey as string = ""
        Dim optiPos as integer

        for i as integer = 0 to ubound(tablePath)-1

            Dim actualElevator as string = tablePath(i)
            Dim nbElevators as integer = nbAdditionalElevators
            Dim dicoTemp = dicElevators

            If Cint(actualElevator) >= 500 and Cint(actualElevator) >= 600 then

                Return mPath

            else

                tablePath(i)=cstr(500+nbElevators)

                Dim mNewElevator as string = tablePath(i)
                Dim nextElevatorPos as integer = dicoTemp(tablePath(i+1))(1)

                nbElevators=nbElevators-1

                dim mNewKey as string = join(tablePath, "")

                if nbElevators>0 then

                    'mNewKey = ReducedPath(mNewKey, nbElevators)

                else

                    dicoTemp.add(mNewElevator, new integer()  {i, nextElevatorPos})
                    
                    Dim calculatedDist as integer = GetTotalBlocks(mNewKey, dicoTemp)*GetTotalDist(mNewKey, dicoTemp)

                    if optiDist>calculatedDist then
                        optiDist=calculatedDist
                        optiKey= mNewKey
                    end if

                End if

            End if

        next i

        console.error.writeline("Optimized : " & join(SplitKey(minKey)," "))
        Return minKey

    End Function 