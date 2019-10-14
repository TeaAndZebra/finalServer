package server79;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RegHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
    private int port;
    RegHandler(int port){
        this.port = port;
        ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
        service.scheduleAtFixedRate(new calSpeed(),0,6,TimeUnit.SECONDS);
    }
    /**测试端口路由数据量*/
    private long bitOfPort =0;
    private long speedOfPort = 0;
    private long testPortSpeed = 0;

    public long getSpeedOfPort() {
        return speedOfPort;
    }

    public long getTestPortSpeed() {
        return testPortSpeed;
    }

    public void setBitOfPort(long bitOfPort) {
        this.bitOfPort = bitOfPort;
    }

    public void setSpeedOfPort(long speedOfPort) {
        this.speedOfPort = speedOfPort;
    }

    public void setTestPortSpeed(long testPortSpeed) {
        this.testPortSpeed = testPortSpeed;
    }

    public long getBitOfPort() {
        return bitOfPort;
    }

    class calSpeed implements Runnable{
        @Override
        public void run() {
            /**每6s更新端口路由速率*/
            speedOfPort = testPortSpeed/6;
            testPortSpeed  = 0;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");

    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        msg.retain();
        ByteBuf buf =msg.content();
        byte fByte = buf.getByte(0);
        byte sByte = buf.getByte(1);

        byte[] pdpAddByte = new byte[4];
        buf.getBytes(2,pdpAddByte,0,4);
        int pdpAddInt =   DataChange.bytes2Int(pdpAddByte);

        byte pdpPort =  buf.getByte(6);
        if(SharedTranMap.pdpPortMap.containsValue(pdpAddInt, pdpPort)) {
            if(fByte==(byte)0x55){
               RegImpl reg = new RegImpl(port);//注册时在map中存入对象及该用户RegImpl
                switch (sByte) {
                    case (byte) 0x00:
                        reg.sinRoute(ctx, msg);
                        break;
                    case (byte) 0x01:
                        reg.multiRoute(ctx, msg);
                        break;
                    case (byte) 0x03:
                        reg.reflect(ctx, msg);
                        break;
                    case (byte) 0x04:
                        reg.getNumOfUser(ctx, msg);
                        break;
                    case (byte) 0x05:
                        reg.getBitsOfUser(ctx, msg);
                        break;
                    case (byte) 0x06:
                        reg.getSpeedOfUser(ctx, msg);
                        break;
                }
                reg = null;
            }
        }
    }
}
