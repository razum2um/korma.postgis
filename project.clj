(defproject korma.postgis "0.1.1-SNAPSHOT"
  :description "Some helpers for working with korma and postgis"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :java-source-path "src"
  :dependencies [[korma "0.4.0"]
                 [com.vividsolutions/jts "1.13"]
                 [org.postgis/postgis-jdbc "1.3.3"
                    :exclusions [postgresql/postgresql] ]]
  :autodoc {:name "Korma Postigs" :page-title "Korma Postgis Docs"}
  :dev-dependencies [[org.clojars.rayne/autodoc "0.8.0-SNAPSHOT"]
                     [enlive "1.0.0"] ;for the documentation scraper
                     [postgresql/postgresql "9.0-801.jdbc4"]])
