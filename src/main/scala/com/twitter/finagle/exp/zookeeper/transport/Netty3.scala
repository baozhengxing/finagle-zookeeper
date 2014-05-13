package com.twitter.finagle.exp.zookeeper.transport

import org.jboss.netty.handler.codec.frame.FrameDecoder
import org.jboss.netty.channel._
import org.jboss.netty.buffer.ChannelBuffer
import com.twitter.finagle.netty3.Netty3Transporter
import com.twitter.util.NonFatal
import com.twitter.finagle.exp.zookeeper._
import org.jboss.netty.buffer.ChannelBuffers._
import com.twitter.finagle.exp.zookeeper.watcher.WatchManager

class PacketFrameDecoder extends FrameDecoder {


  override def decode(ctx: ChannelHandlerContext, channel: Channel, buffer: ChannelBuffer): BufferedResponse = {

    /**
     * Quick solution to solve notification problem
     * 1- Read packet size
     * 2- Read header XID
     * 3- If == -1 then give it to WatchManager
     * 4- If != -1 this is a Response
     */

    println("=== Message Received ===")
    buffer.markReaderIndex()
    val rindex = buffer.readerIndex()

    val size = buffer.readInt // packet size
    val xid = buffer.readInt()

    if(xid == -1){
      buffer.readerIndex(rindex)
      WatchManager.decode(buffer)
      buffer.readerIndex(buffer.writerIndex())
    }else{
      buffer.readerIndex(rindex)
    }

    val bw = BufferedResponse.factory(buffer)
    buffer.readerIndex(buffer.writerIndex)
    bw
  }
}

/**
 * When sending packet, this method is called
 */

class PacketEncoder extends SimpleChannelDownstreamHandler {

  override def writeRequested(ctx: ChannelHandlerContext, evt: MessageEvent) =
    evt.getMessage match {
      case p: Request =>
        try {

          println("=== Message Sent ===")

          val bb = p.toChannelBuffer.toByteBuffer

          bb.putInt(bb.capacity()-4)
          bb.rewind()

          Channels.write(ctx, evt.getFuture, wrappedBuffer(bb), evt.getRemoteAddress)
        } catch {
          case NonFatal(e) =>
            evt.getFuture.setFailure(new ChannelException(e.getMessage))
        }

      case unknown =>
        evt.getFuture.setFailure(new ChannelException(
          "Unsupported request type %s".format(unknown.getClass.getName)))
    }
}

/**
 * A Netty3 pipeline that is responsible for framing network
 * traffic in terms of mysql logical packets.
 */

object ZooKeeperClientPipelineFactory extends ChannelPipelineFactory {
  override def getPipeline: ChannelPipeline = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("packetDecoder", new PacketFrameDecoder)
    pipeline.addLast("packetEncoder", new PacketEncoder)
    pipeline
  }
}

/**
 * Responsible for the transport layer plumbing required to produce
 * a Transport[Packet, Packet]. The current implementation uses
 * Netty3.
 */

object ZooKeeperTransporter extends Netty3Transporter[Request, BufferedResponse](
  name = "zookeeper",
  pipelineFactory = ZooKeeperClientPipelineFactory
)