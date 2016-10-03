; Virus Game
; James Laderoute (c)
; wheezingjim@comcast.net
;

Include "split.bb"
Include "wallobj.bb"
Include "keys.bb"

Const ENTITY_TYPE_CAMERA=1
Const ENTITY_TYPE_VIRUS=3


Const GRAPHICS_WIDTH=640, GRAPHICS_HEIGHT=480


Global WINDOW_WIDTH=GRAPHICS_WIDTH, WINDOW_HEIGHT=GRAPHICS_HEIGHT
Global WINDOW_DX=0, WINDOW_DY=0

Const STANDING=1, WALKING=2, EXPLODING=3, GRINING=4
Const FRAME_SPEED_MS=300


Const WEAPON_BEAM=1
Type weapon
	Field style     ; style of weapon
	Field on		; when true then draw graphic for this
	Field flash		; white out the screen - to show a flash of light
	Field onfor		; millisecs - when to turn weapon graphics off
	Field wx,wy		; mouse x,y when fired weapon
	Field soundFX
End Type

Type mark
	Field entity
	Field ms
End Type

Type virus
	Field name$
	Field action
	Field entity
	Field life#
	Field frame,frame_ms
End Type

;
; Holds a list of various virus characters
;
Type vlist
	Field name$
	Field texture
	Field stand_start, stand_end
	Field walk_start, walk_end
	Field die_start, die_end
	Field grin_start, grin_end
End Type




;
; Put the game into 3D Graphics Mode now
; =================================================================================
;
Graphics3D GRAPHICS_WIDTH, GRAPHICS_HEIGHT, 16


AppTitle "Shoot The Viruses","Really Quit?"

AmbientLight 0,0,50


soundFX1 = LoadSound("media\weapon1.mp3")

Global WEAPON.weapon = New weapon
WEAPON\on = False
WEAPON\onfor = 0
WEAPON\style = WEAPON_BEAM
WEAPON\soundFX = soundFX1



Global cursorSprite = LoadImage( "media\cursor.png")
HandleImage cursorSprite,32,30


;
; Loading sprites must come after going into Graphics3D mode
;
Global markSprite = LoadSprite("media\mark.png", 4)
SpriteViewMode markSprite, 2
EntityAlpha markSprite,0
EntityColor markSprite,255,0,0

;         NAME             FILENAME             STAND       WALK        DIE         GRIN
addVlist( "Jimmy",      "media\virus1.png" ,    4, 4,  	    0, 1,  	    8, 9,       4, 4   )
addVlist( "Kevin",      "media\virus2.png" ,    0, 0,  	    0, 3,  	    4, 7,       8, 10  )
addVlist( "George",     "media\virus3.png" ,    0, 0,  	    0, 3,  	    4, 6,       4, 4   )


Global filein = ReadFile( "virusobjects.txt" )
While Not Eof(filein)

	s$ = ReadLine$(filein) ; NAME X Y

	parseLine( s$, " ")
	
	name$ = pLine$(0)
	x = pLine$(1)
	y = pLine$(2)

	makeVirus( name$, x, y )
			
Wend
CloseFile(filein)


;makeVirus( "Jimmy", -8, 15 )
;makeVirus( "Jimmy", 1, 23 )
;makeVirus( "Jimmy", -31, 4 )
;makeVirus( "Kevin", 10, -11 )
;makeVirus( "George", -20, 23)




;
; Setup collision types here. must be done sometime after the call to
; Graphics3D.
;
ClearCollisions()
Collisions ENTITY_TYPE_CAMERA,ENTITY_TYPE_WALL,3,3
Collisions ENTITY_TYPE_VIRUS,ENTITY_TYPE_WALL,3,3



SetBuffer BackBuffer()


wall_texture = LoadTexture("media/organs.jpg")

Global pl=CreatePlane(1)
PositionEntity pl,0,0,0
RotateEntity pl,0,0,0
EntityAlpha pl,0.6
EntityPickMode pl,2
EntityTexture pl,wall_texture


Local gfilename$ = "design.txt"

ReadWallDesign( gfilename$ )

;
; Setup the camera
;
Global camPivot = CreatePivot()
Global cam=CreateCamera( camPivot )
CameraRange cam,.1,1000
resetCamera()

lit=CreateLight( 2, cam )
LightRange(lit, 10.0)


;
; Here is the main game loop. This updates and draws
; the game screens.
;
While Not KeyDown(KEY_ESCAPE)

;	tim = CreateTimer( 60 )  ; try to keep refresh rate to 60 fps

	userKeyboardInput()
	userMouseInput()

	updateMarks()
	updateVirus()
	
	UpdateWorld
	RenderWorld
	
	drawWeaponCursor()
	drawWeaponGraphic()
	
	Flip
	
;	WaitTimer( tim )		; for controlling refresh rate
;	FreeTimer( tim)

Wend

FreeEntity pl
FreeEntity cam
EndGraphics
End

