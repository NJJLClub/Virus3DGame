; Make Map (for virus game mostly)
; James Laderoute (c)
; March 22nd, 2011
; wheezingjim@comcast.net
;
;Graphics3D 640,480
;

Include "wallobj.bb"
Include "keys.bb"
Include "mathextra.bb"
Include "button.bb"



Global gfilename$ = "design.txt"

Global HEIGHT# = 8

Const SCREEN_WIDTH=1280,SCREEN_HEIGHT=960
Global WINDOW_WIDTH=SCREEN_WIDTH
Global WINDOW_HEIGHT=SCREEN_HEIGHT
Global WINDOW_DX=0
Global WINDOW_DY=0

Graphics3D SCREEN_WIDTH,SCREEN_HEIGHT


SetBuffer BackBuffer()
lit=CreateLight()

Global cam=CreateCamera()
CameraRange cam,.1,1000
PositionEntity cam,0,50,0
RotateEntity cam,90,0,0    ; pitch,yaw,roll,[global]


Local texture1 = LoadTexture( "media/brick.bmp" )
ScaleTexture texture1,5,5

Global block = CreateWall(0,0,0, 1,1,1)
EntityAlpha block,0
EntityColor block,0,255,0
EntityPickMode block,0
Local cursor.wallobj = findWall( block )
cursor\do_not_save = True




planeTex = CreateTexture(50,50,1)
SetBuffer TextureBuffer( planeTex)
ClsColor  0,0,200
Cls 
Color 255,255,255
Rect 0,0,50,50
SetBuffer BackBuffer()

;ScaleTexture planeTex,100,100


Global pl=CreatePlane(1)
PositionEntity pl,0,0,0
RotateEntity pl,0,0,0
EntityAlpha pl,1
EntityPickMode pl,2
EntityTexture pl,planeTex


Local sphere = CreateSphere()


Local pivotCenter = CreatePivot()
PositionEntity pivotCenter,0,0,0

Local viewPerspective = False
PointEntity cam,pivotCenter


Local mz# = 0.01
Local x#=0
Local y#=50
Local z#=0
Local r#=10.0  ; radius from center
Local a#=0     ; angle around center

Global showCursor = False
Global lastEntity = 0
Global selectedEntity = 0

Local gpx#=0
Local gpz#=0

Const mNone=0,mStartLine=1,mFirstLinePoint=2, mSecondLinePoint=3, mMove=4
Const mDoMove=5, mHelp=6, mStretch=7, mDoStretch=8, mQUIT=99
Const mDelete=9


Global mode=mNone
Global lastMode=mNone

Local note$ = ""
Local notems=0

Local ddx# = 0
Local ddz# = 0

buttonbox(0,0,4,1, "SAVE")
buttonbox(0,FontHeight()*2,4,1, "QUIT")

ReadWallDesign( gfilename$ )


