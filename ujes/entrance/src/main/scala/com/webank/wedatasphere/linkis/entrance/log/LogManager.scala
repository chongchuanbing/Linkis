/*
 * Copyright 2019 WeBank
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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

package com.webank.wedatasphere.linkis.entrance.log

import com.webank.wedatasphere.linkis.entrance.EntranceContext
import com.webank.wedatasphere.linkis.entrance.job.EntranceExecutionJob
import com.webank.wedatasphere.linkis.scheduler.listener.LogListener
import com.webank.wedatasphere.linkis.scheduler.queue.Job

/**
  * Created by enjoyyin on 2018/9/4.
  */
abstract class LogManager extends LogListener {

  protected var errorCodeListener: Option[ErrorCodeListener] = None
  protected var errorCodeManager: Option[ErrorCodeManager] = None
  protected var entranceContext: EntranceContext = _

  def setEntranceContext(entranceContext: EntranceContext): Unit = this.entranceContext = entranceContext
  def setErrorCodeListener(errorCodeListener: ErrorCodeListener): Unit = this.errorCodeListener = Option(errorCodeListener)
  def setErrorCodeManager(errorCodeManager: ErrorCodeManager): Unit = this.errorCodeManager = Option(errorCodeManager)

  def getLogReader(execId: String): LogReader

  def createLogWriter(job: Job): LogWriter

 override def onLogUpdate(job: Job, log: String): Unit = {
   job match{
     case entranceExecutionJob:EntranceExecutionJob =>
       if (entranceExecutionJob.getLogWriter.isEmpty) entranceExecutionJob synchronized {
         if(entranceExecutionJob.getLogWriter.isEmpty) createLogWriter(entranceExecutionJob)
       }
       entranceExecutionJob.getLogWriter.foreach(logWriter => logWriter.write(log))
       entranceExecutionJob.getWebSocketLogWriter.foreach(writer => writer.write(log))
       errorCodeManager.foreach(_.errorMatch(log).foreach{ case(code, errorMsg) =>
         errorCodeListener.foreach(_.onErrorCodeCreated(job, code, errorMsg))
       })
     case _ =>
   }
//   if (job.isInstanceOf[EntranceExecutionJob]){
//     if (job.asInstanceOf[EntranceExecutionJob].getLogWriter.isEmpty) createLogWriter(job)
//     job.asInstanceOf[EntranceExecutionJob].getLogWriter.foreach(logWriter => logWriter.write(log))
//     createErrorCodeManager().errorMatch(log).foreach{ case(code, errorMsg) =>
//       errorCodeListener.foreach(_.onErrorCodeCreated(job, code, errorMsg))
//     }
//   }
 }
}