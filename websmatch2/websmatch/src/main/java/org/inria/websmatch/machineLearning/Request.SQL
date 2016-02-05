DROP VIEW errors;

CREATE VIEW errors AS (
SELECT d1.name AS name1, c1.id_sheet AS sheet1, c1.x AS x1, c1.y AS y1, d2.name AS name2, c2.id_sheet AS sheet2, c2.x AS x2, c2.y AS y2
FROM cells c1, cells c2, doc_list d1, doc_list d2
WHERE c1.id_doc = d1.id_doc
AND c2.id_doc = d2.id_doc
AND c1.first_cc_col = c2.first_cc_col
AND c1.first_cc_row = c2.first_cc_row
AND c1.type = c2.type
AND c1.behind_cell = c2.behind_cell
AND c1.right_cell = c2.right_cell
AND c1.is_attribute > c2.is_attribute
AND c1.id_doc <6
AND c2.id_doc <6
AND 
(c1.id_doc != c2.id_doc
OR c1.id_sheet != c2.id_sheet
OR c1.x != c2.x
OR c1.y != c2.y)
ORDER BY c1.id_doc, c2.id_doc, c1.id_sheet, c2.id_sheet, c1.x, c1.y, c2.x, c2.y
)

SELECT classifier, SUM(true_negative), SUM(true_positive), SUM(false_negative),SUM(false_positive), SUM(false_negative)+SUM(false_positive) AS total_false
FROM classifier_results
GROUP BY classifier
ORDER BY total_false


DROP VIEW match_results_view;
CREATE VIEW match_results_view AS
(SELECT r.*,  e1.name AS name1, e2.name AS name2
FROM match_results r, elements e1, elements e2
WHERE id_element1 = e1.id
AND id_element2 = e2.id);

DROP VIEW view_nb_nodes;
CREATE VIEW view_nb_nodes AS (
SELECT schema_id, stored_schemas.name AS name, COUNT(*) AS nb_nodes FROM elements, stored_schemas 
WHERE schema_id = stored_schemas.id GROUP BY schema_id, name
);

DROP VIEW view_total_tuples;
CREATE VIEW view_total_tuples AS 
(SELECT n1.schema_id AS id_schema1, n1.name AS name1, n2.schema_id AS id_schema2, n2.name AS name2,  n1.nb_nodes+n2.nb_nodes AS nb 
FROM  view_nb_nodes n1, view_nb_nodes n2);


DROP VIEW view_total_tuples_8;
CREATE VIEW view_total_tuples_8 AS (
SELECT id_schema1, s1.name AS name1, s1.id_group AS id_group1, id_schema2, s2.name AS name2, s2.id_group AS id_group2 ,COUNT(*) as nb 
FROM match_results r, stored_schemas s1, stored_schemas s2
WHERE id_schema1 = s1.id
AND id_schema2 = s2.id
AND Stoilos_JW > 0.8 GROUP BY id_schema1, name1, id_group1, id_schema2, name2, id_group2
);


DROP VIEW view_total_tuples_9;
CREATE VIEW view_total_tuples_9 AS (
SELECT id_schema1, s1.name AS name1, s1.id_group AS id_group1, id_schema2, s2.name AS name2, s2.id_group AS id_group2 ,COUNT(*) as nb 
FROM match_results r, stored_schemas s1, stored_schemas s2
WHERE id_schema1 = s1.id
AND id_schema2 = s2.id
AND Stoilos_JW > 0.9 GROUP BY id_schema1, name1, id_group1, id_schema2, name2, id_group2
);

DROP VIEW view_schemas_distance_8;
CREATE VIEW view_schemas_distance_8 AS
(SELECT t.id_schema1, t.name1, t8.id_group1, t.id_schema2, t.name2, t8.id_group2, t8.nb / t.nb AS distance
FROM view_total_tuples t, view_total_tuples_8 t8
WHERE  t.id_schema1 = t8.id_schema1
AND t.id_schema2 = t8.id_schema2);

DROP VIEW view_schemas_distance_9;
CREATE VIEW view_schemas_distance_9 AS
(SELECT t.id_schema1,t.name1, t9.id_group1, t.id_schema2, t.name2, t9.id_group2, t9.nb / t.nb AS distance
FROM view_total_tuples t, view_total_tuples_9 t9
WHERE  t.id_schema1 = t9.id_schema1
AND t.id_schema2 = t9.id_schema2);