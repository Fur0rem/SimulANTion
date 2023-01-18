all:
	./make.sh

clean :
	./clean.sh

launch:
	cd ./classes/ && java TestSimulation 4000 1000