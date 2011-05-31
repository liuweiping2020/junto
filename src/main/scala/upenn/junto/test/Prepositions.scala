package upenn.junto.test

/**
 * Copyright 2011 Jason Baldridge
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io._
import io.Source
import upenn.junto.config._

class PrepInfo (val id: String,   val verb: String, val noun: String, 
                val prep: String, val pobj: String, val label: String)

object PrepInfoFromLine {
  def apply (line: String) = {
    val Array(id, verb, noun, prep, pobj, label) = line split(" ")
    new PrepInfo(id, verb, noun, prep, pobj, label)
  }
}

object PrepAttachTest {

  def idNode (s: String)   = s+"_ID"
  def verbNode (s: String) = s+"_VERB"
  def nounNode (s: String) = s+"_NOUN"
  def prepNode (s: String) = s+"_PREP"
  def pobjNode (s: String) = s+"_POBJ"

  def createEdges (info: List[PrepInfo]): List[Edge] = {
    (for (item <- info) yield {
      List(EdgeCreator(idNode(item.id), verbNode(item.verb)),
           EdgeCreator(idNode(item.id), nounNode(item.noun)),
           EdgeCreator(idNode(item.id), prepNode(item.prep)),
           EdgeCreator(idNode(item.id), pobjNode(item.pobj)))
    }).toList.flatten
  }

  def createSeeds (info: List[PrepInfo]): List[Seed] =
    info map { item => SeedCreator(idNode(item.id), verbNode(item.label)) }

  def main (args: Array[String]) {
    val ppadir = args(0)

    // Define the file locations
    val trainFile = ppadir+"/training"
    val devFile = ppadir+"/devset"
    val testFile = ppadir+"/test"

    // Convert files to PrepInfo lists
    val trainInfo = (Source fromFile(trainFile) getLines).toList map (PrepInfoFromLine(_))
    val devInfo = (Source fromFile(devFile) getLines).toList map (PrepInfoFromLine(_))
    val testInfo = (Source fromFile(testFile) getLines).toList map (PrepInfoFromLine(_))

    // Create the edges and seeds
    val edges = createEdges(trainInfo) ::: createEdges(devInfo) ::: createEdges(testInfo)
    val seeds = createSeeds(trainInfo)
    val gold = createSeeds(devInfo)

    // Write out the edges and seeds
    val outputDir = new File("data")
    outputDir.mkdirs()
    EdgeFileWriter(edges, outputDir.getPath + "/input_graph")
    SeedFileWriter(seeds, outputDir.getPath + "/seeds")
    SeedFileWriter(gold, outputDir.getPath + "/gold_labels")

  }

}
