#! /bin/bash

cd ./classes/

for classe in $(ls *.class); do
	if !([ "$classe" = "Terrain.class" ] || [ "$classe" = "Ressource.class" ] || [ "$classe" = "Terrain.class" ]) ; then
		rm $classe
	fi
done

