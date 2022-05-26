Module Player

    Private Const id as Integer = 0
    Private Const type as Integer = 1           ' 0=monster, 1=your hero, 2=opponent hero
    Private Const x as Integer = 2
    Private Const y as Integer = 3
    Private Const shieldLife as Integer = 4     ' Ignore for this league; Count down until shield spell fades
    Private Const isControlled as Integer = 5   ' Ignore for this league; Equals 1 when this entity is under a control spell
    Private Const health as Integer = 6         ' Remaining health of this monster
    Private Const vx as Integer = 7             ' Trajectory of this monster
    Private Const vy as Integer = 8
    Private Const nearBase as Integer = 9      ' 0=monster with no target yet, 1=monster targeting a base
    Private Const threatFor as Integer = 10     ' Given this monster's trajectory, is it a threat to 1=your base, 2=your opponent's base, 0=neither

    Private Const mapWidth as integer = 17630
    Private Const mapHeight as integer = 9000
    Private Const damageRadius as integer = 800
    Private Const windDist as integer = 1280
    Private Const shieldDist as integer = 2200
    Private Const controlDist as integer =2200    
    Private Const offsetValue as integer = 100

    Private enemyBaseCoord as integer() 
    Private playermana as Integer()
    Private playerHealth as Integer()
    Private attackPosition as Integer()

    Private manaPassed as Boolean
    Private winded as Boolean
    Private attackIndex as Integer
    Private baseRadius as integer
    Private baseX as integer
    Private baseY as integer
    Private offsetForWind as integer
    Private mortalThreat as integer

    Private dicEnty as new SortedDictionary(of integer, String())
    Private dicMstr as new SortedDictionary(of integer, String())
    Private dicOppt as new SortedDictionary(of integer, String())
    Private dicHero as new SortedDictionary(of integer, String())

    Sub Main ()

        Dim inp as String() = Console.ReadLine().Split(" ")

        baseX = inp(0)
        baseY = inp(1)

        Dim attackOffset as integer = 2500        

        enemyBaseCoord = new integer() {iif(baseX=0,17630,0), iif(baseY=0,9000,0)}

        Dim heroesPerPlayer as Integer = Console.ReadLine()

        Dim manaPassed as Boolean = False

        While True

            Redim playerHealth(0 to 1)
            Redim playerMana(0 to 1)

            'Read players data

            For i as Integer = 0 To 1
                inp = Console.ReadLine().Split(" ")
                playerHealth(i) = inp(0)    'Each player's base health
                playerMana(i) = inp(1)      'Ignore in the first league; Spend ten mana to cast a spell
            Next

            Dim entityCount as Integer = Console.ReadLine()

            'Initialize dictionaries
            
            dicEnty = new SortedDictionary(of integer, String())
            dicMstr = new SortedDictionary(of integer, String())
            dicOppt = new SortedDictionary(of integer, String())
            dicHero = new SortedDictionary(of integer, String())

            For i as Integer = 0 To entityCount-1

                Dim entity as String() = Console.ReadLine().Split(" ")

                dicEnty.add(entity(id), entity)

                select case entity(type)
                    case 1: dicHero.add(entity(id), entity)
                    case 0: dicMstr.add(entity(id), entity)
                    case 2: dicOppt.add(entity(id), entity)
                end select

            Next

            'Write clears variables

            Dim heroKeys as integer() = dicHero.Keys.ToArray()
            Dim oppKeys as integer() = dicOppt.Keys.ToArray()
            Dim mstrKeys as integer() = dicMstr.Keys.ToArray()

            Dim hero0 as String() = dicHero(heroKeys(0))
            Dim hero1 as String() = dicHero(heroKeys(1))
            Dim hero2 as String() = dicHero(heroKeys(2))

            Dim nbVulnerableOpp as Integer = 0

            'Calculate the number of dangerous enermies in the base

            For each opp in dicOppt.Values
                if not EnemySide(opp) Then nbVulnerableOpp=nbVulnerableOpp+1
            Next

            'Define the attack positions

            if baseX = 0 Then
                if attackIndex = 0 then 
                    attackPosition = new integer() {16630, 3000}
                Else
                    attackPosition = new integer() {11630, 8000}
                End if
            Else
                if attackIndex = 0 then 
                    attackPosition = new integer() {1000, 6000}
                Else
                    attackPosition = new integer() {6000, 1000}
                End if
            End if

            'Apply as strategy

            Dim hard as Boolean = iif(playerHealth(0)<=1, True, False)

            Call Strategy_Defend(hard, hero0, GetWaitPosition(1, 2, 6000))
            Call Strategy_Defend(hard, hero1, GetWaitPosition(2, 2, 6000))
            Call Strategy_Attack(hero2)

        End While

    End Sub
