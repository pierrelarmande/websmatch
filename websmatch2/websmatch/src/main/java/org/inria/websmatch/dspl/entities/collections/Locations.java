package org.inria.websmatch.dspl.entities.collections;

import java.util.HashMap;

import org.inria.websmatch.dspl.entities.Entity;
import org.inria.websmatch.dspl.entities.geo.Location;
import org.inria.websmatch.utils.StringUtils;

public class Locations {

    private HashMap<String,Entity> locations;

    public Locations() {
	setLocations(new HashMap<String,Entity>());
	populate();
    }

    public void setLocations(HashMap<String,Entity> locations) {
	this.locations = locations;
    }

    public HashMap<String,Entity> getLocations() {
	return locations;
    }

    private void populate() {

	getLocations().put(StringUtils.cleanString("Austria"),new Location("Austria", "Austria", (float) 48.210032, (float) 16.356811));
	getLocations().put(StringUtils.cleanString("Belgium"),new Location("Belgium", "Belgium", (float) 50.851041, (float) 4.344177));
	getLocations().put(StringUtils.cleanString("Czech Rep."),new Location("Czech Rep.", "Czech Rep.", (float) 50.099441, (float) 14.412231));
	getLocations().put(StringUtils.cleanString("Denmark"),new Location("Denmark", "Denmark", (float) 55.391592, (float) 10.391235));
	getLocations().put(StringUtils.cleanString("Finland"),new Location("Finland", "Finland", (float) 60.152442, (float) 24.909667));
	getLocations().put(StringUtils.cleanString("France"),new Location("France", "France", (float) 48.835797, (float) 2.369384));
	getLocations().put(StringUtils.cleanString("Germany"),new Location("Germany", "Germany", (float) 52.522906, (float) 13.377685));
	getLocations().put(StringUtils.cleanString("Greece"),new Location("Greece", "Greece", (float) 37.961523, (float) 23.748779));
	getLocations().put(StringUtils.cleanString("Hungary"),new Location("Hungary", "Hungary", (float) 47.517201, (float) 19.026489));
	getLocations().put(StringUtils.cleanString("Iceland"),new Location("Iceland", "Iceland", (float) 64.623877, (float) -17.504885));
	getLocations().put(StringUtils.cleanString("Ireland"),new Location("Ireland", "Ireland", (float) 53.317749, (float) -6.253052));
	getLocations().put(StringUtils.cleanString("Italy"),new Location("Italy", "Italy", (float) 41.902277, (float) 12.498779));
	getLocations().put(StringUtils.cleanString("Luxembourg"),new Location("Luxembourg", "Luxembourg", (float) 49.616939, (float) 6.141129));
	getLocations().put(StringUtils.cleanString("Netherlands"),new Location("Netherlands", "Netherlands", (float) 52.382306, (float) 4.865112));
	getLocations().put(StringUtils.cleanString("Norway"),new Location("Norway", "Norway", (float) 59.844815, (float) 10.708006));
	getLocations().put(StringUtils.cleanString("Poland"),new Location("Poland", "Poland", (float) 51.75424, (float) 19.442138));
	getLocations().put(StringUtils.cleanString("Portugal"),new Location("Portugal", "Portugal", (float) 38.61687, (float) -9.14795));
	getLocations().put(StringUtils.cleanString("Slovak Rep."),new Location("Slovak Rep.", "Slovak Rep.", (float) 48.730832, (float) 19.159241));
	getLocations().put(StringUtils.cleanString("Spain"),new Location("Spain", "Spain", (float) 40.396764, (float) -3.739014));
	getLocations().put(StringUtils.cleanString("Sweden"),new Location("Sweden", "Sweden", (float) 57.686533, (float) 11.995239));
	getLocations().put(StringUtils.cleanString("Switzerland"),new Location("Switzerland", "Switzerland", (float) 47.361153, (float) 8.546448));
	getLocations().put(StringUtils.cleanString("Turkey"),new Location("Turkey", "Turkey", (float) 39.96028, (float) 32.735595));
	getLocations().put(StringUtils.cleanString("UK"),new Location("UK", "UK", (float) 51.508742, (float) -0.102541));
	/*getLocations().put(new Location("Alsace","Alsace",(float)48.365374,(float)7.440491));
	getLocations().put(new Location("Aquitaine","Aquitaine",(float)44.801327,(float)-0.274658));
	getLocations().put(new Location("Auvergne","Auvergne",(float)45.809658,(float)3.356323));
	getLocations().put(new Location("Basse normandie","Basse-Normandie",(float)48.994636,(float)-0.532837));
	getLocations().put(new Location("Bourgogne","Bourgogne",(float)47.129951,(float)4.416504));
	getLocations().put(new Location("Bretagne","Bretagne",(float)48.286848,(float)-2.91687));
	getLocations().put(new Location("Centre","Centre",(float)47.842658,(float)1.680908));
	getLocations().put(new Location("Champagne ardenne","Champagne-Ardenne",(float)48.879167,(float)4.487915));
	getLocations().put(new Location("Corse","Corse",(float)42.081917,(float)9.000549));
	getLocations().put(new Location("Dom","Dom",(float)16.314868,(float)-61.526184));
	getLocations().put(new Location("Franche comte","Franche-Comté",(float)47.191579,(float)6.031494));
	getLocations().put(new Location("Haute normandie","Haute-Normandie",(float)49.562634,(float)0.884399));
	getLocations().put(new Location("Ile de france","Ile-de-France",(float)48.893615,(float)2.647705));
	getLocations().put(new Location("Languedoc roussillon","Languedoc-Roussillon",(float)43.663898,(float)3.290405));
	getLocations().put(new Location("Limousin","Limousin",(float)45.94924,(float)1.573792));
	getLocations().put(new Location("Lorraine","Lorraine",(float)48.947759,(float)6.201782));
	getLocations().put(new Location("Midi pyrenees","Midi-Pyrénées",(float)44.194021,(float)1.565552));
	getLocations().put(new Location("Nord pas de calais","Nord-Pas-de-Calais",(float)50.527397,(float)2.801514));
	getLocations().put(new Location("Pays de la loire","Pays de la Loire",(float)47.861089,(float)-0.32959));
	getLocations().put(new Location("Picardie","Picardie",(float)49.735131,(float)2.521362));
	getLocations().put(new Location("Poitou charentes","Poitou-Charentes",(float)45.993145,(float)-0.274658));
	getLocations().put(new Location("Provence alpes cote d'azur","Provence-Alpes-Côte d'Azur",(float)44.000718,(float)6.091919));
	getLocations().put(new Location("Rhone alpes","Rhône-Alpes",(float)45.259422,(float)5.471191));
	getLocations().put(new Location("Ain","Ain",(float)46.24757,(float)5.130768));
	getLocations().put(new Location("Aisne","Aisne",(float)49.476921,(float)3.441737));
	getLocations().put(new Location("Allier","Allier",(float)46.311554,(float)3.416766));
	getLocations().put(new Location("Alpes-de-Haute-Provence","Alpes-de-Haute-Provence",(float)44.077873,(float)6.237595));
	getLocations().put(new Location("Hautes-Alpes","Hautes-Alpes",(float)44.600872,(float)6.322607));
	getLocations().put(new Location("Alpes-Maritimes","Alpes-Maritimes",(float)43.946678,(float)7.179026));
	getLocations().put(new Location("Ardèche","Ardèche",(float)44.759628,(float)4.562443));
	getLocations().put(new Location("Ardennes","Ardennes",(float)49.762463,(float)4.628505));
	getLocations().put(new Location("Ariège","Ariège",(float)42.932629,(float)1.443469));
	getLocations().put(new Location("Aube","Aube",(float)48.156342,(float)4.373246));
	getLocations().put(new Location("Aude","Aude",(float)43.072468,(float)2.381362));
	getLocations().put(new Location("Aveyron","Aveyron",(float)44.217976,(float)2.618927));
	getLocations().put(new Location("Bouches-du-Rhône","Bouches-du-Rhône",(float)43.591167,(float)5.310251));
	getLocations().put(new Location("Calvados","Calvados",(float)49.12133,(float)-0.433058));
	getLocations().put(new Location("Cantal","Cantal",(float)45.119202,(float)2.632606));
	getLocations().put(new Location("Charente","Charente",(float)45.751995,(float)0.153476));
	getLocations().put(new Location("Charente-Maritime","Charente-Maritime",(float)45.749489,(float)-0.773319));
	getLocations().put(new Location("Cher","Cher",(float)46.954006,(float)2.467191));
	getLocations().put(new Location("Corrèze","Corrèze",(float)45.432007,(float)2.019591));
	getLocations().put(new Location("Côte-d'Or","Côte-d'Or",(float)47.51268,(float)4.635412));
	getLocations().put(new Location("Côtes-d'Armor","Côtes-d'Armor",(float)48.510811,(float)-3.326368));
	getLocations().put(new Location("Creuse","Creuse",(float)46.037762,(float)2.062783));
	getLocations().put(new Location("Dordogne","Dordogne",(float)45.14695,(float)0.757221));
	getLocations().put(new Location("Doubs","Doubs",(float)47.196983,(float)6.3126));
	getLocations().put(new Location("Drôme","Drôme",(float)44.73119,(float)5.226668));
	getLocations().put(new Location("Eure","Eure",(float)49.118176,(float)0.958211));
	getLocations().put(new Location("Eure-et-Loir","Eure-et-Loir",(float)48.552525,(float)1.198981));
	getLocations().put(new Location("Finistère","Finistère",(float)48.252026,(float)-3.930052));
	getLocations().put(new Location("Corse-du-Sud","Corse-du-Sud",(float)41.810265,(float)8.924534));
	getLocations().put(new Location("Haute-Corse","Haute-Corse",(float)42.409786,(float)9.278558));
	getLocations().put(new Location("Gard","Gard",(float)43.944698,(float)4.151376));
	getLocations().put(new Location("Haute-Garonne","Haute-Garonne",(float)43.401047,(float)1.135302));
	getLocations().put(new Location("Gers","Gers",(float)43.636646,(float)0.450237));
	getLocations().put(new Location("Gironde","Gironde",(float)44.849667,(float)-0.450237));
	getLocations().put(new Location("Hérault","Hérault",(float)43.591236,(float)3.258363));
	getLocations().put(new Location("Ille-et-Vilaine","Ille-et-Vilaine",(float)48.229202,(float)-1.530069));
	getLocations().put(new Location("Indre","Indre",(float)46.661396,(float)1.448266));
	getLocations().put(new Location("Indre-et-Loire","Indre-et-Loire",(float)47.289494,(float)0.816097));
	getLocations().put(new Location("Isère","Isère",(float)44.995773,(float)5.929348));
	getLocations().put(new Location("Jura","Jura",(float)46.762474,(float)5.672916));
	getLocations().put(new Location("Landes","Landes",(float)43.941204,(float)-0.753281));
	getLocations().put(new Location("Loir-et-Cher","Loir-et-Cher",(float)47.676189,(float)1.415907));
	getLocations().put(new Location("Loire","Loire",(float)45.98465,(float)4.052545));
	getLocations().put(new Location("Haute-Loire","Haute-Loire",(float)45.082123,(float)3.926637));
	getLocations().put(new Location("Loire-Atlantique","Loire-Atlantique",(float)47.278046,(float)-1.815765));
	getLocations().put(new Location("Loiret","Loiret",(float)47.900772,(float)2.201817));
	getLocations().put(new Location("Lot","Lot",(float)44.537937,(float)1.676069));
	getLocations().put(new Location("Lot-et-Garonne","Lot-et-Garonne",(float)44.247017,(float)0.450237));
	getLocations().put(new Location("Lozère","Lozère",(float)44.494202,(float)3.581269));
	getLocations().put(new Location("Maine-et-Loire","Maine-et-Loire",(float)47.291355,(float)-0.487785));
	getLocations().put(new Location("Manche","Manche",(float)49.114712,(float)-1.311595));
	getLocations().put(new Location("Marne","Marne",(float)49.128754,(float)4.147544));
	getLocations().put(new Location("Haute-Marne","Haute-Marne",(float)48.126099,(float)5.107132));
	getLocations().put(new Location("Mayenne","Mayenne",(float)48.238251,(float)-0.504256));
	getLocations().put(new Location("Meurthe-et-Moselle","Meurthe-et-Moselle",(float)48.799702,(float)6.094701));
	getLocations().put(new Location("Meuse","Meuse",(float)49.082432,(float)5.2824));
	getLocations().put(new Location("Morbihan","Morbihan",(float)47.885292,(float)-2.900186));
	getLocations().put(new Location("Moselle","Moselle",(float)49.098385,(float)6.552764));
	getLocations().put(new Location("Nièvre","Nièvre",(float)47.238171,(float)3.529452));
	getLocations().put(new Location("Nord","Nord",(float)50.385124,(float)3.264244));
	getLocations().put(new Location("Oise","Oise",(float)49.421455,(float)2.41464));
	getLocations().put(new Location("Orne","Orne",(float)48.638859,(float)0.08482));
	getLocations().put(new Location("Pas-de-Calais","Pas-de-Calais",(float)50.573277,(float)2.324468));
	getLocations().put(new Location("Puy-de-Dôme","Puy-de-Dôme",(float)45.712414,(float)3.015583));
	getLocations().put(new Location("Pyrénées-Atlantiques","Pyrénées-Atlantiques",(float)43.326996,(float)-0.753281));
	getLocations().put(new Location("Hautes-Pyrénées","Hautes-Pyrénées",(float)43.01939,(float)0.149499));
	getLocations().put(new Location("Pyrénées-Orientales","Pyrénées-Orientales",(float)42.601292,(float)2.539603));
	getLocations().put(new Location("Bas-Rhin","Bas-Rhin",(float)48.634315,(float)7.525294));
	getLocations().put(new Location("Haut-Rhin","Haut-Rhin",(float)47.931503,(float)7.24411));
	getLocations().put(new Location("Rhône","Rhône",(float)45.735146,(float)4.610804));
	getLocations().put(new Location("Haute-Saône","Haute-Saône",(float)47.756981,(float)6.155628));
	getLocations().put(new Location("Saône-et-Loire","Saône-et-Loire",(float)46.582752,(float)4.486671));
	getLocations().put(new Location("Sarthe","Sarthe",(float)47.9217,(float)0.16558));
	getLocations().put(new Location("Savoie","Savoie",(float)45.493206,(float)6.4724));
	getLocations().put(new Location("Haute-Savoie","Haute-Savoie",(float)46.175678,(float)6.538962));
	getLocations().put(new Location("Paris","Paris",(float)48.856613,(float)2.352222));
	getLocations().put(new Location("Seine-Maritime","Seine-Maritime",(float)49.605419,(float)0.974844));
	getLocations().put(new Location("Seine-et-Marne","Seine-et-Marne",(float)48.841084,(float)2.999366));
	getLocations().put(new Location("Yvelines","Yvelines",(float)48.785095,(float)1.825657));
	getLocations().put(new Location("Deux-Sèvres","Deux-Sèvres",(float)46.592655,(float)-0.396284));
	getLocations().put(new Location("Somme","Somme",(float)49.914516,(float)2.270709));
	getLocations().put(new Location("Tarn","Tarn",(float)43.926441,(float)1.988153));
	getLocations().put(new Location("Tarn-et-Garonne","Tarn-et-Garonne",(float)44.012669,(float)1.289104));
	getLocations().put(new Location("Var","Var",(float)43.467648,(float)6.237595));
	getLocations().put(new Location("Vaucluse","Vaucluse",(float)44.056503,(float)5.143207));
	getLocations().put(new Location("Vendée","Vendée",(float)46.661396,(float)-1.448266));
	getLocations().put(new Location("Vienne","Vienne",(float)46.66954,(float)0.477287));
	getLocations().put(new Location("Haute-Vienne","Haute-Vienne",(float)45.743519,(float)1.402548));
	getLocations().put(new Location("Vosges","Vosges",(float)48.144642,(float)6.335594));
	getLocations().put(new Location("Yonne","Yonne",(float)47.865273,(float)3.607982));
	getLocations().put(new Location("Territoire de Belfort","Territoire de Belfort",(float)47.594658,(float)6.920772));
	getLocations().put(new Location("Essonne","Essonne",(float)48.458569,(float)2.156942));
	getLocations().put(new Location("Hauts-de-Seine","Hauts-de-Seine",(float)48.828506,(float)2.218807));
	getLocations().put(new Location("Seine-Saint-Denis","Seine-Saint-Denis",(float)48.913746,(float)2.484573));
	getLocations().put(new Location("Val-de-Marne","Val-de-Marne",(float)48.793144,(float)2.474034));
	getLocations().put(new Location("Val-d'Oise","Val-d'Oise",(float)49.061588,(float)2.158135));
	getLocations().put(new Location("Guadeloupe","Guadeloupe",(float)16.995971,(float)-62.067641));
	getLocations().put(new Location("Martinique","Martinique",(float)14.641528,(float)-61.024174));
	getLocations().put(new Location("Guyane","Guyane",(float)3.933889,(float)-53.125782));
	getLocations().put(new Location("La Réunion","La Réunion",(float)-21.115141,(float)55.536384));
	getLocations().put(new Location("Mayotte","Mayotte",(float)-12.8333,(float)45.1667));*/
    }
    
    public void addLocation(Location loc){
    	getLocations().put(loc.getName(),loc);
    }
}
