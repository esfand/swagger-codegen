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

/**
 * @since 7/6/12 9:06 AM
 *
 */
trait PathUtil {
  def getResourcePath(host: String) = {
    System.getProperty("fileMap") match {
      case s: String => {
        s + "/resources.json"
      }
      case _ => host
    }
  }

  def getBasePath(basePath: String) = {
    System.getProperty("fileMap") match {
      case s: String => s
      case _ => basePath
    }
  }

  def toApiName(name: String) = {
    name.charAt(0).toUpperCase + name.substring(1) + "Api"
  }

  def nameFromPath(apiPath: String) = {
    apiPath.split("/")(1).split("\\.")(0).replaceAll("/", "")
  }

  def apiNameFromPath(apiPath: String) = toApiName(nameFromPath(apiPath))

  def resourceNameFromFullPath(apiPath: String) = {
    apiPath.split("/")(1).split("\\.")(0).replaceAll("/", "")
  }
}
