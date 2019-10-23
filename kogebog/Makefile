FPRs-klimavenlige-kogebog.pdf: recipes.tex Radikal_Ungdoms_Klimavenlige_kogebog.tex
	pdflatex Radikal_Ungdoms_Klimavenlige_kogebog.tex
	pdftk kogebog-forside.pdf Radikal_Ungdoms_Klimavenlige_kogebog.pdf cat output FPRs-klimavenlige-kogebog.pdf

recipes.tex:
	cd .. && mvn package && cd - && java -jar ../target/cookbook-1.0-SNAPSHOT-jar-with-dependencies.jar ingredients.json recipes.json -r recipes.tex -d data.tex -p 5


clean:
	rm -f *.aux *.log recipes.tex data.tex Radikal_Ungdoms_Klimavenlige_kogebog.pdf FPRs-klimavenlige-kogebog.pdf *.out *.toc *.gz