While Not (mode=mQUIT) 


	tim = CreateTimer( 60 )  ; try to keep refresh rate to 60 fps


	If KeyDown(KEY_UP) Then
		r# = r# - 0.1
	ElseIf KeyDown(KEY_DOWN)
		r# = r# + 0.1
	ElseIf KeyDown(KEY_LEFT)
		a# = a# - 1
	ElseIf KeyDown(KEY_RIGHT)
		a# = a# + 1
	ElseIf KeyDown(KEY_YDOWN)
		y# = y# + 0.1
	ElseIf KeyDown(KEY_YUP)
		y# = y# - 0.1
	EndIf


	If mode = mNone Then
		
		If KeyDown(KEY_NUMPAD_UP) Then
		ElseIf KeyDown(KEY_NUMPAD_DOWN) Then
			MoveEntity cam,0,0,-1.0
		ElseIf KeyDown(KEY_NUMPAD_LEFT) Then
			MoveEntity cam,-1,0,0
		ElseIf KeyDown(KEY_NUMPAD_RIGHT) Then
			MoveEntity cam,1,0,0
		EndIf
		
	EndIf
	
	
	If KeyDown(KEY_Q) Then
		mode = mQUIT
	EndIf
	
	
	If mode<>mHelp  Then
	    If KeyHit(KEY_HELP) Then
			mode = mHelp
			FlushKeys 
		EndIf
	EndIf
	

	If KeyHit(KEY_ESCAPE) Then
		mode = mNone
		cancelMode(mode, lastMode)
		If lastMode = mNone And ( MilliSecs() > notems) Then
			note$ = "To Quit MakeMap you must press - Q"
			notems = MilliSecs() + 3000
		EndIf
		
	EndIf
		
	If KeyHit( KEY_S) Then
		mode = mStretch
	EndIf
	
	
	If KeyHit(KEY_W) Then
		mode = mStartLine
		cancelMode(mode, lastMode)
	EndIf
	
	If KeyHit(KEY_M) Then
		mode = mMove
		cancelMode( mode, lastMode)
	EndIf

	If KeyHit(KEY_DELETE) Then
		mode = mDelete
		cancelMode( mode, lastMode )
	EndIf

	If KeyHit(KEY_D) Then
		mode = mDelete
		cancelMode( mode, lastMode )
	EndIf
	

	Local clicked = False
	If MouseHit(1) Then
		clicked = True
	EndIf
	


	Select mode 
	
		Case mNone
		
			If clicked Then
				clickedName$ = getButtonClicked$( MouseX(), MouseY() )
				
				; Stop - to break into the debugger
				
				Select clickedName$
					Case "SAVE"
						
						SaveWallDesign( gfilename$ )
						
					Case "QUIT"
						mode = mQUIT
						
					Default
						CameraPick cam, MouseX(), MouseY()
						note$ = "CLICKED @ " + PickedX() + " , " + PickedZ()
						notems = MilliSecs() + 4000
												
				End Select
				
			EndIf
		
		Case mHelp
			If KeyHit(KEY_HELP) Then
				mode = mNone
			EndIf
			
		
		Case mStartLine
			EntityAlpha block,0.5
			showCursor = True
			mode = mFirstLinePoint
			
		Case mFirstLinePoint
			If clicked Then
				CameraPick cam,MouseX(),MouseY()
				
				px# = PickedX()
				pz# = PickedZ()
				
				gpx# = round( px# )
				gpz# = round( pz# )
			
				mode = mSecondLinePoint

			EndIf
			
		Case mSecondLinePoint
			If clicked Then
			
				Local grid# = 1.0
				
				CameraPick cam,MouseX(),MouseY()
				
				px# = PickedX()
				pz# = PickedZ()
				
				px# = round( px# )
				pz# = round( pz# )
			
				mode = mNone
				
				If ( Abs(px# - gpx# ) > Abs( pz# - gpz# ) ) Then
					 Local dx# = px#-gpx#
					 If dx# < 0 Then 
						dx# = Abs( dx# ) + grid#
						gpx# = px#
						gpz# = pz#
					 Else
						dx# = dx# + grid#
					 EndIf
					
					 CreateWall(gpx#,0,gpz#, dx#, 1, HEIGHT#)
		
				Else
					Local dz# = pz#-gpz#
					If dz# < 0 Then
						dz# = Abs(dz#) + grid#
						gpx# = px#
						gpz# = pz#
					Else
						dz# = dz# + grid#
					EndIf
					
					CreateWall(gpx#,0,gpz#, 1, dz#, HEIGHT# )
				EndIf
				
				EntityAlpha block,0.0
				
		        showCursor = False
			EndIf
			
		Case mMove
			
			HighlightHover()
						
			If clicked Then
				selectedEntity = lastEntity
				If selectedEntity <> 0 Then
					mode = mDoMove
				Else
					mode = mNone
					If lastEntity <> 0 Then
						EntityAlpha lastEntity,1.0
					EndIf 
				EndIf
			EndIf
			
		Case mStretch
		
			HighlightHover()
			
			If clicked Then
				selectedEntity = lastEntity
				If selectedEntity <> 0 Then
					mode = mDoStretch
				Else
					mode = mNone
					If lastEntity <> 0 Then
						EntityAlpha lastEntity,1.0
					EndIf 
				EndIf
			EndIf

		Case mDelete


			HighlightHover()
			
			
			If clicked Then
				selectedEntity = lastEntity
				If selectedEntity <> 0 Then
					DeleteWall( selectedEntity )
					lastEntity = 0
					selectedEntity = 0
				Else
					mode = mNone
					If lastEntity <> 0 Then
						EntityAlpha lastEntity,1.0
					EndIf 
				EndIf
			EndIf

			If KeyHit( KEY_ENTER )  Then
				UnhighlightEntity( selectedEntity )
				mode = mNone
			EndIf
			
			If KeyHit( KEY_NUMPAD_ENTER) Then
				EntityAlpha selectedEntity,1.0
				mode = mNone
			EndIf
			
			


		Case mDoStretch
			; use arrow keys now to grow/shrink object around
			; press ENTER to leave move mode
			
			newobj.wallobj = Null
			
			If KeyHit( KEY_NUMPAD_PLUS ) Then
				newobj.wallobj = StretchWall( selectedEntity, 0, 0, 1)
			EndIf
			If KeyHit( KEY_NUMPAD_MINUS) Then
				newobj.wallobj = StretchWall( selectedEntity, 0, 0, -1)
			EndIf
			If KeyHit( KEY_NUMPAD_LEFT) Then
				newobj.wallobj = StretchWall( selectedEntity, 0, -1, 0)
			EndIf
			If KeyHit( KEY_NUMPAD_RIGHT) Then
				newobj.wallobj = StretchWall( selectedEntity, 0, 1, 0)
			EndIf
			If KeyHit( KEY_NUMPAD_UP) Then
				newobj.wallobj = StretchWall( selectedEntity, 1, 0, 0)
			EndIf
			If KeyHit( KEY_NUMPAD_DOWN) Then
				newobj.wallobj = StretchWall( selectedEntity, -1, 0, 0)
			EndIf
			
			


			If ( newobj <> Null ) Then
				If ( selectedEntity = lastEntity ) Then
					lastEntity = 0
				EndIf
				
				selectedEntity = newobj\entity
				HighlightEntity(0.3)
			EndIf
			
			If KeyHit( KEY_ENTER )  Then
				EntityAlpha selectedEntity,1.0
				mode = mNone
			EndIf
			
			If KeyHit( KEY_NUMPAD_ENTER) Then
				EntityAlpha selectedEntity,1.0
				mode = mNone
			EndIf
			
			If KeyHit( KEY_NUMPAD_CENTER ) Then
				EntityAlpha selectedEntity,1.0
				mode = mStretch
				selectedEntity = 0
				lastEntity = 0
			EndIf
		
			
		Case mDoMove
			; use arrow keys now to move object around
			; press ENTER to leave move mode
			
			If KeyHit( KEY_NUMPAD_UP ) Then
				MoveWall( selectedEntity, 0,0,1)
			EndIf
			If KeyHit( KEY_NUMPAD_DOWN) Then
				MoveWall( selectedEntity, 0,0,-1)
			EndIf
			If KeyHit( KEY_NUMPAD_LEFT) Then
				MoveWall( selectedEntity, -1,0,0)
			EndIf
			If KeyHit( KEY_NUMPAD_RIGHT) Then
				MoveWall( selectedEntity, 1,0,0)
			EndIf 

			If KeyHit( KEY_NUMPAD_PLUS ) Then
				MoveWall( selectedEntity, 0, 1, 0)
			EndIf
			If KeyHit( KEY_NUMPAD_MINUS) Then
				MoveWall( selectedEntity, 0, -1, 0)
			EndIf
			
			
			If KeyHit( KEY_ENTER )  Then
				EntityAlpha selectedEntity,1.0
				mode = mNone
			EndIf
			
			If KeyHit( KEY_NUMPAD_ENTER) Then
				EntityAlpha selectedEntity,1.0
				mode = mNone
			EndIf
			
			If KeyHit( KEY_NUMPAD_CENTER ) Then
				EntityAlpha selectedEntity,1.0
				mode = mMove
				selectedEntity = 0
				lastEntity = 0
			EndIf
			
			
			
			
			
	End Select


	
	If showCursor Then
		; we need to map MOUSEX,MOUSEY to world x,z against the plane
		CameraPick cam,MouseX(),MouseY()
		
		px# = PickedX()
		pz# = PickedZ()
		px# = round( px# )
		pz# = round( pz# )

		PositionEntity block,px#,0,pz#
	EndIf
	

	lastMode = mode
		
	

	x# = Cos(a#) * r# + ddx#
	z# = Sin(a#) * r# + ddz#

	If KeyHit(KEY_V) Then
		viewPerspective = Not viewPerspective
		
		If viewPerspective Then

			r# = 20
			a# = 45
			y# = 50
			

		Else
			;RotateEntity cam,90,0,0
			r# = 2
			a# = 90
			y# = 50

		EndIf
		
	EndIf

	PositionEntity cam,x#,y#,z#
	PositionEntity sphere,x#,0,z#
	PointEntity cam,pivotCenter		
	
	UpdateWorld
	RenderWorld
	
	If note$ <> "" Then
		If MilliSecs() > notems Then
			note$ = ""
		Else
			textbox(SCREEN_WIDTH/2-FontWidth()*Len(note$)/2,SCREEN_HEIGHT/2,2,1,note$)
		EndIf
	EndIf
	
	If mode = mHelp Then

		help$ = "W - New Wall ; M - Move ; S - Stretch ; D - Delete ; V - Toggle Views ; Q - Quit"
		Text SCREEN_WIDTH/2, FontHeight() ,help$,True,True
		
		help$ = "Z - Zoom out ; A - Zoom In ; LeftArrow - Rotate Left ; RightArrow - Rotate Right  ; UpArrow - Move In ; DownArrow - Move Out"
		Text SCREEN_WIDTH/2, FontHeight()*2, help$, True, True
		
	EndIf
	
	mode_text$ = getMode$( mode )
	If ( mode_text$ <> "" ) Then
		Text SCREEN_WIDTH/2,FontHeight(), mode_text$, True
	EndIf
	
	drawbuttonboxes()
	drawScreenCursor()
	
;	textbox(0,0,4,1, "SAVE")
	
	Flip

	WaitTimer( tim )		; for controlling refresh rate
	FreeTimer( tim)


Wend

FreeEntity pl
FreeEntity cam
EndGraphics
End

Function drawScreenCursor()



	Local cx = WINDOW_WIDTH / 2 + WINDOW_DX
	Local cy = WINDOW_HEIGHT / 2 + WINDOW_DY
	Local size = 10
	
	cx = MouseX()
	cy = MouseY()

	Color 255,255,255
	Line cx-size,cy,cx+size,cy
	Line cx,cy-size,cx,cy+size
	

End Function


Function getMode$( thisMode=0 )
	txt$ = ""

	Select thisMode
	
		Case mNone
		
		Case mStartLine
			txt$ = "Wall"

			
		Case mFirstLinePoint
			txt$ = "Wall 1st Point"

		Case mSecondLinePoint
			txt$ = "Wall 2nd Point"

		Case mStretch
			txt$ = "Stretch - Click on a Wall"

		Case mDoStretch
			txt$ = "Stretch - Use KEYPAD arrows"

		Case mMove
			txt$ = "Move - Click on a Wall"

		Case mDoMove	
			txt$ = "Move - Use KEYPAD arrows to move ; ENTER KEY to place it ; KEYPAD-5 to place it & Move again"

		Case mDelete
			txt$ = "Delete - Click on a Wall to delete it"
			
		Default
		
	End Select
	
	Return( txt$)
	
End Function


Function CancelMode( newMode, prevMode )


	Select newMode
		Case mNone
			If lastEntity <> 0 Then
				EntityAlpha lastEntity,1.0
			EndIf 

			EntityAlpha block,0.0
			showCursor = False


		
		Case mStartLine
			If lastEntity <> 0 Then
				EntityAlpha lastEntity,1.0
			EndIf 
					
			
		Case mFirstLinePoint
			
		Case mSecondLinePoint
			
		Case mMove
			EntityAlpha block,0.0
			showCursor = False

			
		Case mDoMove
					
	End Select
	

End Function

Function HighlightEntity( ee )
    If ( ee <> 0 ) Then 
    	EntityAlpha ee,0.3
	EndIf
	
End Function

Function UnhighlightEntity( ee )
	If (ee <> 0 ) Then
		EntityAlpha ee,1.0
	EndIf
End Function



Function HighlightHover()

	Local ee = CameraPick( cam,MouseX(),MouseY() )
	
	If ( ee <> pl ) And (ee <> 0) Then
		HighlightEntity( ee )
		If lastEntity <> ee Then
			If lastEntity <> 0 Then EntityAlpha lastEntity,1.0
		EndIf
		
		lastEntity = ee
	ElseIf ( lastEntity <> 0 )
		EntityAlpha lastEntity,1.0
	EndIf

End Function