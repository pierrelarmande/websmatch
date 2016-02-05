package org.inria.websmatch.tests;

import static org.junit.Assert.*;

import org.inria.websmatch.matchers.base.CollectionMatcher;
import org.junit.Test;

public class InstanceBasedJUnitTest {
    
    // original 100 depts
    public static String[] depts = new String[]{"Ain","Aisne","Allier","Alpes (Hautes)","Alpes Maritimes","Ardéche","Ardennes"," Ariége","Aube","Aude","Aveyron","Bouches du Rhône","Calvados","Cantal","Charente","Charente Maritime","Cher","Corréze","Creuse ","Dordogne","Doubs","Drôme","Eure","Eure et Loir","Finistére","Gard","Garonne (Haute)","Gers","Gironde","Hérault","Ile et Vilaine","Indre","Indre et Loire","Isére","Jura","Landes","Loir et Cher","Loire","Loire (Haute)","Loire Atlantique","Loiret","Lot","Lot et Garonne","Lozére","Maine et Loire","Manche","Marne","Marne (Haute)","Mayenne","Meurthe et Moselle","Meuse","Morbihan","Moselle","Niévre","Nord","Oise","Orne","Pas de Calais","Puy de Dôme","Pyrénées Atlantiques","Pyrénées (Hautes)","Pyrénées Orientales","Rhin (Bas)","Rhin (Haut)","Rhône","Saône (Haute)","Saône et Loire","Sarthe","Savoie","Savoie (Haute)","Paris","Seine Maritime","Seine et Marne","Yvelines","Sèvres (Deux)","Somme","Tarn","Tarn et Garonne","Var","Vaucluse","Vendée","Vienne","Vienne (Haute)","Vosges","Yonne","Belfort (Territoire de)","Essonne","Hauts de Seine","Seine Saint Denis","Val de Marne","Mayotte","Guadeloupe","Guyane","Martinique","Réunion","Côte d'or","Côtes d'armor","Corse du sud","Haute corse","Val d'oise"};

    // 1 changement
    public static String[] depts1change = new String[]{"Ain","Aisne","Alier","Alpes (Hautes)","Alpes Maritimes","Ardéche","Ardennes"," Ariége","Aube","Aude","Aveyron","Bouches du Rhône","Calvados","Cantal","Charente","Charente Maritime","Cher","Corréze","Creuse ","Dordogne","Doubs","Drôme","Eure","Eure et Loir","Finistére","Gard","Garonne (Haute)","Gers","Gironde","Hérault","Ile et Vilaine","Indre","Indre et Loire","Isére","Jura","Landes","Loir et Cher","Loire","Loire (Haute)","Loire Atlantique","Loiret","Lot","Lot et Garonne","Lozére","Maine et Loire","Manche","Marne","Marne (Haute)","Mayenne","Meurthe et Moselle","Meuse","Morbihan","Moselle","Niévre","Nord","Oise","Orne","Pas de Calais","Puy de Dôme","Pyrénées Atlantiques","Pyrénées (Hautes)","Pyrénées Orientales","Rhin (Bas)","Rhin (Haut)","Rhône","Saône (Haute)","Saône et Loire","Sarthe","Savoie","Savoie (Haute)","Paris","Seine Maritime","Seine et Marne","Yvelines","Sèvres (Deux)","Somme","Tarn","Tarn et Garonne","Var","Vaucluse","Vendée","Vienne","Vienne (Haute)","Vosges","Yonne","Belfort (Territoire de)","Essonne","Hauts de Seine","Seine Saint Denis","Val de Marne","Mayotte","Guadeloupe","Guyane","Martinique","Réunion","Côte d'or","Côtes d'armor","Corse du sud","Haute corse","Val d'oise"};

