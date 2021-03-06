import com.wordnik.swagger.core.util.JsonUtil
import com.wordnik.swagger.core.{Documentation, DocumentationSchema}

import com.wordnik.swagger.codegen.BasicScalaGenerator
import com.wordnik.swagger.codegen.util._
import com.wordnik.swagger.codegen.language._
import com.wordnik.swagger.codegen.PathUtil

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.collection.JavaConverters._
import scala.reflect.BeanProperty

@RunWith(classOf[JUnitRunner])
class BasicScalaGeneratorTest extends FlatSpec with ShouldMatchers {
  val json = ScalaJsonUtil.getJsonMapper

  val config = new BasicScalaGenerator

  behavior of "PathUtil"
  /*
   * We will take an api in the spec and create an API name from it
   */
  it should "convert an api name" in {
  	config.toApiName("fun") should be ("FunApi")
  }

  /*
   * We need to generate an API name from the resource path,
   * i.e. /foo will follow rules to become FooApi
  */
  it should "convert a path" in {
  	config.apiNameFromPath("/foo/bar/cats/dogs") should be ("FooApi")
  }

  behavior of "BasicScalaGenerator"
  /*
   * A response of type "void" will turn into a declaration of None
   * for the template generator
   */
  it should "process a response declaration" in {
  	config.processResponseDeclaration("void") should be (None)
  }

  /*
   * swagger strings are turned into scala Strings
   */
  it should "process a string response" in {
  	config.processResponseDeclaration("string") should be (Some("String"))
  }

  /*
   * swagger int is turned into scala Int
   */
  it should "process an unmapped response type" in {
  	config.processResponseDeclaration("int") should be (Some("Int"))
  }

  /*
   * returns the invoker package from the config
   */
  it should "get the invoker package" in {
  	config.invokerPackage should be (Some("com.wordnik.client.common"))
  }

  /*
   * returns the api package
   */
  it should "get the api package" in {
  	config.apiPackage should be (Some("com.wordnik.client.api"))
  }

  /*
   * returns the model package
   */
  it should "get the model package" in {
  	config.modelPackage should be (Some("com.wordnik.client.model"))
  }

  /*
   * types are mapped between swagger types and language-specific
   * types
   */
  it should "convert to a declared type" in {
  	config.toDeclaredType("string") should be ("String")
    config.toDeclaredType("int") should be ("Int")
    config.toDeclaredType("float") should be ("Float")
    config.toDeclaredType("long") should be ("Long")
    config.toDeclaredType("double") should be ("Double")
    config.toDeclaredType("object") should be ("Any")
  }

  /*
   * codegen should honor special imports to avoid generating
   * classes
   */
  it should "honor the import mapping" in {
  	config.importMapping("Date") should be ("java.util.Date")
  }

  /*
   * single tick reserved words
   */
  it should "quote a reserved var name" in {
  	config.toVarName("package") should be ("`package`")
  }

  /*
   * support list declarations with string inner value and the correct default value
   */
   it should "create a declaration with a List of strings" in {
      val model = new DocumentationSchema
      model.name = "arrayOfStrings"
      model.setType("Array")
      model.items = new DocumentationSchema
      model.items.setType("string")

      val m = config.toDeclaration(model)
      m._1 should be ("java.util.List[String]")
      m._2 should be ("_")
   }

  /*
   * support list declarations with int inner value and the correct default value
   */
   it should "create a declaration with a List of ints" in {
      val model = new DocumentationSchema
      model.name = "arrayOfInts"
      model.setType("Array")
      model.items = new DocumentationSchema
      model.items.setType("int")

      val m = config.toDeclaration(model)
      m._1 should be ("java.util.List[Int]")
      m._2 should be ("0")
   }

  /*
   * support list declarations with float inner value and the correct default value
   */
   it should "create a declaration with a List of floats" in {
      val model = new DocumentationSchema
      model.name = "arrayOfFloats"
      model.setType("Array")
      model.items = new DocumentationSchema
      model.items.setType("float")

      val m = config.toDeclaration(model)
      m._1 should be ("java.util.List[Float]")
      m._2 should be ("0f")
   }

  /*
   * support list declarations with double inner value and the correct default value
   */
   it should "create a declaration with a List of doubles" in {
      val model = new DocumentationSchema
      model.name = "arrayOfDoubles"
      model.setType("Array")
      model.items = new DocumentationSchema
      model.items.setType("double")

      val m = config.toDeclaration(model)
      m._1 should be ("java.util.List[Double]")
      m._2 should be ("0.0")
   }


  /*
   * support list declarations with complex inner value and the correct default value
   */
   it should "create a declaration with a List of complex objects" in {
      val model = new DocumentationSchema
      model.name = "arrayOfFloats"
      model.setType("Array")
      model.items = new DocumentationSchema
      model.items.setType("User")

      val m = config.toDeclaration(model)
      m._1 should be ("java.util.List[User]")
      m._2 should be ("_")
   }
}
