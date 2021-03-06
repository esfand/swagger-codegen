/**
 *  Copyright 2012 Wordnik, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wordnik.swagger.codegen

import com.wordnik.swagger.codegen.language.CodegenConfig
import com.wordnik.swagger.codegen._
import com.wordnik.swagger.codegen.util._
import com.wordnik.swagger.core._
import com.wordnik.swagger.core.util.JsonUtil
import java.io.{ File, FileWriter }

import scala.io._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ ListBuffer, HashMap, HashSet }
import scala.io.Source
import spec.SwaggerSpecValidator

abstract class BasicGenerator extends CodegenConfig with PathUtil {
  def packageName = "com.wordnik.client"
  def templateDir = "src/main/resources/scala"
  def destinationDir = "generated-code/src/main/scala"
  def fileSuffix = ".scala"

  override def invokerPackage: Option[String] = Some("com.wordnik.client.common")
  override def modelPackage: Option[String] = Some("com.wordnik.client.model")
  override def apiPackage: Option[String] = Some("com.wordnik.client.api")

  var codegen = new Codegen(this)
  def json = ScalaJsonUtil.getJsonMapper

  def extractOperations(subDocs:List[Documentation], allModels: HashMap[String, DocumentationSchema] )(implicit basePath:String) = {
    val output = new ListBuffer[(String, String, DocumentationOperation)]
    subDocs.foreach(subDoc => {
      val basePath = subDoc.basePath
      val resourcePath = subDoc.resourcePath
      if (subDoc.getApis != null) {
        subDoc.getApis.foreach(api => {
          for ((apiPath, operation) <- ApiExtractor.extractOperations(basePath, api)) {
            output += Tuple3(basePath, apiPath, operation)
          }
        })
        output.map(op => processOperation(op._2, op._3))
        allModels ++= CoreUtils.extractModels(subDoc)
      }
    })
    output.toList
  }

  def generateClient(args: Array[String]) = {
    if (args.length == 0) {
      throw new RuntimeException("Need url to resources.json as argument. You can also specify VM Argument -DfileMap=/path/to/folder/containing.resources.json/")
    }
    val host = args(0)
    val apiKey = {
      if (args.length > 1) Some("?api_key=" + args(1))
      else None
    }
    val doc = {
      try {
        val jsonString = ResourceExtractor.extractListing(getResourcePath(host), apiKey)
        json.readValue(jsonString, classOf[Documentation])
      } catch {
        case e: Exception => throw new Exception("unable to read from " + host, e)
      }
    }

    implicit val basePath = getBasePath(doc.basePath)

    val apis = doc.getApis
    if (apis == null)
      throw new Exception("No APIs specified by resource")
    val subDocs = ApiExtractor.extractApiDocs(basePath, apis.toList, apiKey)
    // val models = CoreUtils.extractAllModels(subDocs)

    new SwaggerSpecValidator(doc, subDocs).validate()

    val allModels = new HashMap[String, DocumentationSchema]
    val operations = extractOperations(subDocs, allModels)
    val apiMap = groupApisToFiles(operations)

    processApiMap(apiMap)
    processModelMap(allModels)

    codegen.writeSupportingClasses(apiMap.toMap, allModels.toMap)
  }

  def processModelMap(models: HashMap[String, DocumentationSchema]) = {
    val modelBundleList = new ListBuffer[Map[String, AnyRef]]
    for ((name, schema) <- models) {
      if (!defaultIncludes.contains(name)) {
        val m = new HashMap[String, AnyRef]
        m += "name" -> name
        m += "className" -> name
        m += "apis" -> None
        m += "models" -> List((name, schema))
        m += "package" -> modelPackage
        m += "invokerPackage" -> invokerPackage
        m += "outputDirectory" -> (destinationDir + File.separator + modelPackage.getOrElse("").replaceAll("\\.", File.separator))
        m += "newline" -> "\n"
        modelBundleList += m.toMap
        for ((file, suffix) <- modelTemplateFiles) {
          m += "filename" -> (name + suffix)
          generateAndWrite(m.toMap, file)
        }
      }
    }
  }

  def processApiMap(apiMap: Map[(String, String), List[(String, DocumentationOperation)]] ) = {
    for ((identifier, operationList) <- apiMap) {
      val basePath = identifier._1
      val name = identifier._2
      val className = toApiName(name)

      val m = new HashMap[String, AnyRef]
      m += "name" -> name
      m += "className" -> className
      m += "basePath" -> basePath
      m += "package" -> apiPackage
      m += "invokerPackage" -> invokerPackage
      m += "apis" -> Map(className -> operationList.toList)
      m += "models" -> None
      m += "outputDirectory" -> (destinationDir + File.separator + apiPackage.getOrElse("").replaceAll("\\.", File.separator))
      m += "newline" -> "\n"

      for ((file, suffix) <- apiTemplateFiles) {
        m += "filename" -> (className + suffix)
        generateAndWrite(m.toMap, file)
      }
    }
  }

  def generateAndWrite(bundle: Map[String, AnyRef], templateFile: String) = {
    val output = codegen.generateSource(bundle, templateFile)
    val outputDir = new File(bundle("outputDirectory").asInstanceOf[String])
    outputDir.mkdirs

    val filename = outputDir + File.separator + bundle("filename")
    val fw = new FileWriter(filename, false)
    fw.write(output + "\n")
    fw.close()
    println("wrote " + filename)
  }

  def groupApisToFiles(operations: List[(String /*basePath*/ , String /*apiPath*/ , DocumentationOperation /* operation*/ )]): Map[(String, String), List[(String, DocumentationOperation)]] = {
    val opMap = new HashMap[(String, String), ListBuffer[(String, DocumentationOperation)]]
    for ((basePath, apiPath, operation) <- operations) {
      val className = resourceNameFromFullPath(apiPath)
      val listToAddTo = opMap.getOrElse((basePath, className), {
        val l = new ListBuffer[(String, DocumentationOperation)]
        opMap += (basePath, className) -> l
        l
      })
      listToAddTo += Tuple2(apiPath, operation)
    }
    opMap.map(m => (m._1, m._2.toList)).toMap
  }
}
