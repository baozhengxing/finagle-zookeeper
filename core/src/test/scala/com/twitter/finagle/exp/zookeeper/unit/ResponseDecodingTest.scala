package com.twitter.finagle.exp.zookeeper.unit

import java.util

import com.twitter.finagle.exp.zookeeper.ZookeeperDefs.OpCode
import com.twitter.finagle.exp.zookeeper._
import com.twitter.finagle.exp.zookeeper.data.{Stat, Ids}
import com.twitter.finagle.exp.zookeeper.transport._
import com.twitter.finagle.exp.zookeeper.watcher.Watch
import com.twitter.io.Buf
import com.twitter.util.TimeConversions._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResponseDecodingTest extends FunSuite {
  test("Decode a ReplyHeader") {
    val replyHeader = ReplyHeader(1, 1L, 1)
    val readBuffer = Buf.Empty
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(1L))
      .concat(Buf.U32BE(1))

    val (decodedRep, _) = ReplyHeader
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === replyHeader)
  }

  test("Decode a connect response") {
    val connectResponse = ConnectResponse(
      0, 3000.milliseconds, 123415646L, "password".getBytes, true)

    val readBuffer = Buf.Empty
      .concat(Buf.U32BE(0))
      .concat(Buf.U32BE(3000))
      .concat(Buf.U64BE(123415646L))
      .concat(BufArray("password".getBytes))
      .concat(BufBool(true))

    val (decodedRep, _) = ConnectResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(connectResponse.isRO == decodedRep.isRO)
    assert(connectResponse.protocolVersion == decodedRep.protocolVersion)
    assert(connectResponse.sessionId == decodedRep.sessionId)
    assert(connectResponse.timeOut == decodedRep.timeOut)
    assert(util.Arrays.equals(connectResponse.passwd, decodedRep.passwd))
  }

  test("Decode a create response") {
    val createResponse = CreateResponse("/zookeeper/test")

    val readBuffer = Buf.Empty
      .concat(BufString("/zookeeper/test"))

    val (decodedRep, _) = CreateResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === createResponse)
  }

  test("Decode a create2 response") {
    val create2Response = Create2Response("/zookeeper/test",
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L))

    val readBuffer = Buf.Empty
      .concat(BufString("/zookeeper/test"))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = Create2Response
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === create2Response)
  }

  test("Decode a watch event") {
    val watchEvent = WatchEvent(
      Watch.EventType.NODE_CREATED, Watch.EventState.SYNC_CONNECTED, "/zookeeper/test")

    val readBuffer = Buf.Empty
      .concat(Buf.U32BE(Watch.EventType.NODE_CREATED))
      .concat(Buf.U32BE(Watch.EventState.SYNC_CONNECTED))
      .concat(BufString("/zookeeper/test"))

    val (decodedRep, _) = WatchEvent
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === watchEvent)
  }


  test("Decode an exists response") {
    val existsRep = ExistsResponse(Some(
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L)),
      None)

    val readBuffer = Buf.Empty
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = ExistsResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === existsRep)
  }

  test("Decode a getAcl response") {
    val getAclRep = GetACLResponse(Ids.OPEN_ACL_UNSAFE, Stat(
      0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L))

    val readBuffer = Buf.Empty
      .concat(BufSeqACL(Ids.OPEN_ACL_UNSAFE))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = GetACLResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === getAclRep)
  }

  test("Decode a getChildren response") {
    val getChildrenRep = GetChildrenResponse(
      Seq("/zookeeper/test", "zookeeper/hello"), None)

    val readBuffer = Buf.Empty
      .concat(BufSeqString(Seq("/zookeeper/test", "zookeeper/hello")))

    val (decodedRep, _) = GetChildrenResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === getChildrenRep)
  }

  test("Decode a getChildren2 response") {
    val getChildren2Rep = GetChildren2Response(
      Seq("/zookeeper/test", "zookeeper/hello"),
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L),
      None)

    val readBuffer = Buf.Empty
      .concat(BufSeqString(Seq("/zookeeper/test", "zookeeper/hello")))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = GetChildren2Response
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === getChildren2Rep)
  }

  test("Decode a getData response") {
    val getDataRep = GetDataResponse(
      "change".getBytes,
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L),
      None)

    val readBuffer = Buf.Empty
      .concat(BufArray("change".getBytes))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = GetDataResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(util.Arrays.equals(decodedRep.data, getDataRep.data))
    assert(decodedRep.stat === getDataRep.stat)
  }

  test("Decode a setAcl response") {
    val setAclResponse = SetACLResponse(
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L))

    val readBuffer = Buf.Empty
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = SetACLResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === setAclResponse)
  }

  test("Decode a setData response") {
    val setDataRep = SetDataResponse(
      Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L))

    val readBuffer = Buf.Empty
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

    val (decodedRep, _) = SetDataResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === setDataRep)
  }

  test("Decode a sync response") {
    val syncResponse = SyncResponse("/zookeeper/test")

    val readBuffer = Buf.Empty
      .concat(BufString("/zookeeper/test"))

    val (decodedRep, _) = SyncResponse
      .unapply(readBuffer)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === syncResponse)
  }

  test("decode a transaction") {
    val transactionResponse = TransactionResponse(Seq(
      CreateResponse("/zookeeper/test"),
      Create2Response("/zookeeper/test",
        Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L)),
      SetDataResponse(Stat(0L, 0L, 0L, 0L, 1, 1, 1, 0L, 1, 1, 0L)),
      new EmptyResponse,
      new EmptyResponse
    ))

    val readBuf = Buf.Empty
      .concat(Buf.U32BE(OpCode.CREATE))
      .concat(BufBool(false))
      .concat(Buf.U32BE(0))
      .concat(BufString("/zookeeper/test"))

      .concat(Buf.U32BE(OpCode.CREATE2))
      .concat(BufBool(false))
      .concat(Buf.U32BE(0))
      .concat(BufString("/zookeeper/test"))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

      .concat(Buf.U32BE(OpCode.SET_DATA))
      .concat(BufBool(false))
      .concat(Buf.U32BE(0))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))
      .concat(Buf.U32BE(1))
      .concat(Buf.U32BE(1))
      .concat(Buf.U64BE(0L))

      .concat(Buf.U32BE(OpCode.DELETE))
      .concat(BufBool(false))
      .concat(Buf.U32BE(0))

      .concat(Buf.U32BE(OpCode.CHECK))
      .concat(BufBool(false))
      .concat(Buf.U32BE(0))

      .concat(Buf.U32BE(-1))
      .concat(BufBool(true))
      .concat(Buf.U32BE(0))

    val (decodedRep, _) = TransactionResponse
      .unapply(readBuf)
      .getOrElse(throw new RuntimeException)

    assert(decodedRep === transactionResponse)
  }
}