Function updateVirus()
	Local o.virus=Null
	Local vobj.vlist = Null
	Local frame=0
	Local texture=0
	
	For o = Each virus
	
	
		vobj = findVlist( o\name$ )

		texture = vobj\texture
		
		Select o\action
			Case STANDING
				EntityTexture o\entity,texture,vobj\stand_start
				If MilliSecs() > o\frame_ms Then
					o\frame = o\frame + 1
					o\frame_ms = MilliSecs() + FRAME_SPEED_MS
					If o\frame > vobj\stand_end Then
						o\frame = vobj\stand_start
					EndIf
					
				EndIf
				
				
				
				If o\life# < 100.0 Then
				
					If Rand(1,2) = 2 Then 
						o\action = GRINING
						o\frame = vobj\grin_start
					Else 
						o\action = WALKING
						o\frame = vobj\walk_start
					EndIf
					
				EndIf
				
			Case WALKING
			
				MoveEntity o\entity,0,0,-0.25
			
				EntityTexture o\entity,texture,o\frame
				
				If MilliSecs() > o\frame_ms Then
					o\frame = o\frame + 1
					o\frame_ms = MilliSecs() + FRAME_SPEED_MS
					If o\frame > vobj\walk_end Then
						o\frame = vobj\walk_start
					EndIf
					
				EndIf
				
				
			Case GRINING
				EntityTexture o\entity,texture,o\frame
				
				If MilliSecs() > o\frame_ms Then
					o\frame = o\frame + 1
					o\frame_ms = MilliSecs() + FRAME_SPEED_MS
					If o\frame > vobj\grin_end Then
						o\frame = vobj\grin_start


						If o\life# < 50.0 Then
							o\action = WALKING
							o\frame = vobj\walk_start
						EndIf


					EndIf
					
				EndIf


				

				
			Case EXPLODING
				EntityTexture o\entity,texture,o\frame
				
				Local deleteme = False 
				
				If MilliSecs() > o\frame_ms Then
					o\frame = o\frame + 1
					o\frame_ms = MilliSecs() + FRAME_SPEED_MS
					If o\frame > vobj\die_end Then
						o\frame = vobj\die_end
						deleteme = True
					EndIf
					
				EndIf

				If deleteme Then
					FreeEntity o\entity
					o\entity = 0
					Delete( o )
				EndIf
				


			Default
		End Select
		
	Next
	
End Function


Function updateMarks()
	Local o.mark
	
	For o = Each mark
		If MilliSecs() > o\ms Then
			FreeEntity o\entity
			o\entity  = 0
			Delete(o)
			
		EndIf
	Next
	
End Function

Function resetCamera()
	PositionEntity camPivot,0,5,0
	RotateEntity camPivot,0,0,0
	RotateEntity cam,0,0,0
	EntityRadius camPivot,1.0
	EntityType camPivot,ENTITY_TYPE_CAMERA
	
End Function

Function drawWeaponCursor()


	Local size = 10
	
	Local cx = MouseX()
	Local cy = MouseY()


	DrawImage cursorSprite,cx,cy

;	Color 255,255,255
;	Line cx-size,cy,cx+size,cy
;	Line cx,cy-size,cx,cy+size
	

End Function

Function drawWeaponGraphic()

	If WEAPON\on Then
	
	
		If WEAPON\flash Then
			WEAPON\flash = False
			Color 255,255,255
			Rect 0,0,WINDOW_WIDTH,WINDOW_HEIGHT
		EndIf
		
		Local cx = WEAPON\wx
		Local cy = WEAPON\wy
		
		For i=1 To 25
			Color 10 * i, 10 * i, 10 * i
			Line i,WINDOW_HEIGHT,cx,cy
			Line WINDOW_WIDTH-i,WINDOW_HEIGHT,cx,cy
			
		Next
		
	
		If MilliSecs() > WEAPON\onfor Then
			WEAPON\on = False 
		EndIf
		
	EndIf
	
End Function


;
; This handles the mouse movements - controls weapon cursor
;
Function userMouseInput()

	If MouseHit( 1 ) Then
		; shoot weapon
		
		shootSelectedWeapon( MouseX(), MouseY() )
	EndIf
	
End Function

Function shootSelectedWeapon( px, py )
	Local x,y,z
	

	;
	; make weapon sound
	;
	
	If WEAPON\soundFX Then PlaySound WEAPON\soundFX
	
	;
	; show weapon graphics
	;
	WEAPON\on = True
	WEAPON\onfor = MilliSecs() + 150
	WEAPON\flash = True
	WEAPON\wx = MouseX()
	WEAPON\wy = MouseY()
	
	
	;
	; see if we hit any creeps or walls
	;
	Local entity = CameraPick( cam, WEAPON\wx, WEAPON\wy)
	If entity <> 0 Then
	
		DebugLog "Camera Pick Entity: " + entity
	
		Local hitvirus.virus = isVirus( entity )
		
		If entity = pl Then
		ElseIf hitvirus <> Null
			DebugLog "You shot a virus! life=" + hitvirus\life#
			
			hitvirus\life# = hitvirus\life# - 25
			
			If hitvirus\life# <= 0.0 Then
				vobj.vlist = findVlist( hitvirus\name$ )
				hitvirus\life# = 0.0
				; free and delete this after animation of dieing sequence finishes
				hitvirus\action = EXPLODING
				hitvirus\frame = vobj\die_start
				hitvirus\frame_ms = MilliSecs() + FRAME_SPEED_MS
			EndIf
			
		Else
		
			; must be a wall then
			
			x = PickedX()
			y = PickedY()
			z = PickedZ()
			
			nx = PickedNX()
			ny = PickedNY()
			nz = PickedNZ()
			
			mark.mark = createMark()
			PositionEntity mark\entity, x, y, z
			AlignToVector mark\entity, -nx, -ny, -nz, 3, 1
			MoveEntity mark\entity, 0, 0, -0.01
			
			
		EndIf
		
	EndIf
	
