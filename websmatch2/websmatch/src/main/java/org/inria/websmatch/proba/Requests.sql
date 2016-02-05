--Créer une vue des matchs, pour lesquels on dispose de la valeur experte

CREATE VIEW view_validated_results AS
(SELECT m.* 
FROM match_results m, aligned_schemas a
WHERE ((m.id_schema1 = a.id_schema1 AND m.id_schema2 = a.id_schema2)
OR (m.id_schema1 = a.id_schema2 AND m.id_schema2 = a.id_schema1));

--On calcule le nb de ces matchs 
$total = SELECT COUNT(*) FROM validated_results;  

--On calcules les "séparateurs" des quantiles
foreach mesure
	for int i : 1 : 10
		quantile[$mesure][$i] =
  			SELECT $mesure FROM validated_results ORDER BY $mesure LIMIT ($total/10 x $i),1;

--On calcule les stats pour les quantiles:

Interval {
	double min,
	double max,
	double getMid() {
		return (min+max)/2;
	}
	
	
}

Interval inter0 = new Interval(0,0);
Interval inter1 = new Interval(1,1);
Interval interMid = new Interval(0.0001, 0.9999) //on récupère en SQL la première valeur strictement supérieure à 0 et celle juste en dessous de 1. 
                                                 

Map<SortedSet<Interval>, Integer>  nbPossiblesMatches
Map<SortedSet<Interval>, Integer>  nbTrueMatches

SortedSet<Interval> computeIntervals(SortedSet<Interval> splited, SortedSet<Interval> toSplit,epsilon) {
	if (toSplit.size() = 0) {
		return splited;
	else {
	inter = toSplit.first(); //and remove
	
	inter1 = new interval(inter.min,inter.getMid());
	inter2 = new interval(inter.getMid(),inter.max);
	//SQL
	nb1 = count(*) BETWEEN inter1 
	nb2 = count(*) BETWEEN inter2
	nbTrue1 = count(*) BETWEEN inter1 WHERE expert = 1 
	nbTrue2 = count(*) BETWEEN inter2 WHERE expert = 1
	
	if (Math.abs(nbTrue1/nb1 - nbTrue2/nb2) <epsilon) {
		splited.add(inter);
	} else {
		toSpit.add(inter1);
		toSpit.add(inter2);
	}
}



foreach mesure
	for int i : 1 : 10
		proba[$mesure][$i] = 
			SELECT COUNT(*) FROM view_validated_results WHERE $mesure BETWEEN quantile[$mesure][$i-1] AND quantile[$mesure][$i] AND expert = 1
		  / ($total / 10);

--
Maintenant on obtient un nouveau matcher:
for each (elt1,elt2)
  foreach mesure
     $sco = SELECT $measure FROM WHERE (elt1,elt2) 
     $quantile = //le quantile 
     $proba =  proba[$mesure][$quantile]
     
     On calcule le max et le produit et on les insère comme 2 nouvelles mesures
     
	