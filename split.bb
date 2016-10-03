Dim pLine$(80)
Global pLineCount

Dim pLine2$(80)
Global pLineCount2

Global p_max_array = 80

	
Function unparseLines$( start_here , concatChar$ )
	s$ = ""
	
	For i=start_here To pLineCount
		If ( i = start_here ) Then 
			s$ = pLine$(i)
		Else
			s$ = s$ + concatChar$ + pLine$(i)
		EndIf
		
	Next
	
	Return s$

End Function

Function parseLine( lineOfText$ , parseChar$ ) 

	ith = 0
	
	prev$ = ""
	For i = 0 To p_max_array-1
		pLine$(i) = ""
	Next
	

	For i=1 To Len(lineOfText$)
	
		If ( Mid( lineOfText$, i, 1 ) = parseChar$ ) Then
			If ( prev$ <> parseChar$ ) Then ith = ith + 1
			prev$ = parseChar$

		Else
			pLine$(ith) = pLine$(ith) + Mid(lineOfText$,i,1)
			prev$ = Mid(lineOfText$,i,1)

		EndIf
		
	Next

	pLineCount = ith
	

End Function


;
; this exists so you can split text within split text
;
Function parseLine2( lineOfText$ , parseChar$ ) 

	ith = 0
	
	prev$ = ""
	For i = 0 To p_max_array-1
		pLine2$(i) = ""
	Next
	

	For i=1 To Len(lineOfText$)
	
		If ( Mid( lineOfText$, i, 1 ) = parseChar$ ) Then
			If ( prev$ <> parseChar$ ) Then ith = ith + 1
			prev$ = parseChar$

		Else
			pLine2$(ith) = pLine2$(ith) + Mid(lineOfText$,i,1)
			prev$ = Mid(lineOfText$,i,1)

		EndIf
		
	Next

	pLineCount2 = ith
	

End Function