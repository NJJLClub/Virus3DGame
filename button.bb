;
; TBD:
;		visible field so we can create buttons once and hide them, thus making them inactive until we need them.
;		I'm thinking a PROMPT that asks for YES NO
;


Type button
	Field x,y
	Field b,m
	Field txt$
End Type

Function drawbuttonboxes()
	For mine.button = Each button
		textbox( mine\x, mine\y, mine\m, mine\b, mine\txt$ )
	Next
End Function


Function buttonbox(x,y,m,b,txt$)
	Local btn.button = New button
	
	btn\x = x
	btn\y = y
	btn\m = m
	btn\b = b
	btn\txt$ = txt$
	
End Function


; x,y  position of upper left corner of button box
; m = margin between border and text
; b = border thickness
;
; BBBBBBBBBBBB
; BBBBBBBBBBBB
; BBBBmmmmmmmm
; BBBBm
; BBBBm TEXT
; BBBBmmmmmmmm
; BBBBBBBBBBBB
; BBBBBBBBBBBB

Function textbox(x,y,m,b,txt$)

	Local w = FontWidth() * Len(txt$)
	Local h = FontHeight()
	
	Color 255,255,255
	; draw border (white)
	; WIDTH should include TEXTWIDTH + MARGIN*2 + B*2
	Rect x,y,w+b*2+m*2,h+b*2+m*2,True

	; draw inside border
	; BLACK on the insdie
	; WIDTH should include WIDTH of text + m*2 + border
	;
	Color 0,0,0
	Rect x+b,y+b,w+m*2 ,h+m*2, True
	
	Color 255,255,255
	Text x+b+m,y+b+m,txt$


End Function


Function getButtonClicked$(mx,my)

	For mine.button = Each button
		Local w =  FontWidth()*Len(mine\txt$) + mine\b*2 + mine\m*2
		Local h = FontHeight() + mine\b*2 + mine\m*2
		
		If ( ( mx > mine\x ) And ( mx < (mine\x + w) ) ) Then
			If ( my > mine\y ) And ( my < ( mine\y + h )) Then
				Return mine\txt$
			EndIf
		EndIf
		
	Next
	
	Return("")
	

End Function