'--------------------------------------------------------------------------------
'                           OFFENSIVE STRATEGIES
'--------------------------------------------------------------------------------
    Private Sub Strategy_Attack(hero as String())

        Dim nbMortalThreat as Integer = 0
        Dim nbDefenders as Integer = 0
        Dim shouldWind as Boolean = False

        Dim minBaseDist as integer = int32.maxValue
        Dim minEntity as String()

        'Search for the base closer entity

        For each entity in dicEnty.Values

            Dim baseDist as integer = GetDistance(enemyBaseCoord(0), enemyBaseCoord(1), entity(x), entity(y))
            Dim entityDist as Integer = GetDistance(hero(x), hero(y), entity(x), entity(y))
            Dim heroDist as Integer = GetDistance(hero(x), hero(y), enemyBaseCoord(0), enemyBaseCoord(1))

            if baseDist<=minBaseDist and baseDist<=7200 and entity(type)=0 and entity(shieldLife)<=0 Then
                minBaseDist = baseDist
                minEntity = entity
            End if

            if entity(type)=0 and entity(shieldLife)>0 then nbMortalThreat=nbMortalThreat+1
            if entity(type)=2 and baseDist<=6000 then nbDefenders=nbDefenders+1
            if entity(type)=0 and baseDist<=7500 then shouldWind = True

        Next

        'DEBUG("nbMortalThreat", nbMortalThreat, "nbDefenders", nbDefenders, "manas", playermana(0), "winded", winded)
        
        if playermana(0)>=50 and nbMortalThreat>0 and nbDefenders>0 Then

            For each opp in dicOppt.Values

                Dim ctrlDist as Integer = GetDistance(hero(x), hero(y), opp(x), opp(y))
                Dim heroDist as Integer = GetDistance(hero(x), hero(y), enemyBaseCoord(0), enemyBaseCoord(1))
                Dim enemDist as Integer = GetDistance(opp(x), opp(y), enemyBaseCoord(0), enemyBaseCoord(1))

                if ctrlDist<=controlDist and opp(shieldLife)<=0 and heroDist>=enemDist then
                    Call Spell_CONTROL(opp(id), baseX, baseY):winded = False
                    Exit sub
                End if

            Next

        End if

        'Take a decision about what to do

        if minEntity Is Nothing Then

            DEBUG("No monster")

            if winded=True and GetDistance(enemyBaseCoord(0), enemyBaseCoord(1), hero(x), hero(y))>=3500 Then

                Call MOVE(enemyBaseCoord(0), enemyBaseCoord(1)):winded = True

            Else

                Call MOVE(attackPosition(0), attackPosition(1)):winded = False

                if GetDistance(hero(x), hero(y), attackPosition(0), attackPosition(1))<=800 Then attackIndex=1-attackIndex

            End if

        Else        

            Dim canSpell as Boolean = playermana(0)>=10 and minEntity(shieldLife)<=0 
            Dim baseDist as integer = GetDistance(minEntity(x), minEntity(y), enemyBaseCoord(0), enemyBaseCoord(1))
            Dim entityDist as integer = GetDistance(minEntity(x), minEntity(y), hero(x), hero(y))


            if canSpell and entityDist<=controlDist and WillSurvive(minEntity, enemyBaseCoord(0), enemyBaseCoord(1))=True Then

                Call Spell_SHIELD(minEntity(id))

            Elseif canSpell and entityDist<=windDist and minEntity(health)>0 Then 

                Call Spell_WIND(minEntity, hero, enemyBaseCoord(0), enemyBaseCoord(1)):winded = True

            Else

                Dim waitPosition as integer() = AlignWithEntity(minEntity, 801)        
                Call MOVE(waitPosition(0), waitPosition(1)):winded = False 

            End if

        End if
    
    End Sub

    Private Sub Strategy_Defend(hard as Boolean, hero as String(), waitPosition as Integer())

        Dim minBaseDist as integer = int32.maxValue
        Dim minMstr as String()

        'Search for the base-closiest monster

        For each monster in dicMstr.Values

            Dim cDist as integer = GetDistance(baseX, baseY, monster(x), monster(y))

            if cDist<=minBaseDist and EnemySide(monster)=False Then
                minBaseDist = cDist
                minMstr = monster
            End if

        Next

        'Take a decision about what to do

        If minMstr Is Nothing Then

            MOVE(waitPosition(0), waitPosition(1))

        Else

            Dim mstrBaseDist as integer = GetDistance(baseX, baseY, minMstr(x), minMstr(y))
            Dim mstrHeroDist as integer = GetDistance(hero(x), hero(y), minMstr(x), minMstr(y))
            Dim canSpell as Boolean = minMstr(shieldLife)<=0 and playerMana(0)>=10 

            if canSpell and mstrHeroDist<=windDist and WillSurvive(minMstr, baseX, baseY)=true Then                
                Call Spell_WIND(minMstr, hero, enemyBaseCoord(0), enemyBaseCoord(1))
            else
                Call MOVE(minMstr(x), minMstr(y))
            end if

            if dicMstr.Count>1 and not hard Then dicMstr.remove(minMstr(id))

        End if
    
    End Sub

    Private Sub Strategy_WildMana(hero as String(), waitPosition as Integer(), yCriteria as integer)

        Dim minBaseDist as integer = int32.maxValue
        Dim minMstr as String() = Nothing

        'Search for the hero-closiest monster

        For each monster in dicMstr.Values

            Dim cDist as integer = GetDistance(hero(x), hero(y), monster(x), monster(y))

            if cDist<=minBaseDist and math.abs(monster(y)-yCriteria)<(2200/2)+800 Then
                minBaseDist = cDist
                minMstr = monster
            End if

        Next

        'Take a decision about what to do

        If minMstr Is Nothing Then

            MOVE(waitPosition(0), waitPosition(1))

        Else

            Call MOVE(minMstr(x), minMstr(y))
            dicMstr.remove(minMstr(id))

        End if

    End Sub    
