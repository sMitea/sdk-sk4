package com.ridko.sk4;

import com.ridko.sk4.handler.ProtocolHandler;
import com.ridko.sk4.listenter.ConnectEvent;
import com.ridko.sk4.listenter.IListenter;
import com.ridko.sk4.protocol.ReaderProtocol;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * 通道处理器
 * @author smitea
 */
class ReaderClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

  private ProtocolHandler protocolHandler;

  private IListenter<ConnectEvent> listenter;

  public void setProtocolHandler(ProtocolHandler protocolHandler) {
    this.protocolHandler = protocolHandler;
  }

  public void setListenter(IListenter<ConnectEvent> listenter) {
    this.listenter = listenter;
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    // 发生异常后关闭
    ctx.close();
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    if (listenter != null) {
      listenter.notify(ConnectEvent.DISCONNECTION);
    }
    super.channelInactive(ctx);
  }

  @Override
  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    if (listenter != null) {
      listenter.notify(ConnectEvent.DISCONNECTED);
    }
    super.channelUnregistered(ctx);
  }

  @Override
  protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
    if (protocolHandler != null) {
      try {
        // 打印数据内容
//        int capacity = byteBuf.capacity();
//        byte[] datas = new byte[capacity];
//        byteBuf.readBytes(datas);
//        String s = HexTools.byteArrayToHexString(datas);

        int HEAD = byteBuf.readByte();
        int TYPE = byteBuf.readByte();
        int LEN = byteBuf.readByte();
        byte[] data = new byte[LEN & 0xFF];
        byteBuf.readBytes(data);
        int CRC = byteBuf.readByte();

//        System.out.println(TYPE +" "+HexTools.byteArrayToHexString(data));

        ReaderProtocol readerProtocol = new ReaderProtocol();
        readerProtocol.setData(data);
        readerProtocol.setCrc(CRC & 0xFF);
        readerProtocol.setType(TYPE & 0xFF);
        // 回调处理
        protocolHandler.handler(readerProtocol);
      } catch (Exception ignored) { }
    }
  }
}
