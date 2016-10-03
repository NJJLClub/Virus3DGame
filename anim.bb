; Animated Sprite Test
; James Laderoute (c)
; wheezingjim@comcast.net
; April 2 2011
;

Include "keys.bb"


Const GRAPHICS_WIDTH=640, GRAPHICS_HEIGHT=480
Global WINDOW_WIDTH=GRAPHICS_WIDTH, WINDOW_HEIGHT=GRAPHICS_HEIGHT
Global WINDOW_DX=0, WINDOW_DY=0

AppTitle "Animated Sprite Test", "Are you sure you want to quit?" 

;
; Put the game into 3D Graphics Mode now
; =================================================================================
;
Graphics3D GRAPHICS_WIDTH, GRAPHICS_HEIGHT

    SetBuffer BackBuffer()

	ClsColor 255,0,0

	Global fnum=2
	
		

    t = LoadAnimTexture("media\virus"+fnum+".jpg", 4, 256, 256, 0, 16)
    frame=0
    sprite = CreateSprite()
    EntityTexture sprite,t,frame
    PositionEntity sprite,0,0,50
    ScaleSprite sprite,20,20

	cube = CreateCube()
	EntityColor cube,255,0,0
	PositionEntity cube,0,0,55
	ScaleEntity cube,40,40,1

;
; Setup the camera
;
    Global camPivot = CreatePivot()
    Global cam=CreateCamera( camPivot )
    CameraRange cam,.1,1000
    resetCamera()

;
; Here is the main game loop. This updates and draws
; the game screens.
;

rate = 500
framems = MilliSecs() +  rate  ; update frame every 1/2 second

While Not KeyDown(KEY_ESCAPE)
	
	Cls
	
	If ( MilliSecs() > framems ) Then
	    EntityTexture sprite,t,frame
		frame = (frame + 1 ) Mod 16 
		framems = MilliSecs() + rate
	EndIf
	
	
	userKeyboardInput()
	
	UpdateWorld
	RenderWorld
	
	
	Flip
	
Wend

FreeEntity cam
FreeEntity camPivot
FreeEntity sprite
EndGraphics
End



Function resetCamera()
	PositionEntity camPivot,0,5,0
	RotateEntity camPivot,0,0,0
	RotateEntity cam,0,0,0
	EntityRadius camPivot,1.0
	EntityType camPivot,ENTITY_TYPE_CAMERA
	
End Function

;
; This handles all keyboard activity
;
Function userKeyboardInput()

	Local speed# = 0.1
	Local aspeed# = 1.0
	
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