'--------------------------------------------------------------------------------
'                           GENERIC FUNCTIONS
'--------------------------------------------------------------------------------
    Private Function ShouldProtect(hero as String()) as Boolean

        For each opp in dicOppt.Values

            if GetDistance(opp(x), opp(y), hero(x), hero(y))<=2300 and hero(shieldLife)<=0 and playerMana(0)>=10 Then return True

        Next

        Return False
    End Function

    Private Function EnemySide(entity as String()) as Boolean

        Dim xEntity as integer = entity(x)
        Dim yEntity as integer = entity(y)

        Dim fx as double = mapHeight - xEntity * (mapHeight)/mapWidth

        if baseX = 0 and baseY = 0 then
            return iif(yEntity>fx, True, False)
        Else
            return iif(yEntity<fx, True, False)
        end if

    End Function

    Private Function GetWaitPosition(heroID as integer, nbHeroesWaiting as integer, baseRadius as integer) as integer()

        Dim midBaseX as integer, midBaseY as integer
        Dim angle as double = heroID * math.pi/(2*(1+nbHeroesWaiting))

        if baseX = 0 and baseY = 0 Then
            midBaseX = baseX+Cint(baseRadius*math.cos(angle))
            midBaseY = baseY+Cint(baseRadius*math.sin(angle))
        Else
            midBaseX = baseX-Cint(baseRadius*math.cos(angle))
            midBaseY = baseY-Cint(baseRadius*math.sin(angle))                 
        End if

        return new integer() {midBaseX, midBaseY}

    End Function  

    Private Function GetDistance(xA as integer, yA as integer, xB as integer, yB as integer) as integer
        return ((xA-xB)^2+(yA-yB)^2)^0.5
    End Function

    Private Function AlignWithEntity(entity as String(), offset as Integer) as Integer()

        Dim xa as integer = entity(x)
        Dim ya as integer = entity(y)

        Dim Ra as double = GetDistance(xa, ya, enemyBaseCoord(0), enemyBaseCoord(1))
        Dim Rb as double = Ra+offset

        Dim alpha as Double = iif(baseX<>0, math.acos((xa-enemyBaseCoord(0))/Ra), math.acos((enemyBaseCoord(0)-xa)/Ra))

        Dim xb as integer = iif(baseX<>0, enemyBaseCoord(0)+Rb*math.cos(alpha), enemyBaseCoord(0)-Rb*math.cos(alpha))
        Dim yb as integer = iif(baseX<>0, enemyBaseCoord(1)+Rb*math.sin(alpha), enemyBaseCoord(1)-Rb*math.sin(alpha)) 

        Return New Integer() {xb, yb}

    End Function

    Private Function GetCloserEntity(fromX as Integer, fromY as Integer, dico as SortedDictionary(of integer, String())) as String()

        Dim minId as integer
        Dim minDist as integer = int32.MaxValue

        For each entity in dico.Values

            Dim entityDist as integer = GetDistance(entity(x), entity(y), fromX, fromY)

            if entityDist<minDist Then
                minID = entity(id)
                minDist = entityDist
            End if

        Next

        Return dico(minID)

    End Function

    Private Function GetFurtherEntity(fromX as Integer, fromY as Integer, dico as SortedDictionary(of integer, String())) as String()

        Dim maxId as integer
        Dim maxDist as integer = int32.MinValue

        For each entity in dico.Values

            Dim entityDist as integer = GetDistance(entity(x), entity(y), fromX, fromY)

            if entityDist>maxDist Then
                maxId = entity(id)
                maxDist = entityDist
            End if

        Next

        Return dico(maxId)

    End Function

    Private Function WillSurvive(monster as String(), coordX as integer, coordY as integer) as Boolean

        Dim baseDist as Integer = GetDistance(monster(x), monster(y), coordX, coordY)
        
        Dim maxTime as Double = baseDist/400
        Dim minHealth as Double = monster(health) - maxTime*2

        Return iif(minHealth>0, True, False)


    End function

    Private Function IsAligned(hero as String(), entity as String()) as Boolean

        Dim xa as integer = hero(x)-enemyBaseCoord(0)
        Dim ya as integer = hero(y)-enemyBaseCoord(1)

        Dim xb as integer = entity(x)-enemyBaseCoord(0)
        Dim yb as integer = entity(y)-enemyBaseCoord(1)

        return iif(xa*yb-xb*ya=0, True, False)

    End Function

