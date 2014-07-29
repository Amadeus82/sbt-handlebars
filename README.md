sbt-handlebars
==============

This is an sbt-web plugin for using handlebar templates in Play in a very simple way

##How to install
Add a resover in project/plugin.sbt
```scala
resolvers ++= Seq(
  Resolver.url("Dataflow repository", url("https://amadeus82.github.io/repo"))(Resolver.ivyStylePatterns)
)
```

Add a plugin dependency in project/plugin.sbt
```scala
addSbtPlugin("com.dataflow.sbt" % "sbt-handlebars" % "1.0.0")
```

Add a library dependency in build.sbt for the external handlebars web-jar
```scala
libraryDependencies ++= Seq(
  "org.webjars" % "handlebars" % "2.0.0-alpha.2"
)
```

Add the handleburs plugin to the list of sourceGenerators in build.sbt
```scala
sourceGenerators in Assets <+= handlebars
```

Reference two javascript files in your view where ever needed (i.e. main.scala.html)
```javascript
<script src="@routes.Assets.at("lib/handlebars/handlebars.min.js")"></script>
<script src="@routes.Assets.at("templates.js")" type="text/javascript" charset="utf-8"></script>
```
Please be aware that the order of those two lines matter as the javascript file (templates.js) generated
by this plugin extends the Handlebar object by a new member Templates.

##Usage:
If you have a handlebars template at
**app/assets/handlebars/filter/standard.html**
This template will be available as a javascript object **Handlebars.Templates.Filter.Standard**

##Example:
In order to render a file:
**app/assets/handlebars/filter/standard.html**
having the variable hint, description and items, you write the following javascript
code on the client side.

```javascript
Handlebars.compile(Handlebars.Templates.Filter.Standard)({
  hint: Messages('layout.filter.dialog.hint.caption'),
  description: Messages('layout.filter.dialog.hint.description'),
  items: items
});
```
