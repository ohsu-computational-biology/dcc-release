/*
 * Copyright (c) 2015 The Ontario Institute for Cancer Research. All rights reserved.                             
 *                                                                                                               
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * You should have received a copy of the GNU General Public License along with                                  
 * this program. If not, see <http://www.gnu.org/licenses/>.                                                     
 *                                                                                                               
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY                           
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES                          
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT                           
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,                                
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED                          
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;                               
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER                              
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN                         
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.icgc.dcc.release.job.join.function;

import static org.icgc.dcc.common.core.model.FieldNames.DONOR_SPECIMEN;
import static org.icgc.dcc.common.core.model.FieldNames.SubmissionFieldNames.SUBMISSION_DONOR_ID;
import static org.icgc.dcc.release.job.join.utils.JsonNodes.populateArrayNode;
import lombok.val;

import org.apache.spark.api.java.function.Function;

import scala.Tuple2;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;

/**
 * Joins {@code specimen} to {@code donor}.
 */
public class CombineClinical
    implements Function<Tuple2<String, Tuple2<ObjectNode, Optional<Iterable<ObjectNode>>>>, ObjectNode> {

  @Override
  public ObjectNode call(Tuple2<String, Tuple2<ObjectNode, Optional<Iterable<ObjectNode>>>> tuple) throws Exception {
    val donorSpecimen = tuple._2;
    val specimenOpt = donorSpecimen._2;
    val donor = donorSpecimen._1;

    if (specimenOpt.isPresent()) {
      val specimen = donor.withArray(DONOR_SPECIMEN);
      populateArrayNode(specimen, specimenOpt.get(), CombineClinical::trimSpecimen);
    }

    return donor;
  }

  private static ObjectNode trimSpecimen(ObjectNode node) {
    node.remove(SUBMISSION_DONOR_ID);

    return node;
  }

}