End Function

Function isVirus.virus( entity )
	Local v.virus
	For v = Each virus
		If v\entity = entity Then
			Return v
		EndIf
	Next
	
	Return Null
	
End Function

Function findVlist.vlist( name$)
	Local vlist.vlist
	For vlist = Each vlist
		If vlist\name$ = name$ Then
			Return vlist
		EndIf
	Next

	Return Null
	
End Function

Function getTexture( name$ )
	Local vlist.vlist = findVlist( name$ )

	If vlist = Null Then 
		Return 0
	EndIf
	
	
	Return vlist\texture
	
End Function

Function makeVirus( name$, px, pz )
	Local virus.virus
	Local vlist.vlist 
	Local texture=0
	Local ypos = 2
	
	If name$ = "George" Then
		ypos = 3
	EndIf
	
	texture = getTexture( name$ )
		

	virus = New virus
	virus\name$ = name$
	virus\action = STANDING
	virus\entity = CreateSprite()
	virus\life# = 100.0
	
	SpriteViewMode virus\entity, 1 ; 1=always face camera
	ScaleSprite virus\entity,3,3
	EntityAlpha virus\entity,1.0
	PositionEntity virus\entity,px,ypos,pz
	EntityType virus\entity,ENTITY_TYPE_VIRUS
	EntityPickMode  virus\entity,1
	EntityBox virus\entity,px,2,pz,2,4,2
	EntityRadius virus\entity,2

	EntityTexture virus\entity, texture, 0


End Function

Function addVlist( name$, jpg$, stand_start, stand_end, 	walk_start, walk_end, 	die_start, die_end, 	grin_start, grin_end )

	Local vlist.vlist = New vlist
	vlist\name$ = name$
	vlist\stand_start=stand_start
	vlist\stand_end=stand_end
	vlist\walk_start=walk_start
	vlist\walk_end=walk_end
	vlist\die_start=die_start
	vlist\die_end=die_end
	vlist\grin_start= grin_start
	vlist\grin_end  = grin_end
	vlist\texture = LoadAnimTexture( jpg$ , 4, 256, 256, 0, 16 )


End Function

Function createMark.mark()

	Local mark.mark = New mark
	mark\entity = CopyEntity( markSprite ) ; CreateCube()
	mark\ms = MilliSecs() + 60000  ; we clean up marks that are on objects after so much elapsed time
	EntityAlpha mark\entity,0.5
	ScaleSprite mark\entity,0.5,0.5
	
	Return mark
	
End Function


;
; This handles all keyboard activity
;
Function userKeyboardInput()

	Local speed# = 0.2
	Local aspeed# = 2.0
	
	;
	; slide camera left and right
	;
	If KeyDown( KEY_LT) Then
		MoveEntity camPivot,-speed#,0,0
	EndIf
	If KeyDown( KEY_GT ) Then
		MoveEntity camPivot,speed#,0,0
	EndIf
	
	;
	; move camera forward and backwards
	;
	If KeyDown( KEY_UP ) Then
		MoveEntity camPivot,0,0,speed#
	EndIf
	If KeyDown( KEY_DOWN) Then
		MoveEntity camPivot,0,0,-speed#
	EndIf
	
	;
	; rotate camera
	;
	If KeyDown( KEY_LEFT ) Then
		TurnEntity camPivot,0,aspeed#,0
	EndIf
	If KeyDown( KEY_RIGHT) Then
		TurnEntity camPivot,0,-aspeed#,0
	EndIf
	
	
	;
	; Raise camera up Or down
	;
	If KeyDown( KEY_YDOWN ) Then
		MoveEntity camPivot,0,speed#,0
	EndIf
	
	If KeyDown( KEY_YUP) Then
		MoveEntity camPivot,0,-speed#,0
	EndIf
	
	;
	; tilt camera
	;
	If KeyDown( KEY_NUMPAD_UP ) Then
		TurnEntity cam,-aspeed#,0,0
	EndIf
	
	If KeyDown( KEY_NUMPAD_DOWN) Then
		TurnEntity cam,aspeed#,0,0
	EndIf
	
	;
	; fix tilt to point straight ahead
	;
	If KeyDown( KEY_NUMPAD_CENTER ) Then
		Local yaw# = EntityPitch#(cam)
		TurnEntity cam,-yaw#,0,0
	EndIf
	
	
	
End Function