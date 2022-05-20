Module Player

Public Const MAPWIDTH as Integer = 4000
Public Const MAPHEIGHT as Integer = 1800
Public Const DRONESPEED as Integer = 100
Public Const DOMRADIUS as Integer = 100

Public nbPlayers as Integer         'number of players in the game (2 to 4 players) 'P
Public myID as Integer              'ID of your player (0, 1, 2, or 3) 'ID
Public nbDronesPerTeam as Integer   'number of drones in each team (3 to 11) 'D
Public nbZones as Integer           'Number of Zones

Public dicZones as Dictionary (Of Integer, Zone)
Public dicDrones as Dictionary (Of Integer, Drone)
Public dicTeams as Dictionary (Of Integer, Team)
Public dicActions as Dictionary (of integer, Zone)

Sub Main ()

    Dim inputs as String() = Console.ReadLine().Split(" ")

    nbPlayers = inputs(0) 
    myID = inputs(1) 
    nbDronesPerTeam = inputs(2) 
    nbZones = inputs(3) 

    'Création des Zones        

    dicZones = New Dictionary (Of Integer, Zone)

    For i as Integer = 0 To nbZones-1

        inputs = Console.ReadLine().Split(" ")
        dicZones.Add(i, New Zone(i, New Integer(){inputs(0), inputs(1)}))

    Next

    While True

        dicDrones = New Dictionary(Of Integer, Drone)
        dicTeams = New Dictionary (Of Integer, Team)

        'Initialisation des zones controllées

        For i as Integer = 0 To nbZones-1

            dicZones(i).controlledBy = Console.ReadLine()

        Next

        'Initialisation des données relatives aux drones et aux équipes

        Dim droneID as Integer = 0

        For i as Integer = 0 To nbPlayers-1

            For j as Integer = 0 To nbDronesPerTeam-1
                
                inputs = Console.ReadLine().Split(" ") ' The first D lines contain the coordinates of drones of a player with the ID 0, the following D lines those of the drones of player 1, and thus it continues until the last player.

                Dim droneX as Integer = inputs(0) 
                Dim droneY as Integer = inputs(1)
                Dim teamID as Integer = i

                dicDrones.Add(droneID, New Drone(droneID, teamID, New Integer() {droneX, droneY}))

                droneID=droneID+1

            Next

            dicTeams.Add(i, New Team(i))

        Next

        'Comptages des drones par zone

        Dim zoneToAttack as New Dictionary (of integer, zone)(dicZones) 
        dicActions = New Dictionary (of integer, Zone)

        For each zone in zoneToAttack.Values
            zone.CountDrones()
            DEBUG(zone.ID, zone.maxEnemies, zone.nbAllies)
        Next

        'Lancement des actions

        Dim remainingDrones as integer = nbDronesPerTeam
        Dim ignoreZone(0 to nbZones-1) as Boolean
        Dim droneKeys as Integer() = dicTeams(myID).Drones.Keys.toArray()
        Dim droneIndex as Integer = 0
        Dim nbDronePerZone as Integer = Cint((nbDronesPerTeam-1)/nbZones)

        for each drone in dicTeams(myID).Drones.Values

            if drone.zone is Nothing Then 'Le drone n'est pas dans une base

                Call SearchZone(drone, zoneToAttack)

            Else 'le drone est dans une base

                Dim droneZone as Zone = drone.zone

                if droneZone.controlledBy = myID Then 'On controle la zone
                    zoneToAttack.remove(droneZone.ID)
                    if droneZone.nbAllies=dronezone.maxEnemies then
                        'on est à égalité => On reste dans la zone
                        dicActions.Add(drone.ID, droneZone)

                    Else
                        'on a la superiorité numérique => on part sur une autre zone
                        
                        Call SearchZone(drone, zoneToAttack)
                        dronezone.RemoveAllies()

                    End if

                Else 'On ne controle pas la zone

                    Call SearchZone(drone, zoneToAttack)
                    droneZone.RemoveAllies()

                End if

            End if

        Next

        For each action in dicActions.Keys

            'DEBUG(action, dicActions(action).ID)

            MOVE(dicActions(action).x, dicActions(action).y)

        Next

        
    End While
