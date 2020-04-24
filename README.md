# IWiUM: Robocode

Projekt z Robocode na Inżynierię Wiedzy i Uczenie Maszynowe.

## Aktywowanie okienek JavaDoc w IntelliJ

`File` -> `Project Structure...` -> pod `Project Settings`: `Modules` -> w drugiej kolumnie pod `robocodeEnv` zaznaczyć `main` -> w trzeciej kolumnie zakładka `Dependencies` -> prawy przycisk myszy na `robocode.jar` -> `Edit...` -> ikona + z planetą -> wpisać https://robocode.sourceforge.io/docs/robocode/ -> dużo razy zatwierdzić ;)

Od tej pory `Ctrl`+`Q`, gdy karetka jest na wywołaniu API *Robocode'a*, powinno pokazywać dokumentację.

## Budowanie robotów / import do Robocode

1. Uruchom task gradle'owy `build`. 
2. W *Robocode* przejdź do `Options` -> `Preferences` -> `Development Options` i wybierz `Add`. Wskaż ścieżkę `{to repozytorium}/build/classes/java/main` i zamknij okno ustawień.
3. W *Robocode* wybierz `Battle` -> `New` i odświerz listę botów (`Ctrl`+`R`). Możesz już wystawić do walki nowe boty.

Przy kolejnych zmianach lub nowych botach wystarczy powtórzyć kroki 1. i 3. 

## Istotne strony internetowe

* [Strona główna](https://robocode.sourceforge.io/) (linki poniżej to wybór najciekawszych podstron)
* [Pobieranie Robocode'a w wersji 1.9.3.9](https://sourceforge.net/projects/robocode/files/robocode/1.9.3.9/robocode-1.9.3.9-setup.jar/download)
* [My First Robot](https://robowiki.net/wiki/Robocode/My_First_Robot)
* [FAQ Robocode'a](https://robowiki.net/wiki/Robocode/FAQ#Installing_and_using)
* [Wskazywanie Robocode'owi folderu ze skompilowanymi klasami robotów](https://robowiki.net/wiki/Robocode/Add_a_Robot_Project)