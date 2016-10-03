; mathextra
;
; This is round() function that takes a float and rounds up to nearest whole number
;
;


Function round%(f#)


	Local big = Int(f# * 100.0)
	Local big2 = Int(f#) * 100
	Local small = big - big2
	
	If ( Abs(small) <= 50 ) Then
		addone = 0
	Else
		addone = 1 * Sgn(f#)
	EndIf
	
	Return( Int(f#) + addone )	

;    Return( f + 0.5 * Sgn(f) )

End Function 