End Sub

'--------------------------------------------------------------------------------
'                           DECISION FUNCTIONS
'-------------------------------------------------------------------------------- 

    Private Sub SearchZone(drone as Drone, zoneToAttack as Dictionary (of integer, zone))

        Dim zoneNotDominatedWithoutEnemies as Zone 
        Dim zoneNotDominatedWithMinAllieNeeded as Zone 

        Dim minAllieToWin as Integer = int32.MaxValue

        'Classement des zones par distance par rapport au drone

        Dim dicSorted as New SortedDictionary (of integer, zone)

        For each zone in zoneToAttack.Values

            Dim cDist as integer = GetDistance(drone.x, drone.y, zone.x, zone.y)

            dicSorted.TryAdd(cDist, Zone)            

        Next

        'Analyse des zones, de la plus proche à la moins proche

        For each zone in dicSorted.Values

            Dim allieNeededToWin as Integer = zone.maxEnemies-zone.nbAllies

            if zone.maxEnemies = 0 and zone.controlledBy<>myID and zone.nbAllies=0 Then
                'Il n'y a pas d'ennemi dans la base, et on ne la domine pas => on y va 
                zoneNotDominatedWithoutEnemies = zone
            Elseif allieNeededToWin<minAllieToWin and zone.controlledBy<>myID Then
                'Il y a au moins un enemi dans la zone, on met la zone en mémoire pour la traiter après
                minAllieToWin = allieNeededToWin
                zoneNotDominatedWithMinAllieNeeded=zone
            End if

        Next

        'On choisi la meilleur zone trouvée

        if not zoneNotDominatedWithoutEnemies is Nothing Then

            dicActions.Add(drone.ID, zoneNotDominatedWithoutEnemies)
            
            zoneNotDominatedWithoutEnemies.AddAllies()

        Else if not zoneNotDominatedWithMinAllieNeeded is Nothing then 

            dicActions.Add(drone.ID, zoneNotDominatedWithMinAllieNeeded)

            zoneNotDominatedWithMinAllieNeeded.AddAllies()

        Else

            dicActions.Add(drone.ID, New Zone(-1, New Integer(){MAPWIDTH/2, MAPHEIGHT/2}))

        End if        

    End Sub

'--------------------------------------------------------------------------------
'                           GENERIC FUNCTIONS
'-------------------------------------------------------------------------------- 

    Public Function GetDistance(xa as integer, ya as integer, xb as integer, yb as integer) as integer
        return ((xA-xB)^2+(yA-yB)^2)^0.5
    End Function

'--------------------------------------------------------------------------------
'                           ACTION FUNCTIONS
'--------------------------------------------------------------------------------

    Public Sub MOVE(coordX as Integer, coordY as Integer)
        Console.WriteLine(coordX & " " & coordY)
    End Sub

    Public sub DEBUG(message1 as string, optional message2 as String="", optional message3 as String="", optional message4 as String="", optional message5 as String="", optional message6 as String="", optional message7 as String="", optional message8 as String="", optional message9 as String="", optional message10 as String="")
        Console.error.Writeline(message1 & " " & message2 & " " & message3 & " " & message4 & " " & message5 & " " & message6 & " " & message7 & " " & message8 & " " & message9 & " " & message10)
    End sub

End Module

