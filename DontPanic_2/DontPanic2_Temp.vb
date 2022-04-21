Module Player

    'Constantes de pondération
    Private Const V_WEIGHT as integer = 1
    Private Const H_WEIGHT as integer = 1
    Private Const INF_WEIGHT as integer = 99999

    Private Const WIDTH_FORMAT as string = "000"
    Private Const HEIGHT_FORMAT as String = "00"

    Private dicElevators as New SortedDictionary (Of Integer, Integer())
    Private dicBranchs as New Dictionary (Of String, Integer)
    Private dicFloors as New Dictionary (Of Integer, SortedDictionary (Of Integer, String))

    Private openList as New Dictionary (Of String, Node)
    Private closeList as New Dictionary (Of String, Node)

    Sub Main ()

        Dim start as double = timer

        Dim inputs as String() = Console.ReadLine().Split(" ")

        Dim nbFloors as integer = inputs(0)
        Dim width as Integer = inputs(1)
        Dim nbRounds as Integer = inputs(2)        
        Dim exitFloor as integer = inputs(3)
        Dim exitPos as integer = inputs(4)
        Dim nbTotalClones as Integer = inputs(5) 
        Dim nbAddElevators as integer = inputs(6)       
        Dim nbElevators as Integer = inputs(7)

        Dim exitNode as string = format(exitFloor, HEIGHT_FORMAT) & format(exitPos, WIDTH_FORMAT)

        'Initialisation du dictionnaire des étages
        For i as integer = 0 to nbFloors
            dicFloors.Add(i, New SortedDictionary (Of Integer, String))
        Next i

        'Création de deux noeuds par ascenceur (entrée/sortie) + création d'une branche verticale par ascenceur
        For i as Integer = 0 To nbElevators-1

            inputs = Console.ReadLine().Split(" ")
            Dim elevatorFloor as Integer = inputs(0)
            Dim elevatorPos as Integer = inputs(1)

            Call ParseNode(i, elevatorFloor, elevatorPos)

        Next i

        'Création des deux noeuds de la sortie + création de la branche verticale de sortie
        Call ParseNode(nbElevators, exitFloor, exitPos)

        'Début de la boucle principale

        Dim remainingTurns as integer = nbRounds

        While True

            inputs = Console.ReadLine().Split(" ")
            Dim cloneFloor as Integer = inputs(0)
            Dim clonePos as Integer = inputs(1)
            Dim direction as String = inputs(2)            

            'Lancement des algos de pathfinding au premier tour
            If remainingTurns = nbRounds Then

                Dim startX as integer = cloneFloor
                Dim startY as integer = clonePos
                Dim startDir as String = direction

                Dim startNode as String = format(startX, HEIGHT_FORMAT) & format(startY, WIDTH_FORMAT)
                Dim nodeGrid as String(,) = InitNodesGrid(dicElevators, startNode, exitNode, nbFloors, width)

                Dim pathFound as Boolean = False
                Dim allVisited as Boolean = False
                Dim currentNode as Node = New Node()

                With currentNode
                    .X = startX
                    .Y = startY
                    .StartDist = 0
                    '.EndDist = GetDistance(.X, .Y, exitPos, exitFloor)
                End With

                openList(currentNode.Key) = currentNode

                While Not PathFound or Not AllVisited

                    For each curNode in openList.Values
                        
                        Dim rhNode as New Node, lhNode as New Node

                        Dim rhNodeKey as String = format(curNode.X+1, WIDTH_FORMAT) & format(curNode.Y, HEIGHT_FORMAT)
                        Dim lhNodeKey as String = format(curNode.X-1, WIDTH_FORMAT) & format(curNode.Y, HEIGHT_FORMAT)

                        'If not openList.ContainsKey()

                       

                    Next
                    

                End While

            End if:remainingTurns=remainingTurns-1

            Console.writeline("WAIT")

            Console.error.writeLine("mTimer : " & (timer-start)*1000):start=timer

        End While

    End Sub

    Private Function InitNodesGrid(mDicElevators as SortedDictionary (Of Integer, Integer()), startKey as String, exitKey as String, nbFloors as integer, width as Integer) as String(,)

        Dim nodeGrid(0 to nbFloors, 0 to width-1) as String

        For i as Integer = nbFloors to 0 step-1

            Dim mStr as String = ""

            For j as integer = 0 to width-1

                Dim mKey as string = format(i, HEIGHT_FORMAT) & format(j, WIDTH_FORMAT)

                openList.Add(mKey, New Node())

                If mKey = startKey Then
                    nodeGrid(i, j) = "#"
                ElseIf mKey = exitKey then
                    nodeGrid(i, j) = "@"
                ElseIf mDicElevators.ContainsKey(mKey) Then 
                    nodeGrid(i, j) = "*"
                Else
                    nodeGrid(i, j) = "-"
                End if

                mStr = mStr & nodeGrid(i, j) 

            Next j

            Console.error.writeLine(mStr)

        Next i

        Return nodeGrid

    End Function    

    Private Sub ParseNode(index as integer, elevatorFloor as integer, elevatorPos as Integer)

        Dim keyInf as String = format(elevatorFloor, HEIGHT_FORMAT) & format(elevatorPos, WIDTH_FORMAT)
        Dim keySup as String = format(elevatorFloor+1, HEIGHT_FORMAT) & format(elevatorPos, WIDTH_FORMAT)

        dicElevators.TryAdd(keyInf, New Integer(){elevatorPos, elevatorFloor})
        'dicElevators.TryAdd(keySup, New Integer(){elevatorPos, elevatorFloor+1})

        dicBranchs.Tryadd(keyInf, V_WEIGHT)

        dicFloors(elevatorFloor).TryAdd(elevatorPos, keyInf)
        dicFloors(elevatorFloor+1).TryAdd(elevatorPos, keySup)    

    End Sub

    Private Function SplitKey(keyToSPlit as string, mVal as Integer) as string()

        dim mTable(0 to len(keyToSPlit)/mVal-1) as string

        For i as integer = 0 to len(keyToSPlit)/mVal-1

            mTable(i) = mid(keyToSPlit,mVal*i+1,mVal)

        next i

        Return mTable

    End Function