    // 10 changement
    public static String[] depts10change = new String[]{"Ain","Aisne","Alier","Alpes (Hautes)","Alpes maritimes","Ardèche","Ardennes"," Ariége","Aube","Aude","Aveyron","Bouches du Rhône","Calvados","Cantal","Charente","Charente Maritime","Cher","Corréze","Creuse ","Dordogn","Doubs","Drome","eure","Eure Loir","Finistére","Gard","Garonne (Haute)","Gers","Gironde","Hérault","Ile et Vilaine","Indre","Indre et Loire","Isére","Jura","Landes","Loir et Cher","Loire","Loire (Haute)","Loire Atlantique","Loiret","Lot","Lot et Garonne","Lozére","Maine et Loire","Manche","Marne","Marne (Haute)","Mayenne","Meurthe et Moselle","Meuse","Morbihan","Moselle","Niévre","Nord","Oise","Orne","Pas de Calais","Puy de Dôme","Pyrénées Atlantiques","Pyrénées (Hautes)","Pyrénées Orientales","Rhin (Bas)","Rhin (Haut)","Rhône","Saône (Haute)","Saône et Loire","Sarthe","Savoie","Savoie (Haute)","Paris","Seine Maritime","Seine et Marne","Yvelines","Sèvres (Deux)","Somme","Tarn","Tarn et Garonne","Var","Vaucluse","Vendée","Vienne","Vienne (Haute)","Vosges","Yonne","Belfort (Territoire de)","Essonne","Hauts de Seine","Seine Saint Denis","Val de Marne","Mayote","Guadeloupe","Guyane","Martinique","Réunion","Côte d or","Côtes darmor","Corse du sud","Haute corse","Val d'oise"};
    
    // 4 en moins
    public static String[] depts4less = new String[]{"Ain","Allier","Alpes (Hautes)","Alpes Maritimes","Ardéche","Ardennes"," Ariége","Aube","Aude","Aveyron","Bouches du Rhône","Calvados","Cantal","Charente","Cher","Corréze","Creuse ","Dordogne","Doubs","Eure","Eure et Loir","Finistére","Gard","Garonne (Haute)","Gers","Gironde","Hérault","Ile et Vilaine","Indre","Indre et Loire","Isére","Jura","Landes","Loir et Cher","Loire (Haute)","Loire Atlantique","Loiret","Lot","Lot et Garonne","Lozére","Maine et Loire","Manche","Marne","Marne (Haute)","Mayenne","Meurthe et Moselle","Meuse","Morbihan","Moselle","Niévre","Nord","Oise","Orne","Pas de Calais","Puy de Dôme","Pyrénées Atlantiques","Pyrénées (Hautes)","Pyrénées Orientales","Rhin (Bas)","Rhin (Haut)","Rhône","Saône (Haute)","Saône et Loire","Sarthe","Savoie","Savoie (Haute)","Paris","Seine Maritime","Seine et Marne","Yvelines","Sèvres (Deux)","Somme","Tarn","Tarn et Garonne","Var","Vaucluse","Vendée","Vienne","Vienne (Haute)","Vosges","Yonne","Belfort (Territoire de)","Essonne","Hauts de Seine","Seine Saint Denis","Val de Marne","Mayotte","Guadeloupe","Guyane","Martinique","Réunion","Côte d'or","Côtes d'armor","Corse du sud","Haute corse","Val d'oise"};
    
    @Test
    public void testMatch() {
	//fail("Not yet implemented");
	CollectionMatcher cMatcher = new CollectionMatcher();
	assertEquals("Result instance based JUnitTest on same", 1.0, cMatcher.match(depts, depts),0.01);
	
	
	
    }
    
    public static void main(String[] args){
	
	CollectionMatcher cMatcher = new CollectionMatcher();
	// System.out.println("Result instance based JUnitTest with 1 change : " + cMatcher.match(depts, depts1change));
	
	System.out.println("Result instance based JUnitTest with 10 changes : "+cMatcher.match(depts, depts10change));
	
	System.out.println("Result instance based JUnitTest with 4 less : " + cMatcher.match(depts, depts4less));
	
    }

}