'--------------------------------------------------------------------------------
'                               OBJECTS
'--------------------------------------------------------------------------------
Class Zone

    Private cCoord as Integer()
    Private cID as Integer
    Private cControlledBy as Integer
    Private cNbEnemies as Integer
    Private cNbAllies as Integer    

    Public Sub New(ID as integer, coords as integer())

        cCoord = coords
        cID = ID
        cControlledBy = controlledBy

    End Sub

    Public Sub CountDrones()

        Dim dicEnemies as New SortedDictionary(of integer, integer)
        Dim maxEnemies as Integer = 0
        Dim maxAllies as Integer = 0

        For each drone in player.dicDrones.Values

            if Player.GetDistance(cCoord(0), cCoord(1), drone.x, drone.y)<=Player.DOMRADIUS Then

                drone.zone = me

                if drone.TeamID = player.myID Then 

                    maxAllies=maxAllies+1

                Elseif dicEnemies.ContainsKey(drone.teamID) Then

                    Dim teamNbEnemies as Integer = dicEnemies(drone.teamID)+1
                    dicEnemies(drone.teamID)=teamNbEnemies
                    maxEnemies=iif(teamNbEnemies>maxEnemies, teamNbEnemies, maxEnemies)

                Else

                    dicEnemies.Add(drone.teamID, 1)
                    maxEnemies=iif(1>maxEnemies, 1, maxEnemies)

                End if

            End if

        Next

        cNbAllies = maxAllies
        cNbEnemies = maxEnemies

    End Sub

    Public Sub AddAllies(optional nbAllies as Integer = 1)
        cNbAllies=cNbAllies+1
    End Sub

    Public Sub RemoveAllies(optional nbAllies as Integer = 1)
        cNbAllies=cNbAllies-1
    End Sub    

    Public Property nbAllies As Integer
        Get
            Return cNbAllies
        End Get

        Set:End Set
    End Property

    Public Property maxEnemies As Integer
        Get
            Return cNbEnemies
        End Get

        Set:End Set
    End Property

    Public Property controlledBy As Integer
        Get
            Return cControlledBy
        End Get

        Set (byVal value as Integer)
            cControlledBy = Value
        End Set
    End Property

    Public Property ID As Integer
        Get
            Return cID
        End Get

        Set:End Set
    End Property    

    Public Property x As Integer
        Get
            Return cCoord(0)
        End Get

        Set:End Set
    End Property

    Public Property y As Integer
        Get
            Return cCoord(1)
        End Get

        Set:End Set
    End Property     

End Class

Class Drone

    Private cCoord as Integer()
    Private cID as Integer
    Private cTeamID as Integer
    Private cZone as Zone

    Public Sub New(ID as integer, teamID as Integer, coords as integer())

        cTeamID = teamID
        cCoord = coords
        cID = ID

    End Sub    

    Public Property teamID as Integer
        Get
            Return cTeamID
        End Get

        Set:End Set
    End Property

    Public Property ID As Integer
        Get
            Return cID
        End Get

        Set:End Set
    End Property      

    Public Property x As Integer
        Get
            Return cCoord(0)
        End Get

        Set:End Set
    End Property

    Public Property y As Integer
        Get
            Return cCoord(1)
        End Get

        Set:End Set
    End Property

    Public Property zone As Zone
        Get
            Return cZone
        End Get

        Set (value as Zone)
            cZone = Value        
        End Set
    End Property     

End Class

Class Team
    
    Private cZones as Dictionary (Of integer, Zone)
    Private cDrones as Dictionary (Of integer, Drone)
    Private cID as Integer

    Public Sub New(ID as Integer)

        cID = ID
        cZones = New Dictionary (Of integer, Zone)
        cDrones = New Dictionary (Of integer, Drone)

        For each drone in player.dicDrones.Values
            if drone.teamID = cID then cDrones.Add(drone.ID, Drone)
        Next

        For each zone in player.dicZones.Values
            if zone.controlledBy = cID then cZones.Add(zone.ID, zone)
        Next        
    End Sub

    Private Sub AddZone(zone as Zone)
        cZones.Add(zone.ID, zone)
    End Sub

    Private Sub RemoveZone(zone as Zone)
        cZones.Remove(zone.ID)
    End Sub

    Public Property ID as Integer
        Get
            Return cID
        End get
        Set:End Set
    End Property

    Public Property zoneCount as Integer
        Get
            Return cZones.count
        End get
        Set:End Set
    End Property

    Public Property Drones as Dictionary (Of integer, Drone)
        Get
            Return cDrones
        End get
        Set:End Set
    End Property

    Public Property Zones as Dictionary (Of integer, Zone)
        Get
            Return cZones
        End get
        Set:End Set
    End Property    
    

End Class
