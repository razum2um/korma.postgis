## Introduction ##
This project augments the tasty [Korma](https://github.com/korma/Korma) SQL library for [Clojure](http://clojure.org/) with some conveniences for working with [PostGIS](http://postgis.refractions.net/) databases.
This includes conversions from and to [JTS (formerly Java) Topology Suite](http://tsusiatsoftware.net/jts/main.html) geometries to keep you sane
and "aliases" for the PostGIS functions, so you can type less and your editor might even
provide auto-complete.

## Warning ##
The `korma.postgis` library is in no way production ready,
and has currently only been run with PostgreSQL 9.0 / PostGIS 1.5.
Other versions might work, but they also might blow up in your face
and even if you use PostGIS 1.5: you might want to keep your distance.
Not everything has been tested and most of the code was auto-generated.

## Example / rough tutorial ##
```clojure

(use 'korma.core)
(use 'korma.postgis)
; ... define postgres-db here...
```
Please note that the Postgres JDBC driver is not an explicit `korma.postgis` dependency. The PostGIS extension however is included.

Register the PostGIS types in the db-pool...
This is only needed for the `transform-postgis` function (see below).

```clojure

(register-types db)
```

Defining the entities allows you to use JTS geometries in your insert/update statements:

```clojure

(defentity geom-ent
  (prepare prepare-postgis)

  ; this converts PGGeometry to JTS-Geometries ->
  ; "SELECT geom FROM geom_table" gets you JTS-Geometries, if you called register-types
  (transform tranform-postgis)
)

; define some JTS point
(def point (new Point 7.7 8.8))

; simple example
(select geom-ent
  (where (st-within :geom (st-buffer [point 4326] 100))
  (limit 100)
)

```

The spatial functions take either a keyword `(-> column)` or a `seq` with `[geometry, srid]` for the geometry parameter. Geometry can either be a JTS geometry or a WKT string.


#### Generated SQL examples ####
```clojure

(sql-only
  (select geom-ent
  (fields :id (st-x :geom) )
  (where (st-intersects (st-buffer ["POINT(6.6 7.7)" 4326] 10 ) :geom )))
)
```
###### Generates the following SQL: ######
```sql

SELECT "geom-ent"."id", ST_X("geom-ent"."geom")
FROM "geom-ent"
WHERE ST_INTERSECTS(ST_BUFFER(ST_GeometryFromText(?, ?), ?), "geom-ent"."geom")
```

For a 'spatial join' you have to add the extra table using `(from :table)`.

```clojure

(sql-only
  (select korma_postgis_point
  (from :korma_postgis_poly)
  (where (and (st-within :geom :korma_postgis_poly.geom)
  (> :id 0))))
)
```

###### Generates the following SQL: ######

```sql

SELECT "korma_postgis_point".*
FROM "korma_postgis_point", "korma_postgis_poly"
WHERE ST_WITHIN("korma_postgis_point"."geom", "korma_postgis_poly"."geom")
```

### TODO ###
* Currently the input geometries are converted to WKT (Well-known text) which is obviously a stupid idea from a performance standpoint... so change to WKB (Well-known binary) instead.
* Test all those generated SQL function macros/aliases.
* Maybe attach SRID metadata to an entity, so there is no need to provide it every time? (Not sure about this... Having the spatial refrence explicit prevents errors.)

### Features ###
* Allow input of JTS geometries or WKT strings
* Convert `PGgeometry` to JTS
* Generated macros for most of the PostGIS `ST_` functions
* Macros include basic documentation, so you can use e.g. `(doc st-buffer)` to look at PostGIS documentation - which was scraped from the PostGIS function reference page.


### Extension points ###
There is currently one multimethod that can be used to customize `korma.postgis` to your needs:

```clojure

(defmulti to-wkt class)
```
`to-wkt` converts the input parameter into WKT to interface with the database (this WILL change to `to-wkb`) useful if you don't use JTS as your geometry library (although you should).

### I don't use PostGIS; Oracle, MS SQL, ArcSDE, etc. ###
Transforming korma.postgis into korma.spatial to handle all the different spatial SQL databases (MS SQL Server, Oracle `spatial`/`locator`, ArcSDE `ST_Geometry`) is currently not a short-term goal.
Generated queries need to be a bit different depending on the database. Esri's `ST_GEOMETRY` in Oracle, for example, returns `0` or `1` instead of a proper boolean,
so we would have to generate
    `[...] WHERE ST_INTERSECTS(table1.shape, table2.shape) = 1`
instead of
    `[...] WHERE ST_INTERSECTS(table1.shape, table2.shape)`


This might happen, after the API is finalized.


## License ##

Distributed under the Eclipse Public License, the same as Clojure.
