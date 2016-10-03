
Const ENTITY_TYPE_WALL=2
Global wall_texture = 0

Type wallobj
	Field entity
	Field x,y,z
	Field w,l,h
	Field do_not_save
End Type


Function MoveWall( entity, dx#, dy#, dz# )

	MoveEntity entity,x,y,z
	
	Local found.wallobj = findWall( entity )
	
	If (found <> Null ) Then
		MoveEntity entity,dx#,dy#,dz#
		found\x = EntityX(entity)
		found\y = EntityY(entity)
		found\z = EntityZ(entity)
	EndIf
	

End Function

Function findWall.wallobj( entity )
	Local mine.wallobj = Null
	Local found.wallobj = Null
	
	For mine.wallobj = Each wallobj	
		If mine\entity = entity Then
			found = mine
		EndIf
	Next
	
	Return( found )
	
End Function

Function DeleteWall( entity )
	Local found.wallobj = findWall( entity)
	If ( found <> Null ) Then
		If found\entity <> 0 Then
			FreeEntity( found\entity )
			Delete( found )
		Else
			Delete( found )
		EndIf
	EndIf
	
End Function



Function StretchWall.wallobj( entity, W#, L#, H# )


	Local found.wallobj = findWall( entity )
	
	If ( found <> Null ) Then
		found\w = found\w + W#
		found\l = found\l + L#
		found\h = found\h + H#
		
		Local newmesh = CreateWall( found\x, found\y, found\z, found\w, found\l, found\h , True)

		FreeEntity( found\entity )

		found\entity = newmesh
		
		
	EndIf
	
	Return( found )
	

End Function

Function ReadWallDesign( filename$ )
    Local x,y,z,w,l,h


	Local filein = ReadFile( filename$ )
	While Not Eof(filein)
	
			x = ReadInt(filein)
			y = ReadInt(filein)
			z = ReadInt(filein)
			w = ReadInt(filein)
			l = ReadInt(filein)
			h = ReadInt(filein)
						
			CreateWall( x, y, z, w, l, h )

	Wend 
	CloseFile(filein)
	
End Function




Function SaveWallDesign( filename$ )
	
	Local fileout = WriteFile( filename$ )
	Local mine.wallobj = Null
	For mine.wallobj = Each wallobj	
	
		If Not mine\do_not_save Then
	
			WriteInt(fileout, mine\x )
			WriteInt(fileout, mine\y )
			WriteInt(fileout, mine\z )
			WriteInt(fileout, mine\w )
			WriteInt(fileout, mine\l )
			WriteInt(fileout, mine\h )
		
		EndIf
		
		
	Next	

	CloseFile( fileout )
		
End Function


Function CreateWall(atx#=0,aty#=0, atz#=0, W#=1,L#=1,H#=1, MESHONLY=False )


	DebugLog("CreateWall: " + atx# + "," + atz# + " w=" +W# + " h=" + H# + " l="+L#)
	brush=CreateBrush() ; create a brush
	BrushColor brush,150,150,150 ; a grey brush 
	BrushShininess brush,1
	If ( wall_texture <> 0 ) Then
		DebugLog "adding wall_texture to brush"
		BrushTexture brush, wall_texture
	EndIf
	
	
	mesh=CreateMesh() ; create a mesh
	
	If ( Not MESHONLY ) Then 
		wallobj.wallobj = New wallobj
		wallobj\entity = mesh
		wallobj\x = atx#
		wallobj\y = aty#
		wallobj\z = atz#
		wallobj\w = W#
		wallobj\l = L#
		wallobj\h = H#
	EndIf
	
	
	
	;
	; to make a cube requires 4 (top)  4(bottom) vertex's, then we 
	; just create triangles based on these
	;
	
	;
	; bottom of box
	;
	surf=CreateSurface( mesh, brush ) ; create a (painted) surface



				     ;  x, y, z, u, v, 
		AddVertex surf, 0, 1, 0, 0, 0 ; 0 - Now, we add 4 vertices...  (4 coordinates create a square X,Y,Z)
		AddVertex surf, 1, 1, 0, 1, 0 ; 1 - 
		AddVertex surf, 1, 0, 0, 1, 1 ; 2 - 
		AddVertex surf, 0, 0, 0, 0, 1 ; 3 - 
	
		AddVertex surf, 0, 1, 1, 1, 0 ; 4
		AddVertex surf, 1, 1, 1, 0, 0 ; 5
		AddVertex surf, 1, 0, 1, 0, 1 ; 6
		AddVertex surf, 0, 0, 1, 1, 1 ; 7


	

    ;
	; the direction in which you create your triangles will
	; affect which side of the triangle is shown to the user.
	; either the inside or outside.
	; here I'm making sure the outside is always shown so it
	; looks like a solid cube.
	;	
	AddTriangle surf,0,1,2
	AddTriangle surf,3,0,2
	
	AddTriangle surf,6,5,4
	AddTriangle surf,7,6,4
	
	
	AddTriangle surf,2,1,5
	AddTriangle surf,6,2,5
	
	AddTriangle surf,3,2,6
	AddTriangle surf,7,3,6
	
	AddTriangle surf,3,7,4 ; left side
	AddTriangle surf,0,3,4

	
	AddTriangle surf,5,1,0 ; back side
	AddTriangle surf,5,0,4
	

   ; VertexNormal surf,0,1,0,0   ; ,nx#,ny#,nz#

	
	UpdateNormals mesh 
	ScaleMesh mesh,W,H,L
	PositionEntity mesh,atx,aty,atz
	
	EntityPickMode  mesh,2
	
	EntityBox mesh,0,0,0,W,H,L
	EntityType mesh,ENTITY_TYPE_WALL
	
	Return mesh
	
End Function