package com.dataflow.sbt.handlebars


// import all required classes
import sbt._
import sbt.Keys._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.SbtWeb.autoImport._
import com.typesafe.sbt.web.Import.WebKeys
import scala.collection.mutable.{ArrayBuffer => MutList}



object Imports {
  val handlebars = taskKey[Seq[File]]("Join all handlebar files into a single javascript file")
}



object SbtHandlebars extends AutoPlugin {
  /**
   * This case class defines a node of the abstract templates tree
   * @author Samuel Lörtscher
   */
  case class Node(label: String, val children: MutList[Node], val content: Option[String])



  override def requires = SbtWeb
  override def trigger  = AllRequirements

  val autoImport = Imports

  import autoImport._
  import WebKeys._



  /**
   * This private recursive helper method generates a javascript object tree using the
   * parametric node tree structure as input.
   * @author Samuel Lörtscher
   */
  private def generateJavascript(node: Node): String = node match {
    // the parametric node is the root
    case Node("Handlebars", children, None) =>
      s"Handlebars.Templates={${children.map(generateJavascript).mkString(",")}}"
    // the parametric node is a non - leaf
    case Node(label, children, None) =>
      s"${label.capitalize}:{${children.map(generateJavascript).mkString(",")}}"
    // the parametric node is a leaf
    case Node(label, _, Some(content)) =>
      s"${label.capitalize}:'${content.replace("\n","").replace("\r","")}'"
  }



  /**
   * This private helper method transforms the parametric list of template
   * files into an abstract tree
   * @author Samuel Lörtscher
   */
  private def generateTemplateTree(basePath: String, templates: Seq[File]): Node = {
    // start folding with an empty root node
    templates.foldLeft(Node("Handlebars", MutList[Node](), None)) { case (tree, file) =>
      // make the current file path relative to the base path, split it into
      // its tokens and save a ref to the current subtree
      val relativePath = file.absolutePath.replace(basePath, "")
      val tokens = relativePath.split(Path.sep).toSeq
      var node   = tree

      // print a little control message
      println(s"""Processing handlebar template \"$relativePath\"""")

      // process all path tokens
      tokens.foreach { token =>
        // check whether the current token isn't a file
        if (!token.endsWith(".html")) {
          node.children.find(_.label.toLowerCase == token.toLowerCase) match {
            // the tree already contains a matching node for the current path token
            case Some(child) =>
              node = child
            // a corresponding node has to be created and inserted
            case None =>
              val newNode = Node(token, MutList[Node](), None)
              node.children += newNode
              node = newNode
          }
        }
        // otherwise the current token is a file and
        // we append a leaf node into the tree
        else node.children += Node(Path(token).base, MutList[Node](), Some(IO.read(file)))
      }

      // finally return the final abstract tree
      tree
    }
  }


  
  /**
   * This is the entry point of this plugin.
   * @author Samuel Lörtscher
   */
  override def projectSettings = Seq(
    handlebars := {
      // filter out any handlebar templates
      val basePath  = ((sourceDirectory in Assets).value / "handlebars")
      val templates = (basePath ** "*.html").get

      // print a little control message
      println(s"Start processing ${templates.size} handlebar template(s)")

      // generate a template tree using all handlebar templates
      val tree = generateTemplateTree(basePath.absolutePath.stripSuffix(Path.sep.toString) + Path.sep, templates)

      // create a new file context
      (webTarget.value / "handlebars" / "templates.js") match { case destination =>
        // create a new file if it doesn't already exists,
        // generate a javascript object tree and write it to the destination
        IO.touch(destination)
        IO.write(destination, generateJavascript(tree))

        // finally return the destination file to sbt
        Seq(destination)
      }
    }
  )
}