'--------------------------------------------------------------------------------
'                           ACTION FUNCTIONS
'--------------------------------------------------------------------------------
    Private sub WAIT()
        Console.Writeline("WAIT")
    End sub

    Private sub MOVE(coordX as integer, coordY as integer, optional heroID as String="")
        Console.WriteLine("MOVE " & coordX & " " & coordY & " " & heroID)        
    End sub

    Private Sub Spell_SHIELD(id as integer, optional heroID as String="")
        Console.WriteLine("SPELL SHIELD " & id & " " & heroID)
        playerMana(0)=playerMana(0)-10
    end sub

    Private sub Spell_WIND(entity as String(), hero as String(), coordX as integer, coordY as integer, optional heroID as String="")

        Dim xc as integer = coordX-entity(x)+hero(x)
        Dim yc as integer = coordY-entity(y)+hero(y)

        Console.WriteLine("SPELL WIND " & xc & " " & yc & " " & heroID)
        playerMana(0)=playerMana(0)-10
    End sub

    Private Sub Spell_CONTROL(id as integer, coordX as integer, coordY as integer, optional heroID as String="")
        Console.WriteLine("SPELL CONTROL " & id & " " & coordX & " " & coordY & " " & heroID)
        playerMana(0)=playerMana(0)-10
    End Sub

    Private sub DEBUG(message1 as string, optional message2 as String="", optional message3 as String="", optional message4 as String="", optional message5 as String="", optional message6 as String="", optional message7 as String="", optional message8 as String="", optional message9 as String="", optional message10 as String="")
        Console.error.Writeline(message1 & " " & message2 & " " & message3 & " " & message4 & " " & message5 & " " & message6 & " " & message7 & " " & message8 & " " & message9 & " " & message10)
    End sub
End Module
