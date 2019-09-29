# Klimavenlig kogebog

Dette er en klimavenlig kogebog lavet af Radikal Ungdom.

# Bygning af programmets dele

Programmet består af to dele. Et program der tager rå data om ingredienser og opskrifter, og danner en tex fil. Derefter bygges selve kogebogen fra tex filen.

## Bygning af Java programmet

Brug Maven til at bygge programmet med:

```
mvn clean package
```

## Kørsel af Java programmet

Under Windows dannes en `.exe` fil i target folderen, der kan køres som et hvilket som helst andet windows program.

```
target/cookbook.exe
```

Under Linux og Mac, skal programmet køres via en Java VM.

```
java -jar target/cookbook-1.0-SNAPSHOT-jar-with-dependencies.jar
```

Hvis man kører programet uden argumenter, vil det udskrive en hjælpeside:

```
$ java -jar target/cookbook-1.0-SNAPSHOT-jar-with-dependencies.jar
You need to supply two arguments: The list of ingredients, and the list of recepies.
usage: cookbook [OPTIONS] ingredients.json recepies.json
Where options can be:
 -c,--config <arg>    configuration file [config.json]
 -d,--data <arg>      data output file [data.tex]
 -h,--help            print this message
 -p,--persons <arg>   how many persons to create for [4]
 -r,--recipe <arg>    recipe output file [recipes.tex]
```

## Dannelse af input til LaTeX

Den nemmeste måde at danne input filerne til LaTeX er at gå ind i `kogebog` kataloget og køre programmet derfra:

```
java -jar ../target/cookbook-1.0-SNAPSHOT-jar-with-dependencies.jar \
   ingredients.json recipes.json \
   --recipe recipes.tex --data data.tex --persons 5
```

Ovenstående laver input svarende til opskrifter til 5 personer.

For at oversætte tex filen til latex, skal man installere xcookybooky som style i sin tex installation,
samt support for Dansk.

Under ubuntu, kan det gøres ved at installere disse pakker:

```
sudo apt install texlive-publishers texlive-lang-european
```

Brug derefter f.eks. `texstudio` til at lave tex filen med, eller f.eks. `pdflatex`:

```
pdflatex Radikal_Ungdoms_Klimavenlige_kogebog.tex
```

Kogebogen er derefter klar til brug i filen `Radikal_Ungdoms_Klimavenlige_kogebog.pdf`

# Tilføjelse af opskrifter

Opskrifter tilføjes i filen `kogebog/recipes.json`. Formatet burde være til at forstå, men det vigtige er faktisk ingredienserne. Disse defineres i filen `kogebog/ingredients.json`. Hver ingrediens har et id/navn, en enhed og en CO2 belastningsværdi.

Når man skal oprette en ny ret, sikrer man sig først at alle ingredienser til registreret. Hvis man mangler nogle, tilføjes disse. Derefter kan der laves en opskrift.

Bemærk, at det er nemmere hvis ingredienserne tilføjes i alfabetisk rækkefølge. For Linux (eller Mac) brugere, ligger der et script i `kogebog/sort-ingredients.sh` der kan sortere indholdet i ingredienslisten.