End Module

Class Node

    Private cKey as String
    Private cParent as String
    Private cStartDist as Integer
    Private cEndDist as Integer
    Private cWeight as Integer
    Private cX as integer
    Private cY as integer
    Private cVisited as Boolean

    Private Sub Initialize(x as Integer, y as Integer, parent as String, parentDist as String)
        cKey  = format(x,"000") & format(y, "00")
        cParent = parent
        cX = x
        cY = y
        cStartDist = parentDist+1
        cVisited = True
    End Sub

    Property Parent() As String
        Get
            Return cParent
        End Get
        Set(value As String)
            cParent = value
        End Set
    End Property

    Property StartDist() As Integer
        Get
            Return cStartDist
        End Get
        Set(value As Integer)
            cStartDist = value
        End Set
    End Property

    Property EndDist() As Integer
        Get
            Return cEndDist
        End Get
        Set(value As Integer)
            cEndDist = value
        End Set
    End Property

    Property Weight() As Integer
        Get
            Return cWeight
        End Get
        Set(value As Integer)
            cWeight = value
        End Set
    End Property

    Property X() As Integer
        Get
            Return cX
        End Get
        Set(value As Integer)
            cX = value
        End Set
    End Property

    Property Y() As Integer
        Get
            Return cY
        End Get
        Set(value As Integer)
            cY = value
        End Set
    End Property

    Property Visited() As Boolean
        Get
            Return cVisited
        End Get
        Set(value As Boolean)
            cVisited = value
        End Set
    End Property

    Property Key() As String
        Get
            Return cKey
        End Get
        Set(value As String):End Set
    End Property

End Class