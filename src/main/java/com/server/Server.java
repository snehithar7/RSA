package com.server;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Created by alec.ferguson on 4/20/2017.
 */
public class Server {

    public static void main(String[] args) {
        ChannelFactory factory =
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new PipelineFactory());

        // Keep connections open
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        // Start server on port 4000
        bootstrap.bind(new InetSocketAddress(4000));

        try {
            System.out.println("Listening on " + InetAddress.getLocalHost().toString() + ":" + 4000);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
